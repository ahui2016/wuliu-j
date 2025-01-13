package wuliu_j.tools;

import com.fasterxml.jackson.jr.ob.JSON;
import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class WuliuAdd implements Runnable{
    private static ProjectInfo projInfo;
    private static final int pictureSizeLimit = 300;
    private static final int recentLabelsLimit = 10;

    private JFrame frame;
    private List<String> labels;
    private JList<String> labelList;
    private JTextField labelText;
    private JTextField notesText;

    private final DB db = new DB(MyUtil.WULIU_J_DB);
    private Path currentFile;

    public static void main(String[] args) throws IOException {
        check();
        SwingUtilities.invokeLater(new WuliuAdd());
    }

    static void check() throws IOException {
        loadsProjInfo();
        MyUtil.checkNotBackup(projInfo);
    }

    private static void loadsProjInfo() throws IOException {
        projInfo = ProjectInfo.fromJsonFile(MyUtil.PROJ_INFO_PATH);
    }

    public void run() {
        currentFile = MyUtil.getOneFileFrom(MyUtil.INPUT_PATH);
        var filename = currentFile.getFileName().toString();
        var filetype = Simplemeta.typeByFilename(filename);
        var isImage = Simplemeta.isImage(filetype);

        frame = new JFrame("Wuliu Add");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var pane_1 = new JPanel();
        var pane_2 = new JPanel();
        pane_1.setBorder(new EmptyBorder(10, 10, 10, 10));
        pane_2.setLayout(new BoxLayout(pane_2, BoxLayout.PAGE_AXIS));
        pane_2.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel fileArea;
        if (isImage) {
            try {
                var image = MyUtil.getImageCropLimit(currentFile.toFile(), pictureSizeLimit);
                var pic = new ImageIcon(image);
                fileArea = new JLabel(pic);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            fileArea = new JLabel(filename);
        }
        fileArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        pane_1.add(fileArea);

        var labelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var labelLabel = new JLabel("Label:");
        labelText = new JTextField(20);
        labelText.setFont(MyUtil.FONT_18);
        labelPane.add(labelLabel);
        labelPane.add(labelText);
        pane_1.add(labelPane);

        var notesPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var notesLabel = new JLabel("Notes:");
        notesText = new JTextField(20);
        notesText.setFont(MyUtil.FONT_18);
        notesPane.add(notesLabel);
        notesPane.add(notesText);
        pane_1.add(notesPane);

        var submitBtn = new JButton("Submit");
        submitBtn.addActionListener(new SubmitBtnListener());
        pane_1.add(submitBtn);

        JLabel recentLabelsTitle = new JLabel("Recent Labels:");
        // recentLabelsTitle.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        pane_2.add(recentLabelsTitle);

        labels = db.getRecentLabels(recentLabelsLimit);
        labelList = new JList<>(labels.toArray(new String[0]));
        labelList.addMouseListener(new DoubleClickAdapter());
        labelList.setFont(MyUtil.FONT_18);
        pane_2.add(labelList);

        // 两个 alignment 必须同时设置才有效
        // https://docs.oracle.com/javase/tutorial/uiswing/layout/box.html
        recentLabelsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelList.setAlignmentX(Component.LEFT_ALIGNMENT);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.add(BorderLayout.EAST, pane_2);
        frame.setSize(600, 520);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);

        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                labelText.requestFocusInWindow();
            }
        });
    }

    class DoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int i = labelList.locationToIndex(event.getPoint());
                var text = labelText.getText();
                if (!text.isBlank()) {
                    text += "-";
                }
                text += labels.get(i);
                labelText.setText(text);
                labelText.requestFocusInWindow();
            }
        }
    }

    class SubmitBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var meta = new Simplemeta(currentFile);
            meta.label = labelText.getText();
            meta.notes = notesText.getText();
            try {
                var metaJson = JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).asString(meta.toMap());
                System.out.println(metaJson);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            JOptionPane.showMessageDialog(frame, "添加檔案成功!");
            labelText.setText("");
            notesText.setText("");
        }
    }
}
