package wuliu_j.tools;

import wuliu_j.common.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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

    private JFrame frame;
    private JPanel pane_1;

    public static void main(String[] args) throws IOException {
        projStat_1 = new ProjectStatus(Path.of(".").toAbsolutePath().normalize());
        SwingUtilities.invokeLater(new WuliuBackup());
    }

    @Override
    public void run() {
        createGUI();
        var toIndex = projStat_1.projInfo.Projects.size();
        bkProjects = projStat_1.projInfo.Projects.subList(1, toIndex);

        proj1TF.setText(projStat_1.projRoot.toString());
        bkProjList.setListData(bkProjects.toArray(new String[0]));
        bkProjList.addMouseListener(new DoubleClickAdapter());
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

        msgArea = new JTextArea(15, textCols);
        msgArea.setFont(MyUtil.FONT_16);
        pane_1.add(msgArea);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    void printStatus() {
        var totalSize = MyUtil.fileSizeToString(projStat_1.totalSize);
        var lastBackup = projStat_1.projInfo.LastBackupAt.getFirst();
        msgArea.setText("");
        msgArea.append("專案 1    %s%n".formatted(projStat_1.projRoot));
        msgArea.append("檔案數量  %d%n".formatted(projStat_1.totalFiles));
        msgArea.append("體積合計  %s%n".formatted(totalSize));
        msgArea.append("受損檔案  %d%n".formatted(projStat_1.totalDamaged));
        msgArea.append("上次備份  %s%n".formatted(lastBackup));
        msgArea.append("\n");
        totalSize = MyUtil.fileSizeToString(projStat_2.totalSize);
        lastBackup = projStat_1.projInfo.LastBackupAt.get(bkProjIndex);
        msgArea.append("專案 2    %s%n".formatted(projStat_2.projRoot));
        msgArea.append("檔案數量  %d%n".formatted(projStat_2.totalFiles));
        msgArea.append("體積合計  %s%n".formatted(totalSize));
        msgArea.append("受損檔案  %d%n".formatted(projStat_2.totalDamaged));
        msgArea.append("上次備份  %s%n".formatted(lastBackup));
        msgArea.append("\n");
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
                    overwritten.add(file2);
                    // 如果更新了內容, 則認為它的屬性也已變更, 因此這裡可直接返回。
                    return;
                }

                // 更新了屬性(metadata/json)的檔案
                var num1 = file1.size + file1.like;
                var num2 = file2.size + file2.like;
                var str1 = file1.type+file1.label+file1.notes+file1.ctime+file1.utime;
                var str2 = file2.type+file2.label+file2.notes+file2.ctime+file2.utime;
                if (num1 != num2 || !str1.equals(str2)) {
                    updated.add(file2);
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
        for (var meta : updated) {
            textArea.append(".");
            overwriteMeta(meta);
            overwriteFile(meta);
            db2.updateSimplemeta(meta);
        }
    }

    private void syncAdded(JTextArea textArea) throws IOException {
        for (var meta : updated) {
            textArea.append(".");
            overwriteMeta(meta);
            overwriteFile(meta);
            db2.insertSimplemeta(meta);
        }
    }
}
