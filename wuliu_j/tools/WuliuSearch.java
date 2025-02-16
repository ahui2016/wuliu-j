package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.Simplemeta;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class WuliuSearch implements Runnable{
    private static DB db;
    private static final int RESULT_LIST_HEIGHT = 550;
    private static final Integer DEFAULT_RESULT_LIMIT = 23;

    private JFrame frame;
    private JCheckBox cBoxID;
    private JCheckBox cBoxFilename;
    private JCheckBox cBoxLabel;
    private JCheckBox cBoxNotes;
    private List<String> radioButtons = List.of("like", "size", "ctime", "utime");
    private ButtonGroup btnGroup = new ButtonGroup();
    private JTextField datePrefixTF;
    private JTextField resultLimitTF;
    private JTextField searchTF;
    private JButton searchBtn;

    private List<Simplemeta> result;
    private JList<String> resultList;

    public static void main(String[] args) throws IOException {
        initAndCheck();
        SwingUtilities.invokeLater(new WuliuSearch());
    }

    static void initAndCheck() throws IOException {
        var projInfo = MyUtil.initCheck();
        MyUtil.checkNotBackup(projInfo);
        db = new DB(MyUtil.WULIU_J_DB);
    }

    @Override
    public void run() {
        createGUI();
        loadRecentFiles(DEFAULT_RESULT_LIMIT);
    }

    public void createGUI() {
        frame = new JFrame("Wuliu Search");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var pane_1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var pane_r = new JPanel();
        pane_r.setLayout(new BoxLayout(pane_r, BoxLayout.LINE_AXIS));
        pane_r.setPreferredSize(new Dimension(750, RESULT_LIST_HEIGHT+70));
        var pane_2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pane_2.setPreferredSize(new Dimension(420, RESULT_LIST_HEIGHT+70));
        var pane_3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pane_r.add(pane_2);
        pane_r.add(pane_3);

        // pane_1
        var pane_boxes = new JPanel();
        pane_boxes.setLayout(new BoxLayout(pane_boxes, BoxLayout.PAGE_AXIS));
        cBoxID = new JCheckBox("id");
        cBoxFilename = new JCheckBox("filename");
        cBoxLabel = new JCheckBox("label");
        cBoxLabel.setSelected(true);
        cBoxNotes = new JCheckBox("notes");
        cBoxNotes.setSelected(true);

        List.of(cBoxID, cBoxFilename, cBoxLabel, cBoxNotes).forEach(item -> {
            item.setFont(MyUtil.FONT_18);
            pane_boxes.add(item);
        });
        pane_1.add(pane_boxes);

        pane_1.add(newSeparator(90));

        var pane_radio = new JPanel();
        pane_radio.setLayout(new BoxLayout(pane_radio, BoxLayout.PAGE_AXIS));
        radioButtons.forEach(name -> {
            var rBtn = new JRadioButton(name);
            rBtn.setActionCommand(name);
            rBtn.setFont(MyUtil.FONT_18);
            btnGroup.add(rBtn);
            pane_radio.add(rBtn);
            if (name.equals("utime")) rBtn.setSelected(true);
        });
        pane_1.add(pane_radio);

        pane_1.add(newSeparator(90));

        datePrefixTF = newTextField18(5);
        resultLimitTF = newTextField18(5);
        resultLimitTF.setText(DEFAULT_RESULT_LIMIT.toString());
        pane_1.add(new JLabel("date prefix"));
        pane_1.add(datePrefixTF);
        pane_1.add(new JLabel("result limit"));
        pane_1.add(resultLimitTF);

        // pane_2
        searchTF = newTextField18(22);
        searchBtn = new JButton("search");
        searchBtn.addActionListener(e -> {
            var limitStr = resultLimitTF.getText();
            var limit = Integer.parseInt(limitStr);
            loadRecentFiles(limit);
        });
        pane_2.add(searchTF);
        pane_2.add(searchBtn);

        resultList = new JList<>();
        resultList.setFont(MyUtil.FONT_16);
        resultList.setFixedCellWidth(400);
        var scrollPane = new JScrollPane(
                resultList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(410, RESULT_LIST_HEIGHT));
        pane_2.add(scrollPane);

        // pane_3
        var tempTF = newTextField18(20);
        pane_3.add(tempTF);

        // add panels to the frame
        var pane_top = new JPanel();
        pane_top.add(Box.createRigidArea(new Dimension(10, 10)));
        frame.add(pane_top, BorderLayout.NORTH);
        frame.add(pane_1, BorderLayout.CENTER);
        frame.add(pane_r, BorderLayout.EAST);
        frame.setSize(900, RESULT_LIST_HEIGHT+120);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    private void loadRecentFiles(int limit) {
        result = db.getRecentMetaLimit(limit);
        var idFilenames = metaToStringList(result);
        resultList.setListData(idFilenames.toArray(new String[0]));
    }

    private List<String> metaToStringList(List<Simplemeta> metaList) {
        var heart = "❤️";
        return metaList.stream().map(file -> "%s[%s] %s".formatted(
                heart.repeat(file.like), file.id, file.filename)).toList();
    }

    private JSeparator newSeparator(int width) {
        var sep = new JSeparator();
        sep.setPreferredSize(new Dimension(width, 2));
        return sep;
    }

    private JTextField newTextField18(int columns) {
        var tf = new JTextField(columns);
        tf.setFont(MyUtil.FONT_18);
        return tf;
    }
}
