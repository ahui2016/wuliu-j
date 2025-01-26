package wuliu_j.tools;

import wuliu_j.common.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WuliuBackup implements Runnable {
    private static final int textCols = 35;

    private static int bkProjIndex;
    private static ProjectStatus projStat_1;
    private static ProjectStatus projStat_2;

    private static JTextField proj1TF;
    private static JTextField proj2TF;
    private static List<String> bkProjects;
    private static JList<String> bkProjList;
    private static JTextArea msgArea;
    private static JButton backupBtn;

    private JFrame frame;
    private JPanel pane_1;

    public static void main(String[] args) throws IOException {
        projStat_1 = new ProjectStatus(Path.of(".").toAbsolutePath().normalize());
        SwingUtilities.invokeLater(new WuliuBackup());
    }

    @Override
    public void run() {
        createGUI();
        var toIndex = projStat_1.projInfo.projects.size();
        bkProjects = projStat_1.projInfo.projects.subList(1, toIndex);

        proj1TF.setText(projStat_1.projRoot.toString());
        bkProjList.setListData(bkProjects.toArray(new String[0]));
        bkProjList.addMouseListener(new DoubleClickAdapter());
        backupBtn.addActionListener(new BackupBtnListener());
    }

    private void createGUI() {
        frame = new JFrame("Wuliu Checksum");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pane_1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pane_1.add(new JLabel("專案 1"));
        proj1TF = new JTextField(textCols);
        proj1TF.setFont(MyUtil.FONT_16);
        proj1TF.setEditable(false);
        pane_1.add(proj1TF);

        pane_1.add(new JLabel("請選擇備份專案(按兩下):"));
        bkProjList = new JList<>();
        bkProjList.setFont(MyUtil.FONT_16);
        bkProjList.setFixedCellWidth(450);
        pane_1.add(bkProjList);

        pane_1.add(new JLabel("專案 2"));
        proj2TF = new JTextField(textCols);
        proj2TF.setFont(MyUtil.FONT_16);
        proj2TF.setEditable(false);
        pane_1.add(proj2TF);

        msgArea = new JTextArea(17, textCols);
        msgArea.setFont(MyUtil.FONT_16);
        pane_1.add(new JScrollPane(msgArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

        backupBtn = new JButton("Backup");
        pane_1.add(backupBtn);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    /**
     * @return 返回字符串作為出錯消息。
     */
    private Optional<String> checkStatus() {
        if (!projStat_1.projInfo.projectName.equals(projStat_2.projInfo.projectName)) {
            return Optional.of("專案名稱不一致:%n'%s' != '%s'%n".formatted(
                    projStat_1.projInfo.projectName, projStat_2.projInfo.projectName));
        }
        if (!projStat_2.projInfo.isBackup) {
            return Optional.of("專案2不是備份專案:%n%s裡的 IsBackup 不是 true%n".formatted(
                    projStat_2.projRoot.resolve(MyUtil.PROJECT_JSON)));
        }
        if (bkProjects.isEmpty()) {
            return Optional.of(
            "無備份專案。添加備份專案的方法請參閱\n"+projStat_1.projInfo.repoURL);
        }
        if (projStat_1.totalDamaged + projStat_2.totalDamaged > 0) {
            return Optional.of("發現損壞的檔案, 請使用 WuliuChecksum 進行修復。");
        }
        var sizeDiff = projStat_1.totalSize - projStat_2.totalSize;
        return checkDiskUsage(projStat_2.projRoot, sizeDiff);
    }

    /**
     * @return 返回字符串作為出錯消息。
     */
    private Optional<String> checkDiskUsage(Path bkProjRoot, long wantSpace) {
        if (wantSpace <= 0) {
            return Optional.empty();
        }
        long usableSpace = bkProjRoot.toFile().getUsableSpace();
        long margin = 1 << 30; // 1GB
        if (wantSpace+margin < usableSpace) {
            return Optional.empty();
        }
        var want = MyUtil.fileSizeToString(wantSpace);
        var usable = MyUtil.fileSizeToString(usableSpace);
        return Optional.of(
            "磁盤空間不足: %s%n want: %s, usable: %s%n".formatted(
                    bkProjRoot, want, usable));
    }

    void printStatus() {
        var totalSize = MyUtil.fileSizeToString(projStat_1.totalSize);
        var lastBackup1 = projStat_1.projInfo.lastBackupAt.getFirst();
        msgArea.setText("");
        msgArea.append("專案 1     %s%n".formatted(projStat_1.projRoot));
        msgArea.append("檔案數量  %d%n".formatted(projStat_1.totalFiles));
        msgArea.append("體積合計  %s%n".formatted(totalSize));
        msgArea.append("受損檔案  %d%n".formatted(projStat_1.totalDamaged));
        msgArea.append("上次備份  %s%n".formatted(lastBackup1));
        msgArea.append("\n");
        totalSize = MyUtil.fileSizeToString(projStat_2.totalSize);
        var lastBackup2 = projStat_1.projInfo.lastBackupAt.get(bkProjIndex);
        msgArea.append("專案 2     %s%n".formatted(projStat_2.projRoot));
        msgArea.append("檔案數量  %d%n".formatted(projStat_2.totalFiles));
        msgArea.append("體積合計  %s%n".formatted(totalSize));
        msgArea.append("受損檔案  %d%n".formatted(projStat_2.totalDamaged));
        msgArea.append("上次備份  %s%n".formatted(lastBackup2));
        msgArea.append("\n");
        var sizeDiff = projStat_1.totalSize - projStat_2.totalSize;
        var timeDiff = lastBackup1.equals(lastBackup2) ? "相同" : "不同";
        msgArea.append("專案1檔案數量 - 專案2檔案數量 = %d%n".formatted(projStat_1.totalFiles-projStat_2.totalFiles));
        msgArea.append("專案1檔案體積 - 專案2檔案體積 = %s%n".formatted(MyUtil.fileSizeToString(sizeDiff)));
        msgArea.append("上次備份時間: %s%n".formatted(timeDiff));
    }

    class DoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int i = bkProjList.locationToIndex(event.getPoint());
                bkProjIndex = i+1;
                var bkProjRoot = Path.of(bkProjects.get(i));
                try {
                    projStat_2 = new ProjectStatus(bkProjRoot);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                proj2TF.setText(bkProjRoot.toString());
                printStatus();
            }
        }
    }

    class BackupBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (projStat_2 == null) {
                JOptionPane.showMessageDialog(frame, "請先選擇備份專案");
                return;
            }
            var err = checkStatus();
            if (err.isPresent()) {
                JOptionPane.showMessageDialog(frame, err.get());
                return;
            }
            msgArea.append("\n正在準備...\n");
            var filesChanged = new FilesChanged(
                    projStat_1.projRoot, projStat_2.projRoot, projStat_1.db, projStat_2.db);
            if (filesChanged.count() == 0) {
                msgArea.append("沒有需要備份的檔案。");
                return;
            }
            msgArea.append("準備結束, 開始備份 ( %d 個檔案)%n".formatted(filesChanged.count()));
            filesChanged.syncOneWay(msgArea);
            msgArea.append("\n備份結束。\n");
        }
    }
}

class ProjectStatus {
    DB db;
    ProjectInfo projInfo;
    Path projRoot;
    long totalSize;   // 全部檔案體積合計
    int totalFiles;   // 檔案數量合計
    int totalDamaged; // 受損檔案數量合計

    ProjectStatus(Path projRoot) throws IOException {
        var dbPath = projRoot.resolve(MyUtil.WULIU_J_DB);
        MyUtil.pathMustExists(dbPath);
        db = new DB(dbPath.toString());
        this.projRoot = projRoot;
        projInfo = ProjectInfo.fromJsonFile(projRoot.resolve(MyUtil.PROJECT_JSON));
        totalSize = db.sumMetaSize();
        totalFiles = db.countSimplemeta();
        totalDamaged = db.countDamaged();
    }
}

class FilesChanged {
    Path root1;
    Path root2;
    DB db1;
    DB db2;
    List<Simplemeta> deleted = new ArrayList<>();
    List<Simplemeta> updated = new ArrayList<>();
    List<Simplemeta> overwritten = new ArrayList<>();
    List<Simplemeta> added = new ArrayList<>();

    FilesChanged(Path root1, Path root2, DB db1, DB db2) {
        this.root1 = root1;
        this.root2 = root2;
        this.db1 = db1;
        this.db2 = db2;

        db2.jdbi.useHandle(handle -> {
            var filesInDB2 = handle.select(Stmt.GET_ALL_METAS).mapToMap().stream();
            filesInDB2.forEach(meta -> {
                var file2 = Simplemeta.ofMap(meta);
                var file1result = db1.getMetaByID(file2.id);

                // 已被刪除的檔案
                if (file1result.isEmpty()) {
                    deleted.add(file2);
                    return;
                }
                var file1 = file1result.get();

                // 更新了內容的檔案
                if (!file1.checksum.equals(file2.checksum)) {
                    overwritten.add(file1);
                    // 如果更新了內容, 則認為它的屬性也已變更, 因此這裡可直接返回。
                    return;
                }

                // 更新了屬性(metadata/json)的檔案
                var num1 = file1.size + file1.like;
                var num2 = file2.size + file2.like;
                var str1 = file1.type+file1.label+file1.notes+file1.ctime+file1.utime;
                var str2 = file2.type+file2.label+file2.notes+file2.ctime+file2.utime;
                if (num1 != num2 || !str1.equals(str2)) {
                    updated.add(file1);
                }
            });
        });

        // 新增的檔案
        db1.jdbi.useHandle(handle -> {
            var filesInDB1 = handle.select(Stmt.GET_ALL_METAS).mapToMap().stream();
            filesInDB1.forEach(meta -> {
                var file1 = Simplemeta.ofMap(meta);
                var file2result = db2.getMetaByID(file1.id);
                if (file2result.isEmpty()) {
                    added.add(file1);
                }
            });
        });
    }

    int count() {
        return deleted.size()+updated.size()+overwritten.size()+added.size();
    }

    void syncOneWay(JTextArea textArea) {
        try {
            syncDeleted(textArea);
            syncUpdated(textArea);
            syncOverwritten(textArea);
            syncAdded(textArea);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void syncDeleted(JTextArea textArea) throws IOException {
        for (var meta : deleted) {
            textArea.append(".");
            var filepath = root2.resolve("files", meta.filename);
            var metapath = root2.resolve("simplemeta", meta.filename+".json");
            Files.deleteIfExists(filepath);
            Files.deleteIfExists(metapath);
            db2.deleteSimplemeta(meta.id);
        }
    }

    private void syncUpdated(JTextArea textArea) throws IOException {
        for (var meta : updated) {
            textArea.append(".");
            overwriteMeta(meta);
            db2.updateSimplemeta(meta);
        }
    }

    private void overwriteMeta(Simplemeta meta) throws IOException {
        var src = root1.resolve("simplemeta", meta.filename+".json");
        var dst = root2.resolve("simplemeta", meta.filename+".json");
        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
    }

    private void overwriteFile(Simplemeta meta) throws IOException {
        var src = root1.resolve("files", meta.filename);
        var dst = root2.resolve("files", meta.filename);
        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
    }

    private void syncOverwritten(JTextArea textArea) throws IOException {
        for (var meta : overwritten) {
            System.out.printf("%s %s%n", meta.id, meta.filename);
            textArea.append(".");
            overwriteMeta(meta);
            overwriteFile(meta);
            db2.updateSimplemeta(meta);
        }
    }

    private void syncAdded(JTextArea textArea) throws IOException {
        for (var meta : added) {
            textArea.append(".");
            overwriteMeta(meta);
            overwriteFile(meta);
            db2.insertSimplemeta(meta);
        }
    }
}
