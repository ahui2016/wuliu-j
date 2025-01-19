package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class WuliuEditMeta implements Runnable {
    private static ProjectInfo projInfo;
    private static DB db;
    private static final int fileListLimit = 20;

    private JFrame frame;
    private JTextField filenameTF;
    private JButton searchFilenameBtn;
    private JButton searchIdBtn;

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
        projInfo = ProjectInfo.fromJsonFile(MyUtil.PROJ_INFO_PATH);
        MyUtil.checkNotBackup(projInfo);
        db = new DB(MyUtil.WULIU_J_DB);
    }

    @Override
    public void run() {
        createGUI();
        searchIdBtn.addActionListener(new SearchIdListener());
        searchFilenameBtn.addActionListener(new SearchFilenameListener());
        idFileList.addMouseListener(new DoubleClickAdapter());
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

        var updateBtn = new JButton("Update");
        pane_2.add(updateBtn);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.add(BorderLayout.EAST, pane_2);
        frame.setSize(850, 600);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);

        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                filenameTF.requestFocusInWindow();
            }
        });
    }

    private List<String> metaToStringList(List<Simplemeta> metaList) {
        return metaList.stream().map(file -> "[%s] %s".formatted(file.id, file.filename)).toList();
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

    class DoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int i = idFileList.locationToIndex(event.getPoint());
                var file = files.get(i);
                fileIdTF.setText(file.id);
                fillTheForm(file);
            }
        }
    }

    class SearchFilenameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var filename = filenameTF.getText();
            if (filename.isBlank()) {
                files = db.getRecentMetaLimit(fileListLimit);
            } else {
                files = db.getByFilenameLimit(filename, fileListLimit);
            }
            var idFilenames = metaToStringList(files);
            idFileList.setListData(idFilenames.toArray(new String[0]));
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

    class UpdateBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var metaPart = new Simplemeta();
            metaPart.like = Integer.parseInt(likeTF.getText());
            metaPart.label = labelTF.getText();
            metaPart.notes = notesTF.getText();
            metaPart.ctime = ctimeTF.getText();
            metaPart.utime = utimeTF.getText();
            var fileID = readonlyIdTF.getText();
            if (fileID.isBlank() || fileID.equals("id")) {
                JOptionPane.showMessageDialog(frame, "請先尋找檔案");
                return;
            }
            var result = db.getMetaByID(fileID);
            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "找不到ID: " + fileID);
                return;
            }
            var file = result.get();
            var oldText = file.label+file.notes+ file.ctime+file.utime;
            var newText = metaPart.label+metaPart.notes+metaPart.ctime+metaPart.utime;
            if (file.size.equals(metaPart.size) && oldText.equals(newText)) {
                JOptionPane.showMessageDialog(frame, "無變化, 內容未更新。");
                return;
            }
            db.updateMetaPart(metaPart);
        }

        private void updateMetaFile(Simplemeta meta) {}
        private void printResult() {}
    }
}
