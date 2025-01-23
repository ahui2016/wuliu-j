package wuliu_j.tools;

import com.fasterxml.jackson.jr.ob.JSON;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class WuliuChecksum implements Runnable {
    private static DB db;
    private static ProjectInfo projInfo;
    private static Path projRoot;
    private static List<String> idsNeedCheck;

    private JFrame frame;
    private JList<String> projList;
    private JTextField currentProjTF;
    private JTextArea msgArea;
    private JButton renewBtn;
    private JButton checkBtn;

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

    public void createGUI() {
        frame = new JFrame("Wuliu Checksum");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var pane_1 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        projList = new JList<>();
        projList.setFont(MyUtil.FONT_18);
        projList.setFixedCellWidth(480);
        var label_1 = new JLabel("請選擇專案(按兩下):");
        pane_1.add(label_1);
        pane_1.add(projList);

        currentProjTF = new JTextField(35);
        currentProjTF.setFont(MyUtil.FONT_16);
        currentProjTF.setEditable(false);
        var label_2 = new JLabel("已選擇專案:");
        pane_1.add(label_2);
        pane_1.add(currentProjTF);

        msgArea = new JTextArea(15, 35);
        msgArea.setFont(MyUtil.FONT_16);
        pane_1.add(msgArea);

        renewBtn = new JButton("Renew");
        var spacer = new JLabel(" ");
        spacer.setBorder(new EmptyBorder(0, 100, 0, 100));
        checkBtn = new JButton("Check");
        pane_1.add(renewBtn);
        pane_1.add(spacer);
        pane_1.add(checkBtn);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
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
        var damagedFiles = db.getDamagedIds();
        if (clear) msgArea.setText("");
        msgArea.append("檔案總數: %d%n".formatted(db.countSimplemeta()));
        msgArea.append("檢查周期: %d 天%n".formatted(projInfo.CheckInterval));
        msgArea.append("待檢查檔案數: %d%n".formatted(idsNeedCheck.size()));
        msgArea.append("單次檢查上限: %d MB%n".formatted(projInfo.CheckSizeLimit));
        msgArea.append("已損壞檔案數: %d%n".formatted(damagedFiles.size()));
        if (damagedFiles.size() > 0) {
            msgArea.append("已損壞檔案ID:\n");
            msgArea.append(String.join(", ", damagedFiles));
        }
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
