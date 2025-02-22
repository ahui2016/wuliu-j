package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class WuliuSearch implements Runnable {
    private static DB db;
    private static ProjectInfo projInfo;

    private static final String HEART = "❤️";
    private static final int RESULT_LIST_HEIGHT = 550;
    private static final int PANEL_HEIGHT = 550 + 70;
    private static final Integer DEFAULT_RESULT_LIMIT = 23;
    private static final int EXPORT_AMOUNT_LIMIT = 25; // 批量導出檔案數量上限
    private static final int PIC_SIZE = 250;
    private static final int MB = MyUtil.MB;

    private boolean isMoreVisible = false;
    private boolean isMoreListsLoaded = false;

    private JFrame frame;
    private JCheckBox cBoxFilename;
    private JCheckBox cBoxLabel;
    private JCheckBox cBoxNotes;
    private final List<String> radioButtons = List.of(
            "id", "like", "size", "ctime", "utime");
    private final ButtonGroup btnGroup = new ButtonGroup();
    private JRadioButton rBtnUTime;
    private JTextField datePrefixTF;
    private JTextField resultLimitTF;
    private JButton moreBtn;
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

    private JPanel pane_more;
    private JList<String> labelList;
    private JList<String> notesList;

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
        moreBtn.addActionListener(new MoreBtnListener());
        searchBtn.addActionListener(new SearchBtnListener());
        renameBtn.addActionListener(new RenameBtnListener());
        exportBtn.addActionListener(new ExportBtnListener());
        deleteBtn.addActionListener(new DeleteBtnListener());
        loadRecentFiles();
        searchTF.requestFocusInWindow();
    }

    private void createGUI() {
        final int labelListWidth = 250;
        final int labelListHeight = RESULT_LIST_HEIGHT;

        frame = new JFrame("Wuliu Search");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List.of("OptionPane.messageFont", "TextField.font", "ComboBox.font",
                "CheckBox.font", "RadioButton.font", "Label.font"
        ).forEach(k -> UIManager.put(k, MyUtil.FONT_18));

        var pane_1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pane_1.setPreferredSize(new Dimension(120, PANEL_HEIGHT));
        var pane_r = new JPanel();
        pane_r.setLayout(new BoxLayout(pane_r, BoxLayout.LINE_AXIS));
        var pane_2 = new JPanel();
        var pane_3 = new JPanel();
        pane_r.setPreferredSize(new Dimension(730, PANEL_HEIGHT));
        pane_2.setPreferredSize(new Dimension(420, PANEL_HEIGHT));
        pane_3.setPreferredSize(new Dimension(310, PANEL_HEIGHT));
        pane_r.add(pane_2);
        pane_r.add(pane_3);
        pane_more = new JPanel(new GridLayout(1, 2, 10, 10));
        pane_more.setPreferredSize(new Dimension(labelListWidth*2+50, PANEL_HEIGHT));

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
            if (name.equals("utime")) {
                rBtn.setSelected(true);
                rBtnUTime = rBtn;
            }
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

        pane_1.add(Box.createRigidArea(new Dimension(90, 20)));
        moreBtn = new JButton("more");
        pane_1.add(moreBtn);

        // pane_2
        searchTF = new JTextField(22);
        searchBtn = new JButton("search");
        pane_2.add(searchTF);
        pane_2.add(searchBtn);

        resultList = new JList<>();
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

        pane_3.add(Box.createRigidArea(new Dimension(300, 10)));
        renameBtn = new JButton("Rename");
        exportBtn = new JButton("Export");
        deleteBtn = new JButton("Delete");
        pane_3.add(renameBtn);
        pane_3.add(exportBtn);
        pane_3.add(deleteBtn);

        // pane_more
        var paneLeft = new JPanel(new FlowLayout());
        paneLeft.add(new JLabel("Labels"));
        labelList = new JList<>();
        var labelListScroll = makeScrollPane(labelList, labelListWidth, labelListHeight);
        paneLeft.add(labelListScroll);
        pane_more.add(paneLeft);
        var paneRight = new JPanel(new FlowLayout());
        paneRight.add(new JLabel("Notes"));
        notesList = new JList<>();
        var notesListScroll = makeScrollPane(notesList, labelListWidth, labelListHeight);
        paneRight.add(notesListScroll);
        pane_more.add(paneRight);
        pane_more.setVisible(isMoreVisible);

        // add panels to the frame
        var pane_top = new JPanel();
        pane_top.add(Box.createRigidArea(new Dimension(10, 10)));
        frame.add(pane_top, BorderLayout.NORTH);
        frame.add(pane_1, BorderLayout.WEST);
        frame.add(pane_r, BorderLayout.CENTER);
        frame.add(pane_more, BorderLayout.EAST);
        frame.setSize(900, RESULT_LIST_HEIGHT+120);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private JScrollPane makeScrollPane(JList<String> list, int listWidth, int height) {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(MyUtil.FONT_16);
        list.setFixedCellWidth(listWidth);
        var scrollPane = new JScrollPane(
                list,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(listWidth+10, height));
        return scrollPane;
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

    class MoreBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isMoreVisible) {
                frame.setSize(900, RESULT_LIST_HEIGHT+120);
                pane_more.setVisible(false);
                isMoreVisible = false;
                return;
            }
            if (!isMoreListsLoaded) {
                var labelsLimit = 500;
                var allLabels = db.getRecentLabels(labelsLimit);
                labelList.setListData(allLabels.toArray(new String[0]));
                labelList.addMouseListener(new LabelsDoubleClickAdapter());
                var allNotes = db.getRecentNotes(labelsLimit);
                notesList.setListData(allNotes.toArray(new String[0]));
                notesList.addMouseListener(new LabelsDoubleClickAdapter());
                isMoreListsLoaded = true;
            }
            frame.setSize(1450, RESULT_LIST_HEIGHT+120);
            pane_more.setVisible(true);
            isMoreVisible = true;
        }
    }

    class LabelsDoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                cBoxFilename.setSelected(false);
                rBtnUTime.setSelected(true);
                String text;
                var list = event.getComponent();
                if (list == labelList) {
                    text = labelList.getSelectedValue();
                    cBoxLabel.setSelected(true);
                    cBoxNotes.setSelected(false);
                } else if (list == notesList) {
                    text = notesList.getSelectedValue();
                    cBoxLabel.setSelected(false);
                    cBoxNotes.setSelected(true);
                } else {
                    text = "";
                }
                searchTF.setText(text);
                searchBtn.doClick();
            }
        }
    }

    class SearchBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            resultList.setEnabled(false);
            searchTF.setEditable(false);
            searchBtn.setEnabled(false);
            var timer = new javax.swing.Timer(1000, _ -> search());
            timer.setRepeats(false);
            timer.start();
        }

        private void search() {
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
            final String listed_files = "listed files";
            var userInput = JOptionPane.showInputDialog(
                    frame,
                    "Select what to export please:",
                    "Export",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"file", "meta", "file+meta", listed_files},
                    "file"
            );
            if (userInput == null) return;
            var exportType = (String) userInput;

            var meta = new Simplemeta();
            if (!exportType.equals(listed_files)) {
                var metaOpt = getOldMeta();
                if (metaOpt.isEmpty()) return;
                meta = metaOpt.get();
            }
            var msg = switch (exportType) {
                case "file" -> exportFile(meta);
                case "meta" -> exportMeta(meta);
                case "file+meta" -> exportFileAndMeta(meta);
                case listed_files -> exportListedFiles();
                default -> "[warning] Unknown Selection";
            };
            msg = "[Project Root] %s%n%s".formatted(Path.of("").toAbsolutePath(), msg);
            JOptionPane.showMessageDialog(frame, msg);
        }

        private Optional<String> checkSizeLimit(Simplemeta meta) {
            long limit = projInfo.exportSizeLimit * MB;
            if (meta.size > limit) {
                return Optional.of("檔案體積超過上限(%s MB), 請手動導出: [%s] %s"
                        .formatted(projInfo.exportSizeLimit, meta.id, meta.filename));
            }
            return Optional.empty();
        }

        private String exportListedFiles() {
            if (result.size() > EXPORT_AMOUNT_LIMIT) {
                return "列表中有 %d 個檔案，超過批量導出上限 (%d 個)".formatted(result.size(), EXPORT_AMOUNT_LIMIT);
            }
            List<String> messages = new ArrayList<>();
            for (var meta : result) {
                messages.add(exportFile(meta));
            }
            return String.join("\n", messages);
        }

        private String exportFileAndMeta(Simplemeta meta) {
            var msg1 = exportFile(meta);
            var msg2 = exportMeta(meta);
            return msg1 + "\n" + msg2;
        }

        private String exportFile(Simplemeta meta) {
            var err = checkSizeLimit(meta);
            if (err.isPresent()) {
                return err.get();
            }
            var src = MyUtil.FILES_PATH.resolve(meta.filename);
            var dst = MyUtil.BUFFER_PATH.resolve(meta.filename);
            return exportFileHelper(src, dst);
        }

        private String exportMeta(Simplemeta meta) {
            var src = MyUtil.getSimplemetaPath(meta.filename);
            var dst = MyUtil.BUFFER_PATH.resolve(src.getFileName());
            return exportFileHelper(src, dst);
        }

        private String exportFileHelper(Path src, Path dst) {
            if (Files.exists(dst)) {
                return "[warning] file exists: " + dst;
            }
            String msg = "Export => " + dst;
            try {
                Files.copy(src, dst);
            } catch (IOException e) {
                msg += "\n" + e.getMessage();
            }
            return msg;
        }
    }

    class DeleteBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var metaOpt = getOldMeta();
            if (metaOpt.isEmpty()) return;
            var meta = metaOpt.get();
            int n = JOptionPane.showConfirmDialog(frame, "要刪除這個檔案嗎:\n"+meta.filename);
            if (n != JOptionPane.YES_OPTION) {
                return;
            }
            var file = MyUtil.FILES_PATH.resolve(meta.filename);
            var metaFile = MyUtil.getSimplemetaPath(meta.filename);
            var msg = "[Project Root] " + Path.of("").toAbsolutePath() + "\n";
            msg += moveFileToRecycle(file);
            msg += moveFileToRecycle(metaFile);
            db.deleteSimplemeta(meta.id);
            JOptionPane.showMessageDialog(frame, msg);
            resetFileForm();
        }

        private String moveFileToRecycle(Path file) {
            if (Files.notExists(file)) {
                return "Not Found => " + file + "\n";
            }
            var dst = MyUtil.RECYCLEBIN_PATH.resolve(file.getFileName());
            var msg = "Move => " + dst + "\n";
            try {
                Files.move(file, dst, REPLACE_EXISTING);
            } catch (IOException e) {
                msg += e.getMessage() + "\n";
            }
            return msg;
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
