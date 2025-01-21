package wuliu_j.tools;

import com.fasterxml.jackson.jr.ob.JSON;
import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WuliuAdd implements Runnable{
    private static DB db;
    private static final int pictureSizeLimit = 300;
    private static final int recentLabelsLimit = 10;

    private JFrame frame;
    private List<String> labels;
    private JList<String> labelList;
    private JLabel previewArea;
    private JTextField filenameText;
    private JTextField labelText;
    private JTextField notesText;

    private Path currentFile;
    private Simplemeta currentMeta;

    public static void main(String[] args) throws IOException {
        initAndCheck();
        SwingUtilities.invokeLater(new WuliuAdd());
    }

    static void initAndCheck() throws IOException {
        var projInfo = MyUtil.initCheck();
        MyUtil.checkNotBackup(projInfo);
        db = new DB(MyUtil.WULIU_J_DB);
    }

    private void reset() {
        loadCurrentFile();
        filenameText.setText(currentFile.getFileName().toString());
        resetPreviewArea();
        labels = db.getRecentLabels(recentLabelsLimit);
        labelList.setListData(labels.toArray(new String[0]));
    }

    private void resetPreviewArea() {
        var filename = currentFile.getFileName().toString();
        var filetype = Simplemeta.typeByFilename(filename);
        var isImage = Simplemeta.isImage(filetype);
        if (isImage) {
            try {
                var image = MyUtil.getImageCropLimit(currentFile.toFile(), pictureSizeLimit);
                previewArea.setIcon(new ImageIcon(image));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            previewArea.setIcon(new ImageIcon());
        }
    }

    private void loadCurrentFile() {
        currentFile = MyUtil.getOneFileFrom(MyUtil.INPUT_PATH);
        currentMeta = new Simplemeta(currentFile);
        fileMustNotExist(currentMeta);
    }

    private void fileMustNotExist(Simplemeta meta) {
        MyUtil.pathMustNotExists(MyUtil.FILES_PATH.resolve(meta.filename));
        MyUtil.pathMustNotExists(MyUtil.getSimplemetaPath(meta.filename));
        var opt = db.getMetaByChecksum(meta.checksum);
        if (opt.isPresent()) {
            var conflict = opt.get();
            System.out.println("已存在相同內容的檔案");
            System.out.println("input: " + meta.filename);
            System.out.println("files: " + conflict.filename);
            System.exit(0);
        }
    }

    @Override
    public void run() {
        loadCurrentFile();

        frame = new JFrame("Wuliu Add");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var pane_1 = new JPanel();
        var pane_2 = new JPanel();
        pane_1.setBorder(new EmptyBorder(10, 10, 10, 10));
        pane_2.setLayout(new BoxLayout(pane_2, BoxLayout.PAGE_AXIS));
        pane_2.setBorder(new EmptyBorder(10, 10, 10, 10));

        previewArea = new JLabel();
        resetPreviewArea();
        previewArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        pane_1.add(previewArea);

        var filenamePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var filenameLabel = new JLabel("Filename:");
        filenameText = new JTextField(20);
        filenameText.setFont(MyUtil.FONT_18);
        filenamePane.add(filenameLabel);
        filenamePane.add(filenameText);
        pane_1.add(filenamePane);
        filenameText.setText(currentFile.getFileName().toString());
        filenameText.setEditable(false);

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

        var clearBtn = new JButton("Clear");
        clearBtn.addActionListener(_ -> {
            notesText.setText("");
            labelText.setText("");
            labelText.requestFocusInWindow();
        });
        pane_1.add(clearBtn);

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
        labelList.setFixedCellWidth(250);
        pane_2.add(labelList);

        // 两个 alignment 必须同时设置才有效
        // https://docs.oracle.com/javase/tutorial/uiswing/layout/box.html
        recentLabelsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelList.setAlignmentX(Component.LEFT_ALIGNMENT);

        frame.add(BorderLayout.CENTER, pane_1);
        frame.add(BorderLayout.EAST, pane_2);
        frame.setSize(700, 500);
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
            // 添加档案时不需要检查磁盘空间，
            // 因为 input 资料夹与 files 资料夹在同一个磁盘分区内。
            try {
                var meta = currentMeta;
                meta.label = labelText.getText();
                meta.notes = notesText.getText();
                var metaPath = MyUtil.getSimplemetaPath(meta.filename);
                System.out.println("Create => " + metaPath);
                JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT)
                        .write(meta.toMap(), metaPath.toFile());
                var src = MyUtil.INPUT_PATH.resolve(meta.filename);
                var dst = MyUtil.FILES_PATH.resolve(meta.filename);
                System.out.println("Add => " + dst);
                Files.move(src, dst);
                db.insertSimplemeta(meta.toMap());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            JOptionPane.showMessageDialog(frame, "添加檔案成功！");
            reset();
        }
    }
}
