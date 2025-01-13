package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

public class WuliuAdd implements Runnable{
    static final int pictureSizeLimit = 300;
    static ProjectInfo projInfo;
    private static final int recentLabelsLimit = 10;

    private List<String> labels;
    private JList<String> labelList;

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
        DB db = new DB(MyUtil.WULIU_J_DB);

        var file = MyUtil.getOneFileFrom(MyUtil.INPUT_PATH);
        var filename = file.getFileName().toString();
        var filetype = Simplemeta.typeByFilename(filename);
        var isImage = Simplemeta.isImage(filetype);

        var frame = new JFrame("Wuliu Add");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var pane_1 = new JPanel();
        var pane_2 = new JPanel();
        pane_2.setLayout(new BoxLayout(pane_2, BoxLayout.PAGE_AXIS));
        pane_2.setBorder(new EmptyBorder(10, 0, 0, 10));

        JLabel fileArea;
        if (isImage) {
            try {
                var image = MyUtil.getImageCropLimit(file.toFile(), pictureSizeLimit);
                var pic = new ImageIcon(image);
                fileArea = new JLabel(pic);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            fileArea = new JLabel(filename);
        }
        pane_1.add(fileArea);

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
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    class DoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int i = labelList.locationToIndex(event.getPoint());
                System.out.println(labels.get(i));
            }
        }
    }
}

