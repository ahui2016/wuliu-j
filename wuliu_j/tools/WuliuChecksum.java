package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class WuliuChecksum implements Runnable {
    private static DB db;
    private static ProjectInfo projInfo;
    private static Path projRoot;

    private JFrame frame;
    private JList<String> projList;
    private JTextField currentProjTF;
    private JTextArea msgArea;
    private JButton dangerBtn;
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

        msgArea = new JTextArea(10, 35);
        msgArea.setFont(MyUtil.FONT_16);
        pane_1.add(msgArea);

        dangerBtn = new JButton("DANGER");
        var spacer = new JLabel(" ");
        spacer.setBorder(new EmptyBorder(0, 100, 0, 100));
        checkBtn = new JButton("Check");
        pane_1.add(dangerBtn);
        pane_1.add(spacer);
        pane_1.add(checkBtn);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    private String getDateNeedCheck(int intervalDay) {
        var interval = intervalDay * MyUtil.Day;
        var needCheckUnix = Instant.now().getEpochSecond() - interval;
        var needCheckDate = LocalDateTime.ofEpochSecond(needCheckUnix, 0, ZoneOffset.of("+08:00"));
        return needCheckDate.format(MyUtil.RFC3339);
    }

    class DoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int i = projList.locationToIndex(event.getPoint());
                projRoot = Path.of(projInfo.Projects.get(i));
                db = new DB(projRoot.resolve(MyUtil.WULIU_J_DB).toString());
                currentProjTF.setText(projRoot.toAbsolutePath().toString());
            }
        }
    }
}
