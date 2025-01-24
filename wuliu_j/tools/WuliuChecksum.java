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
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class WuliuChecksum implements Runnable {
    private static final int textCols = 35;

    private static DB db;
    private static DB db2;
    private static ProjectInfo projInfo;
    private static Path projRoot;
    private static Path projRoot2;
    private static List<String> projects2;
    private static List<String> idsNeedCheck;
    private static List<String> proj_1_damagedIds;

    private JFrame frame;
    private JPanel pane_1;
    private JList<String> projList;
    private JList<String> projList2;
    private JTextField currentProjTF;
    private JTextField project2TF;
    private JTextArea msgArea;
    private JTextArea msgArea2;
    private JButton renewBtn;
    private JButton checkBtn;
    private JButton gotoPane2Btn;

    public static void main(String[] args) throws IOException {
        projInfo = MyUtil.initCheck();
        SwingUtilities.invokeLater(new WuliuChecksum());
    }

    @Override
    public void run() {
        createGUI();
        projList.setListData(projInfo.Projects.toArray(new String[0]));
        projList.addMouseListener(new DoubleClickAdapter());
        renewBtn.addActionListener(new RenewBtnListener());
        checkBtn.addActionListener(new CheckBtnListener());
    }

    private void createGUI() {
        frame = new JFrame("Wuliu Checksum");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pane_1 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        projList = new JList<>();
        projList.setFont(MyUtil.FONT_16);
        projList.setFixedCellWidth(450);
        var label_1 = new JLabel("請選擇專案(按兩下):");
        pane_1.add(label_1);
        pane_1.add(projList);

        currentProjTF = new JTextField(textCols);
        currentProjTF.setFont(MyUtil.FONT_16);
        currentProjTF.setEditable(false);
        var label_2 = new JLabel("已選擇專案:");
        pane_1.add(label_2);
        pane_1.add(currentProjTF);

        msgArea = new JTextArea(15, textCols);
        msgArea.setFont(MyUtil.FONT_16);
        pane_1.add(msgArea);

        renewBtn = new JButton("Renew");
        var spacer = new JLabel(" ");
        spacer.setBorder(new EmptyBorder(0, 100, 0, 100));
        checkBtn = new JButton("Check");
        pane_1.add(renewBtn);
        pane_1.add(spacer);
        pane_1.add(checkBtn);

        gotoPane2Btn = new JButton("Repair");
        gotoPane2Btn.addActionListener(_ -> gotoPane2());
        pane_1.add(gotoPane2Btn);
        // gotoPane2Btn.setVisible(false);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    private void gotoPane2() {
        pane_1.setVisible(false);

        var pane_2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var label_1 = new JLabel("專案 1");
        pane_2.add(label_1);
        pane_2.add(currentProjTF);
        frame.add(pane_2);

        projects2 = projInfo.Projects.stream().filter(p ->
                !projRoot.equals(Path.of(p))).toList();

        var label_2 = new JLabel("請按兩下選擇另一個專案:");
        projList2 = new JList<>(projects2.toArray(new String[0]));
        projList2.setFont(MyUtil.FONT_16);
        projList2.setFixedCellWidth(450);
        projList2.addMouseListener(new DoubleClickAdapter2());
        pane_2.add(label_2);
        pane_2.add(projList2);

        var label_3 = new JLabel("專案 2");
        project2TF = new JTextField(textCols);
        project2TF.setFont(MyUtil.FONT_16);
        project2TF.setEditable(false);
        pane_2.add(label_3);
        pane_2.add(project2TF);

        msgArea2 = new JTextArea(10, textCols);
        msgArea2.setFont(MyUtil.FONT_16);
        pane_2.add(msgArea2);

        var repairBtn = new JButton("Repair");
        repairBtn.addActionListener(new RepairBtnListener());
        pane_2.add(repairBtn);
    }

    private List<String> getIdsNeedCheck(int intervalDay) {
        var interval = intervalDay * MyUtil.Day;
        var needCheckUnix = Instant.now().getEpochSecond() - interval;
        var needCheckDate = LocalDateTime.ofEpochSecond(needCheckUnix, 0, ZoneOffset.of("+08:00"));
        var datetime = needCheckDate.format(MyUtil.RFC3339);
        return db.getIdsNeedCheck(datetime);
    }

    private void printInfo(boolean clear) {
        idsNeedCheck = getIdsNeedCheck(projInfo.CheckInterval);
        var damagedIds = db.getDamagedIds();
        if (clear) msgArea.setText("");
        msgArea.append("檔案總數: %d%n".formatted(db.countSimplemeta()));
        msgArea.append("檢查周期: %d 天%n".formatted(projInfo.CheckInterval));
        msgArea.append("待檢查檔案數: %d%n".formatted(idsNeedCheck.size()));
        msgArea.append("單次檢查上限: %d MB%n".formatted(projInfo.CheckSizeLimit));
        msgArea.append("已損壞檔案數: %d%n".formatted(damagedIds.size()));
        if (damagedIds.size() > 0) {
            msgArea.append("已損壞檔案ID:\n");
            msgArea.append(String.join(", ", damagedIds));
        }
        // gotoPane2Btn.setVisible(damagedFiles.size() > 0);
    }

    /**
     * 檢查檔案是否損壞, 若未損壞則返回 0, 若已損壞則返回大於零的整數。
     */
    public int checkFileIsDamaged(Path projRoot, Simplemeta meta) {
        var filePath = projRoot.resolve("files", meta.filename);
        var checksumNow = Simplemeta.getFileSHA1(filePath);
        var ok = meta.checksum.equals(checksumNow);
        return ok ? 0 : 1;
    }

    class DoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int i = projList.locationToIndex(event.getPoint());
                projRoot = Path.of(projInfo.Projects.get(i));
                db = new DB(projRoot.resolve(MyUtil.WULIU_J_DB).toString());
                currentProjTF.setText(projRoot.toAbsolutePath().normalize().toString());
                printInfo(true);
            }
        }
    }

    class DoubleClickAdapter2 extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int i = projList2.locationToIndex(event.getPoint());
                projRoot2 = Path.of(projects2.get(i));
                db2 = new DB(projRoot.resolve(MyUtil.WULIU_J_DB).toString());
                project2TF.setText(projRoot2.toAbsolutePath().normalize().toString());
                proj_1_damagedIds = db.getDamagedIds();
                msgArea2.setText("在專案 1 中發現 %d 個己損壞的檔案%n".formatted(proj_1_damagedIds.size()));
                msgArea2.append("請點擊 Repair 按鈕開始修復\n");
            }
        }
    }

    class RepairBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (proj_1_damagedIds == null) {
                JOptionPane.showMessageDialog(frame, "請先選擇專案2");
                return;
            }
            if (proj_1_damagedIds.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "沒有需要修復的檔案");
                return;
            }
            msgArea2.append("\n嘗試從專案 2 中尋找完整的檔案...\n");
            for (var fileID : proj_1_damagedIds) {
                fixFile(fileID);
            }
            msgArea2.append("修復結束, 結果如上所示。");
        }

        private void fixFile(String fileID) {
            var now = MyUtil.timeNowRFC3339();
            var file1 = db.getMetaByID(fileID).orElseThrow();
            var f2 = db2.getMetaByID(fileID);
            msgArea2.append("%n%s %s%n".formatted(fileID, file1.filename));
            if (f2.isEmpty()) {
                msgArea2.append("在專案 2 中找不到同名檔案\n");
                return;
            }
            var file2 = f2.get();
            if (!file1.utime.equals(file2.utime) || !file1.checksum.equals(file2.checksum)) {
                msgArea2.append("專案 2 中的同名檔案不可用 (utime或checksum不一致)\n");
                return;
            }
            var filepath1 = projRoot.resolve("files", file1.filename);
            var filepath2 = projRoot2.resolve("files", file2.filename);
            // 這裡不訪問 db2, 而是重新計算檔案 checksum, 是為了確保檔案可靠並簡化代碼。
            var checksumNow = Simplemeta.getFileSHA1(filepath2);
            if (!file2.checksum.equals(checksumNow)) {
                msgArea2.append("專案 2 中的同名檔案也已損壞\n");
                return;
            }
            msgArea2.append("發現有用檔案, 執行自動修復");
            try {
                Files.copy(filepath2, filepath1, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            db.updateCheckedDamaged(file1.id, now, 0);
        }
    }

    class CheckBtnListener implements ActionListener {
        private static final long MB = 1 << 20;
        @Override
        public void actionPerformed(ActionEvent e) {
            if (idsNeedCheck == null) {
                JOptionPane.showMessageDialog(frame, "請先選擇專案");
                return;
            }
            if (idsNeedCheck.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "沒有需要檢查的檔案");
                return;
            }
            var now = MyUtil.timeNowRFC3339();
            int damagedN = 0;
            int checkN = 0;
            long checkSizeTotal = 0L;
            long checkSizeLimit = projInfo.CheckSizeLimit * MB;
            msgArea.append("\n\n開始檢查\n");
            for (var fileID : idsNeedCheck) {
                msgArea.append(".");
                var meta = db.getMetaByID(fileID).orElseThrow();
                var damaged = checkFileIsDamaged(projRoot, meta);
                if (damaged > 0) damagedN++;
                db.updateCheckedDamaged(meta.id, now, damaged);
                checkN++;
                checkSizeTotal += meta.size;
                if (checkN > 0 && checkSizeTotal > checkSizeLimit) {
                    break;
                }
            }
            msgArea.append("\n本次檢查檔案體積合計: " + MyUtil.fileSizeToString(checkSizeTotal));
            msgArea.append("\n本次檢查檔案數量: " + checkN);
            msgArea.append("\n  其中己損壞檔案: " + damagedN);
            gotoPane2Btn.setVisible(damagedN > 0);
            idsNeedCheck = getIdsNeedCheck(projInfo.CheckInterval);
            msgArea.append("\n剩餘待檢查檔案: " + idsNeedCheck.size());
        }
    }

    class RenewBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var msg = "清空檔案檢查結果, 並重建「待檢查檔案」列表";
            var confirm = JOptionPane.showConfirmDialog(frame, msg);
            if (confirm == JOptionPane.YES_OPTION) {
                msgArea.append("\n\n");
                msgArea.append(msg);
                db.renewChecked();
                msgArea.append("\n");
                msgArea.append("完成。\n\n");
                printInfo(false);
            }
        }
    }
}
