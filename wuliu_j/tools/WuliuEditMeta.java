package wuliu_j.tools;

import com.fasterxml.jackson.jr.ob.JSON;
import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class WuliuEditMeta implements Runnable {
    private static DB db;
    private static final int fileListLimit = 20;
    private static final int pictureSizeLimit = 200;

    private JFrame frame;
    private JTextField filenameTF;
    private JButton searchFilenameBtn;
    private JButton searchIdBtn;
    private JButton likeBtn;
    private JButton updateBtn;
    private JLabel previewArea;

    private JTextField fileIdTF;
    private JTextField readonlyIdTF;
    private JTextField readonlyFilenameTF;
    private JTextField readonlySizeTF;
    private JTextField likeTF;
    private JTextField labelTF;
    private JTextField notesTF;
    private JTextField ctimeTF;
    private JTextField utimeTF;

    private List<Simplemeta> files;
    private JList<String> idFileList;

    public static void main(String[] args) throws IOException {
        initAndCheck();
        SwingUtilities.invokeLater(new WuliuEditMeta());
    }

    static void initAndCheck() throws IOException {
        var projInfo = MyUtil.initCheck();
        MyUtil.checkNotBackup(projInfo);
        db = new DB(MyUtil.WULIU_J_DB);
    }

    @Override
    public void run() {
        createGUI();
        searchIdBtn.addActionListener(new SearchIdListener());
        searchFilenameBtn.addActionListener(new SearchFilenameListener());
        idFileList.addMouseListener(new DoubleClickAdapter());
        likeBtn.addActionListener(new LikeBtnListener());
        updateBtn.addActionListener(new UpdateBtnListener());

        searchFilename();
        filenameTF.requestFocusInWindow();
    }

    public void createGUI() {
        frame = new JFrame("Wuliu Edit Meta");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var pane_1 = new JPanel();
        var pane_2 = new JPanel();
        pane_2.setPreferredSize(new Dimension(400, 500));

        var filenamePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var filenameLabel = new JLabel("Filename:");
        filenameTF = new JTextField(20);
        filenameTF.setFont(MyUtil.FONT_18);
        filenamePane.add(filenameLabel);
        filenamePane.add(filenameTF);
        pane_1.add(filenamePane);

        searchFilenameBtn = new JButton("Search");
        pane_1.add(searchFilenameBtn);

        idFileList = new JList<>();
        idFileList.setFont(MyUtil.FONT_16);
        idFileList.setFixedCellWidth(400);
        pane_1.add(idFileList);

        var fileIdPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var fileIdLabel = new JLabel("ID:");
        fileIdTF = new JTextField(10);
        fileIdTF.setFont(MyUtil.FONT_18);
        searchIdBtn = new JButton("Search");
        fileIdPane.add(fileIdLabel);
        fileIdPane.add(fileIdTF);
        fileIdPane.add(searchIdBtn);
        pane_2.add(fileIdPane);

        previewArea = new JLabel();
        // previewArea.setBorder(new EmptyBorder(0, 100, 0, 100));
        pane_2.add(previewArea);

        readonlyIdTF = new JTextField("id");
        readonlyIdTF.setEditable(false);
        readonlyFilenameTF = new JTextField("filename");
        readonlyFilenameTF.setEditable(false);
        readonlySizeTF = new JTextField("size");
        readonlySizeTF.setEditable(false);
        likeTF = new JTextField("like");
        labelTF = new JTextField("label");
        notesTF = new JTextField("notes");
        ctimeTF = new JTextField("ctime");
        utimeTF = new JTextField("utime");
        List<JTextField> textFields = List.of(
                readonlyIdTF, readonlyFilenameTF, readonlySizeTF,
                likeTF, labelTF, notesTF, ctimeTF, utimeTF
        );
        textFields.forEach(tf -> {
            tf.setFont(MyUtil.FONT_18);
            tf.setColumns(25);
            pane_2.add(tf);
        });

        likeBtn = new JButton("Like");
        updateBtn = new JButton("Update");
        pane_2.add(likeBtn);
        pane_2.add(updateBtn);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.add(BorderLayout.EAST, pane_2);
        frame.setSize(850, 600);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    private List<String> metaToStringList(List<Simplemeta> metaList) {
        var heart = "❤️";
        return metaList.stream().map(file -> "%s[%s] %s".formatted(
                heart.repeat(file.like), file.id, file.filename)).toList();
    }

    private Integer getLike() {
        var like = 0;
        var likeStr = likeTF.getText();
        if (likeStr.isBlank()) return 0;
        try {
            like = Integer.parseInt(likeStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame,
                    "不允許輸入 [%s], 請輸入數字。".formatted(likeStr));
            like = -1;
        }
        return like;
    }

    private void fillTheForm(Simplemeta file) {
        readonlyIdTF.setText(file.id);
        readonlyFilenameTF.setText(file.filename);
        readonlySizeTF.setText(MyUtil.fileSizeToString(file.size));
        likeTF.setText(file.like.toString());
        labelTF.setText(file.label);
        notesTF.setText(file.notes);
        ctimeTF.setText(file.ctime);
        utimeTF.setText(file.utime);
    }

    private void resetPreviewArea(Simplemeta meta) {
        if (meta.isImage()) {
            try {
                var file = MyUtil.FILES_PATH.resolve(meta.filename).toFile();
                var image = MyUtil.getImageCropLimit(file, pictureSizeLimit);
                previewArea.setIcon(new ImageIcon(image));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            previewArea.setIcon(new ImageIcon());
        }
    }

    private void searchFilename() {
        var filename = filenameTF.getText();
        if (filename.isBlank()) {
            files = db.getRecentMetaLimit(fileListLimit);
        } else {
            files = db.searchFilenameLimit(filename, fileListLimit);
        }
        var idFilenames = metaToStringList(files);
        idFileList.setListData(idFilenames.toArray(new String[0]));
    }

    class DoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int i = idFileList.locationToIndex(event.getPoint());
                var file = files.get(i);
                fileIdTF.setText(file.id);
                fillTheForm(file);
                resetPreviewArea(file);
            }
        }
    }

    class SearchFilenameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            searchFilename();
        }
    }

    class SearchIdListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var fileID = fileIdTF.getText();
            if (fileID.isBlank()) {
                JOptionPane.showMessageDialog(frame, "請輸入檔案ID");
                return;
            }
            var result = db.getMetaByID(fileID);
            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "找不到ID: " + fileID);
                return;
            }
            var file = result.get();
            fillTheForm(file);
        }
    }

    class LikeBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var like = getLike();
            if (like < 0) {
                likeTF.requestFocusInWindow();
                return;
            }
            like++;
            likeTF.setText(like.toString());
        }
    }

    class UpdateBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var fileID = readonlyIdTF.getText();
            if (fileID.isBlank() || fileID.equals("id")) {
                JOptionPane.showMessageDialog(frame, "請先尋找檔案");
                return;
            }
            var metaPart = new Simplemeta();
            metaPart.id = fileID;
            var like = getLike();
            if (like < 0) {
                likeTF.requestFocusInWindow();
                return;
            }
            metaPart.like = like;
            metaPart.label = labelTF.getText();
            metaPart.notes = notesTF.getText();
            metaPart.ctime = ctimeTF.getText();
            metaPart.utime = utimeTF.getText();
            var result = db.getMetaByID(fileID);
            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "找不到ID: " + fileID);
                return;
            }
            var file = result.get();
            var oldText = file.label+file.notes+file.ctime+file.utime;
            var newText = metaPart.label+metaPart.notes+metaPart.ctime+metaPart.utime;
            if (file.like.equals(metaPart.like) && oldText.equals(newText)) {
                JOptionPane.showMessageDialog(frame, "無變化, 內容未更新。");
                return;
            }
            db.updateMetaPart(metaPart);
            try {
                updateMetaFile(fileID);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            JOptionPane.showMessageDialog(frame, "更新成功！");
            System.exit(0);
        }

        private void updateMetaFile(String fileID) throws IOException {
            var meta = db.getMetaByID(fileID).orElseThrow();
            var metaPath = MyUtil.getSimplemetaPath(meta.filename);
            var metaJson = JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).asString(meta.toMap());
            System.out.println("Update => " + metaPath);
            Files.write(metaPath, metaJson.getBytes());
            System.out.println(metaJson);
        }
    }
}
