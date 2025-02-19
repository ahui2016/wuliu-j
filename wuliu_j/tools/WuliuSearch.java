package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class WuliuSearch implements Runnable{
    private static DB db;
    private static ProjectInfo projInfo;
    private static final String HEART = "❤️";
    private static final int RESULT_LIST_HEIGHT = 550;
    private static final Integer DEFAULT_RESULT_LIMIT = 23;
    private static final int PIC_SIZE = 250;
    private static final int MB = MyUtil.MB;

    private JFrame frame;
    private JCheckBox cBoxFilename;
    private JCheckBox cBoxLabel;
    private JCheckBox cBoxNotes;
    private final List<String> radioButtons = List.of(
            "id", "like", "size", "ctime", "utime");
    private final ButtonGroup btnGroup = new ButtonGroup();
    private JTextField datePrefixTF;
    private JTextField resultLimitTF;
    private JTextField searchTF;
    private JButton searchBtn;

    private List<Simplemeta> result;
    private JList<String> resultList;

    private JLabel previewArea;
    private final List<String> fileFields = List.of(
            "id", "filename", "size", "like",
            "label", "notes", "ctime", "utime");
    private final Map<String, JTextField> fileFormFields = new HashMap<>();
    private JButton renameBtn;
    private JButton exportBtn;
    private JButton deleteBtn;

    public static void main(String[] args) throws IOException {
        initAndCheck();
        SwingUtilities.invokeLater(new WuliuSearch());
    }

    static void initAndCheck() throws IOException {
        projInfo = MyUtil.initCheck();
        MyUtil.checkNotBackup(projInfo);
        db = new DB(MyUtil.WULIU_J_DB);
    }

    @Override
    public void run() {
        createGUI();
        resultList.addMouseListener(new DoubleClickAdapter());
        renameBtn.addActionListener(new RenameBtnListener());
        exportBtn.addActionListener(new ExportBtnListener());
        loadRecentFiles();
        searchTF.requestFocusInWindow();
    }

    public void createGUI() {
        frame = new JFrame("Wuliu Search");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List.of("OptionPane.messageFont", "TextField.font", "ComboBox.font",
                "CheckBox.font", "RadioButton.font", "Label.font"
        ).forEach(k -> UIManager.put(k, MyUtil.FONT_18));

        var pane_1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var pane_r = new JPanel();
        pane_r.setLayout(new BoxLayout(pane_r, BoxLayout.LINE_AXIS));
        var pane_2 = new JPanel();
        var pane_3 = new JPanel();
        pane_r.setPreferredSize(new Dimension(750, RESULT_LIST_HEIGHT+70));
        pane_2.setPreferredSize(new Dimension(420, RESULT_LIST_HEIGHT+70));
        pane_3.setPreferredSize(new Dimension(320, RESULT_LIST_HEIGHT+70));
        pane_r.add(pane_2);
        pane_r.add(pane_3);

        // pane_1
        var pane_boxes = new JPanel();
        pane_boxes.setLayout(new BoxLayout(pane_boxes, BoxLayout.PAGE_AXIS));
        cBoxFilename = new JCheckBox("filename");
        cBoxFilename.setSelected(true);
        cBoxLabel = new JCheckBox("label");
        cBoxNotes = new JCheckBox("notes");

        List.of( cBoxFilename, cBoxLabel, cBoxNotes).forEach(pane_boxes::add);
        pane_1.add(pane_boxes);

        pane_1.add(newSeparator(90));

        var pane_radio = new JPanel();
        pane_radio.setLayout(new BoxLayout(pane_radio, BoxLayout.PAGE_AXIS));
        radioButtons.forEach(name -> {
            var rBtn = new JRadioButton(name);
            rBtn.setActionCommand(name);
            // rBtn.setFont(MyUtil.FONT_18);
            btnGroup.add(rBtn);
            pane_radio.add(rBtn);
            if (name.equals("utime")) rBtn.setSelected(true);
            if (name.equals("ctime")) rBtn.setEnabled(false);
        });
        pane_1.add(pane_radio);

        pane_1.add(newSeparator(90));

        datePrefixTF = new JTextField(5);
        resultLimitTF = new JTextField(5);
        resultLimitTF.setText(DEFAULT_RESULT_LIMIT.toString());
        pane_1.add(new JLabel("date prefix"));
        pane_1.add(datePrefixTF);
        datePrefixTF.setEditable(false);
        pane_1.add(new JLabel("result limit"));
        pane_1.add(resultLimitTF);

        // pane_2
        searchTF = new JTextField(22);
        searchBtn = new JButton("search");
        searchBtn.addActionListener(new SearchBtnListener());
        pane_2.add(searchTF);
        pane_2.add(searchBtn);

        resultList = new JList<>();
        resultList.setFont(MyUtil.FONT_16);
        resultList.setFixedCellWidth(400);
        var scrollPane = new JScrollPane(
                resultList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(410, RESULT_LIST_HEIGHT));
        pane_2.add(scrollPane);

        // pane_3
        previewArea = new JLabel();
        previewArea.setPreferredSize(new Dimension(PIC_SIZE, PIC_SIZE));
        // previewArea.setVisible(false);
        pane_3.add(previewArea);

        fileFields.forEach(name -> {
            var field = new JTextField(name);
            field.setEditable(false);
            // field.setFont(MyUtil.FONT_18);
            field.setColumns(18);
            pane_3.add(field);
            fileFormFields.put(name, field);
        });

        pane_3.add(Box.createRigidArea(new Dimension(350, 10)));
        renameBtn = new JButton("Rename");
        exportBtn = new JButton("Export");
        deleteBtn = new JButton("Delete");
        pane_3.add(renameBtn);
        pane_3.add(exportBtn);
        pane_3.add(deleteBtn);

        // add panels to the frame
        var pane_top = new JPanel();
        pane_top.add(Box.createRigidArea(new Dimension(10, 10)));
        frame.add(pane_top, BorderLayout.NORTH);
        frame.add(pane_1, BorderLayout.CENTER);
        frame.add(pane_r, BorderLayout.EAST);
        frame.setSize(900, RESULT_LIST_HEIGHT+120);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    private void fillFileForm(Simplemeta file) {
        var meta = file.toMap();
        fileFields.forEach(name -> {
            var obj = meta.get(name);
            var text = switch (name) {
                case "size" -> MyUtil.fileSizeToString((long) obj);
                case "like" -> HEART.repeat((int) obj);
                default -> (String) obj;
            };
            var field = fileFormFields.get(name);
            field.setText(text);
        });
    }

    private void resetFileForm() {
        resultList.setListData(new String[0]);
        previewArea.setIcon(new ImageIcon());
        fileFields.forEach(name -> fileFormFields.get(name).setText(name));
    }

    private void loadRecentFiles() {
        var limit = Integer.parseInt(resultLimitTF.getText());
        result = db.getRecentMetaLimit(limit);
        resultList.setListData(metaToStringArray(result));
    }

    private void searchByID() {
        var fileID = searchTF.getText().strip();
        if (fileID.isBlank()) {
            JOptionPane.showMessageDialog(frame, "請輸入檔案ID");
            return;
        }
        var resultOpt = db.getMetaByID(fileID);
        if (resultOpt.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "找不到ID: " + fileID);
            return;
        }
        var file = resultOpt.get();
        result = List.of(file);
        resultList.setListData(metaToStringArray(result));
    }

    private void searchFilenameLabelNotes(int limit) {
        var str = searchTF.getText().strip();
        if (str.isBlank()) {
            loadRecentFiles();
            return;
        }
        var filenameChecked = cBoxFilename.isSelected() ? 1 : 0;
        var labelChecked = cBoxLabel.isSelected() ? 1 : 0;
        var notesChecked = cBoxNotes.isSelected() ? 1 : 0;
        if (filenameChecked+labelChecked+notesChecked == 0) {
            filenameChecked = 1;
            cBoxFilename.setSelected(true);
        }
        if (filenameChecked == 1 && labelChecked+notesChecked == 0) {
            searchFilenameLimit(str, limit);
            return;
        }
        if (labelChecked == 1 && filenameChecked+notesChecked == 0) {
            searchLabelLimit(str, limit);
            return;
        }
        if (notesChecked == 1 && filenameChecked+labelChecked == 0) {
            searchNotesLimit(str, limit);
            return;
        }
        cBoxFilename.setSelected(true);
        cBoxLabel.setSelected(true);
        cBoxNotes.setSelected(true);
        result = db.searchFilenameLabelNotesLimit(str, limit);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "找不到filename/label/notes包含: " + str);
            return;
        }
        resultList.setListData(metaToStringArray(result));
    }

    private void searchFilenameLimit(String str, int limit) {
        result = db.searchFilenameLimit(str, limit);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "找不到filename包含: " + str);
            return;
        }
        resultList.setListData(metaToStringArray(result));
    }

    private void searchLabelLimit(String str, int limit) {
        result = db.searchLabelLimit(str, limit);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "找不到label包含: " + str);
            return;
        }
        resultList.setListData(metaToStringArray(result));
    }

    private void searchNotesLimit(String str, int limit) {
        result = db.searchNotesLimit(str, limit);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "找不到notes包含: " + str);
            return;
        }
        resultList.setListData(metaToStringArray(result));
    }

    private void searchLikeLimit(int limit) {
        result = db.getLikeLimit(limit);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "找不到like大於零的檔案");
            return;
        }
        var listData = result.stream()
                .map(file -> "%s %s".formatted(
                        HEART.repeat(file.like), file.filename))
                .toArray(String[]::new);
        resultList.setListData(listData);
    }

    private void searchSizeLimit(int limit) {
        result = db.getOrderBySize(limit);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "找不到檔案");
            return;
        }
        var listData = result.stream()
                .map(file -> "(%s) %s".formatted(
                        MyUtil.fileSizeToString(file.size), file.filename))
                .toArray(String[]::new);
        resultList.setListData(listData);
    }

    class SearchBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            resultList.setEnabled(false);
            searchTF.setEditable(false);
            searchBtn.setEnabled(false);
            var timer = new javax.swing.Timer(1000, _ -> run());
            timer.setRepeats(false);
            timer.start();
        }

        private void run() {
            var limit = Integer.parseInt(resultLimitTF.getText().strip());
            var radioSelection = btnGroup.getSelection().getActionCommand();
            switch (radioSelection) {
                case "id" -> searchByID();
                case "like" -> searchLikeLimit(limit);
                case "size" -> searchSizeLimit(limit);
                default -> searchFilenameLabelNotes(limit);
            }
            resultList.setEnabled(true);
            searchBtn.setEnabled(true);
            searchTF.setEditable(true);
            searchTF.requestFocusInWindow();
        }
    }

    class DoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int i = resultList.locationToIndex(event.getPoint());
                var file = result.get(i);
                fillFileForm(file);
                resetPreviewArea(file);
            }
        }
    }

    private void resetPreviewArea(Simplemeta meta) {
        if (meta.isImage()) {
            try {
                var file = MyUtil.FILES_PATH.resolve(meta.filename).toFile();
                var image = MyUtil.getImageCropLimit(file, PIC_SIZE);
                previewArea.setIcon(new ImageIcon(image));
            } catch (IOException e) {
                // throw new RuntimeException(e);
                JOptionPane.showMessageDialog(frame, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            previewArea.setIcon(new ImageIcon());
        }
    }

    private Optional<Simplemeta> getOldMeta() {
        var fileID = fileFormFields.get("id").getText();
        if (fileID.isBlank() || fileID.equals("id")) {
            JOptionPane.showMessageDialog(frame, "請先選擇檔案");
            return Optional.empty();
        }
        var oldMetaOpt = db.getMetaByID(fileID);
        if (oldMetaOpt.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "找不到ID: " + fileID);
        }
        return oldMetaOpt;
    }

    class ExportBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var oldMetaOpt = getOldMeta();
            if (oldMetaOpt.isEmpty()) return;
            var oldMeta = oldMetaOpt.get();

            var exportType = JOptionPane.showInputDialog(
                    frame,
                    "Select what to export please:",
                    "Export",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"file", "meta", "file+meta"},
                    "file"
            );
            System.out.println(exportType);
        }

        private Optional<String> checkSizeLimit(long size) {
            long limit = projInfo.exportSizeLimit * MB;
            if (size > limit) {
                var sizeStr = MyUtil.fileSizeToString(size);
                return Optional.of("檔案體積(%s) 超過上限(%s), 請手動導出。"
                        .formatted(sizeStr, projInfo.exportSizeLimit));
            }
            return Optional.empty();
        }

        private void exportFile(Simplemeta meta) {
            var err = checkSizeLimit(meta.size);
            if (err.isPresent()) {
                JOptionPane.showMessageDialog(frame, err.get());
                return;
            }
        }
    }

    class RenameBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var oldMetaOpt = getOldMeta();
            if (oldMetaOpt.isEmpty()) return;
            var oldMeta = oldMetaOpt.get();

            var oldName = fileFormFields.get("filename").getText();
            var newName = JOptionPane.showInputDialog("new filename:", oldName);
            if (newName == null) return;
            newName = newName.strip();
            if (newName.isBlank() || newName.equals(oldName)) {
                return;
            }

            var err = checkFilename(newName);
            if (err.isPresent()) {
                JOptionPane.showMessageDialog(frame, err.get());
                return;
            }
            err = checkFiles(oldMeta.filename, newName);
            if (err.isPresent()) {
                JOptionPane.showMessageDialog(frame, err.get());
                return;
            }
            try {
                renameFile(oldMeta.filename, newName);
                var meta = renameMeta(oldMeta.filename, newName);
                renameInDB(oldMeta.id, meta);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(frame, "更新成功！");

            resetFileForm();
        }

        private Optional<String> checkFilename(String filename) {
            var pattern = Pattern.compile("[:*?<>|/\"\\\\]");
            var matcher = pattern.matcher(filename);
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

    private String[] metaToStringArray(List<Simplemeta> metaList) {
        var heart = "❤️";
        return metaList.stream()
                .map(file -> "%s[%s] %s".formatted(
                        heart.repeat(file.like), file.id, file.filename))
                .toArray(String[]::new);
    }

    private JSeparator newSeparator(int width) {
        var sep = new JSeparator();
        sep.setPreferredSize(new Dimension(width, 2));
        return sep;
    }

/*
    private JTextField newTextField18(int columns) {
        var tf = new JTextField(columns);
        tf.setFont(MyUtil.FONT_18);
        return tf;
    }
*/
}
