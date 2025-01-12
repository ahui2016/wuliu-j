package wuliu_j.tools;

import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

public class WuliuAdd implements Runnable{
    static final int pictureSizeLimit = 300;
    static ProjectInfo projInfo;

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
        var file = getOneFileFromInput();
        var filename = file.getFileName().toString();
        var filetype = Simplemeta.typeByFilename(filename);
        var isImage = Simplemeta.isImage(filetype);

        var frame = new JFrame("Wuliu Add");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var panel_1 = new JPanel();
        var panel_2 = new JPanel();

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
        panel_1.add(fileArea);

        frame.add(BorderLayout.CENTER, panel_1);
        frame.add(BorderLayout.EAST, panel_2);
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    Path getOneFileFromInput() {
        return MyUtil.getOneFileFrom(MyUtil.INPUT_PATH);
    }
}
