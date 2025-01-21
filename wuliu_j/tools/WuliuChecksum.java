package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class WuliuChecksum implements Runnable {
    private static DB db;

    private JFrame frame;
    private JList<String> projList;
    private JTextField currentProjTF;
    private JTextArea msgArea;
    private JButton checkBtn;

    public static void main(String[] args) throws IOException {
        initAndCheck();
        SwingUtilities.invokeLater(new WuliuChecksum());
    }

    static void initAndCheck() throws IOException {
        var projInfo = MyUtil.initCheck();
        // MyUtil.checkNotBackup(projInfo);
        db = new DB(MyUtil.WULIU_J_DB);
    }

    @Override
    public void run() {
        createGUI();
    }

    public void createGUI() {
        frame = new JFrame("Wuliu Checksum");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var pane_1 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        projList = new JList<>();
        projList.setFont(MyUtil.FONT_18);
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

        checkBtn = new JButton("Check");
        pane_1.add(checkBtn);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }
}
