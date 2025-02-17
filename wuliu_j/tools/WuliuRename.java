package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class WuliuRename implements Runnable {
    private static DB db;
    private static final int fileListLimit = 20;
    private static final int pictureSizeLimit = 200;

    private JFrame frame;
    private JButton searchFilenameBtn;
    private JButton searchIdBtn;
    private JButton renameBtn;
    private JLabel previewArea;

    private JTextField fileIdTF;
    private JTextField readonlyIdTF;
    private JTextField filenameTF;
    private JTextField filenameTF_2;
    private JTextField readonlyLabelTF;
    private JTextField readonlyNotesTF;

    private List<Simplemeta> files;
    private JList<String> idFileList;

    public static void main(String[] args) throws IOException {
        initAndCheck();
        SwingUtilities.invokeLater(new WuliuRename());
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
        renameBtn.addActionListener(new RenameBtnListener());
    }

    public void createGUI() {
        frame = new JFrame("Wuliu Rename");
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
        filenameTF_2 = new JTextField("filename");
        filenameTF_2.setEditable(false);
        readonlyLabelTF = new JTextField("label");
        readonlyLabelTF.setEditable(false);
        readonlyNotesTF = new JTextField("notes");
        readonlyNotesTF.setEditable(false);
        List<JTextField> textFields = List.of(
                readonlyIdTF, filenameTF_2,
                readonlyLabelTF, readonlyNotesTF
        );
        textFields.forEach(tf -> {
            tf.setFont(MyUtil.FONT_18);
            tf.setColumns(25);
            pane_2.add(tf);
        });

        renameBtn = new JButton("Rename");
        pane_2.add(Box.createRigidArea(new Dimension(350, 10)));
        pane_2.add(renameBtn);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.add(BorderLayout.EAST, pane_2);
        frame.setSize(850, 600);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);

        filenameTF.requestFocusInWindow();
    }

    private List<String> metaToStringList(List<Simplemeta> metaList) {
        var heart = "❤️";
        return metaList.stream().map(file -> "%s[%s] %s".formatted(
                heart.repeat(file.like), file.id, file.filename)).toList();
    }

    private void fillTheForm(Simplemeta file) {
        readonlyIdTF.setText(file.id);
        filenameTF_2.setText(file.filename);
        readonlyLabelTF.setText(file.label);
        readonlyNotesTF.setText(file.notes);
        filenameTF_2.setEditable(true);
        filenameTF_2.requestFocusInWindow();
    }

    private void resetPreviewArea(Simplemeta meta) {
        if (meta.isImage()) {
            try {
                var file = MyUtil.FILES_PATH.resolve(meta.filename).toFile();
                var image = MyUtil.getImageCropLimit(file, pictureSizeLimit);
                previewArea.setIcon(new ImageIcon(image));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage());
                System.exit(1);
            }
        } else {
            previewArea.setIcon(new ImageIcon());
        }
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
            var filename = filenameTF.getText().strip();
            if (filename.isBlank()) {
                files = db.getRecentMetaLimit(fileListLimit);
            } else {
                files = db.searchFilenameLimit(filename, fileListLimit);
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

    class RenameBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var fileID = readonlyIdTF.getText();
            if (fileID.isBlank() || fileID.equals("id")) {
                JOptionPane.showMessageDialog(frame, "請先尋找檔案");
                return;
            }
            var metaPart = new Simplemeta();
            metaPart.id = fileID;
            metaPart.filename = filenameTF_2.getText().strip();
            var result = db.getMetaByID(fileID);
            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "找不到ID: " + fileID);
                return;
            }
            var oldMeta = result.get();
            var err = checkFilename(oldMeta.filename, metaPart.filename);
            if (err.isPresent()) {
                JOptionPane.showMessageDialog(frame, err.get());
                return;
            }
            err = checkFiles(oldMeta.filename, metaPart.filename);
            if (err.isPresent()) {
                JOptionPane.showMessageDialog(frame, err.get());
                return;
            }
            try {
                renameFile(oldMeta.filename, metaPart.filename);
                var meta = renameMeta(oldMeta.filename, metaPart.filename);
                renameInDB(oldMeta.id, meta);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(frame, "更新成功！");
            System.exit(0);
        }

        private Optional<String> checkFilename(String oldName, String newName) {
            if (oldName.equals(newName)) return Optional.of("檔案名稱無變化。");
            var pattern = Pattern.compile("[:*?<>|/\"\\\\]");
            var matcher = pattern.matcher(newName);
            if (matcher.find()) {
                return Optional.of("檔案名稱不允許包含這些字符 \\/:*?\"<>|");
            }
            return Optional.empty();
        }

        private Optional<String> checkFiles(String oldName, String newName) {
            var oldFile = MyUtil.FILES_PATH.resolve(oldName);
            var newFile = MyUtil.FILES_PATH.resolve(newName);
            var oldMeta = MyUtil.getSimplemetaPath(oldName);
            var newMeta = MyUtil.getSimplemetaPath(newName);
            if (Files.notExists(oldFile)) return Optional.of("Not Found: " + oldFile);
            if (Files.exists(newFile)) return Optional.of("File Exists: " + newFile);
            if (Files.notExists(oldMeta)) return Optional.of("Not Found: " + oldMeta);
            if (Files.exists(newMeta)) return Optional.of("File Exists: " + newMeta);
            return Optional.empty();
        }

        private void renameFile(String oldName, String newName) throws IOException {
            var oldFile = MyUtil.FILES_PATH.resolve(oldName);
            var newFile = MyUtil.FILES_PATH.resolve(newName);
            System.out.printf("Rename %s => %s%n", oldFile, newFile);
            Files.move(oldFile, newFile);
        }

        private Simplemeta renameMeta(String oldName, String newName) throws IOException {
            var oldMetaPath = MyUtil.getSimplemetaPath(oldName);
            var newMetaPath = MyUtil.getSimplemetaPath(newName);
            System.out.printf("Rename %s => %s%n", oldMetaPath, newMetaPath);
            var meta = new Simplemeta();
            meta.readFromJsonFile(oldMetaPath);
            meta.id = Simplemeta.nameToID(newName);
            meta.filename = newName;
            meta.type = Simplemeta.typeByFilename(newName);
            MyUtil.writeJsonToFilePretty(meta.toMap(), newMetaPath.toFile());
            Files.delete(oldMetaPath);
            return meta;
        }

        private void renameInDB(String oldID, Simplemeta meta) {
            System.out.println("Update database...");
            // 要先刪除, 否則 checksum 衝突。
            db.deleteSimplemeta(oldID);
            db.insertSimplemeta(meta);
        }
    }
}
