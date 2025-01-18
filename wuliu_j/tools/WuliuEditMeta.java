package wuliu_j.tools;

import wuliu_j.common.MyUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class WuliuEditMeta implements Runnable {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new WuliuEditMeta());
    }

    @Override
    public void run() {
        var frame = new JFrame("Wuliu Edit Meta");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var pane_1 = new JPanel();
        var pane_2 = new JPanel();
        pane_2.setPreferredSize(new Dimension(400, 500));

        var filenamePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var filenameLabel = new JLabel("Filename:");
        var filenameTF = new JTextField(20);
        filenameTF.setFont(MyUtil.FONT_18);
        filenamePane.add(filenameLabel);
        filenamePane.add(filenameTF);
        pane_1.add(filenamePane);

        var searchFilenameBtn = new JButton("Search");
        pane_1.add(searchFilenameBtn);

        var fileList = new JList<>();
        fileList.setFont(MyUtil.FONT_16);
        pane_1.add(fileList);

        var fileIdPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var fileIdLabel = new JLabel("ID:");
        var fileIdTF = new JTextField(10);
        fileIdTF.setFont(MyUtil.FONT_18);
        var searchIdBtn = new JButton("Search");
        fileIdPane.add(fileIdLabel);
        fileIdPane.add(fileIdTF);
        fileIdPane.add(searchIdBtn);
        pane_2.add(fileIdPane);

        var readonlyIdTF = new JTextField("id");
        var readonlyFilenameTF = new JTextField("filename");
        var readonlySizeTF = new JTextField("size");
        var likeTF = new JTextField("like");
        var labelTF = new JTextField("label");
        var notesTF = new JTextField("notes");
        var ctimeTF = new JTextField("ctime");
        var utimeTF = new JTextField("utime");
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
        frame.setSize(800, 550);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);

        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                filenameTF.requestFocusInWindow();
            }
        });
    }
}
