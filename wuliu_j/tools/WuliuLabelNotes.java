package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

public class WuliuLabelNotes implements Runnable {
    private static DB db;
    private static ProjectInfo projInfo;
    private static final int LIST_HEIGHT = 550;
    private static final int RESULT_AMOUNT_LIMIT = 500;

    private JFrame frame;
    private List<String> allLabels;
    private JList<String> labelList;
    private List<String> allNotes;
    private JList<String> notesList;
    private JTextField textField;

    public static void main(String[] args) throws IOException {
        initAndCheck();
        SwingUtilities.invokeLater(new WuliuLabelNotes());
    }

    static void initAndCheck() throws IOException {
        projInfo = MyUtil.initCheck();
        // MyUtil.checkNotBackup(projInfo);
        db = new DB(MyUtil.WULIU_J_DB);
    }

    @Override
    public void run() {
        createGUI();
        allLabels = db.getRecentLabels(RESULT_AMOUNT_LIMIT);
        labelList.setListData(allLabels.toArray(new String[0]));
        labelList.addMouseListener(new DoubleClickAdapter());
        allNotes = db.getRecentNotes(RESULT_AMOUNT_LIMIT);
        notesList.setListData(allNotes.toArray(new String[0]));
        notesList.addMouseListener(new DoubleClickAdapter());
    }

    private void createGUI() {
        final int listWidth = 400;
        final int listHeight = 450;
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        frame = new JFrame("Wuliu Labels and Notes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List.of("OptionPane.messageFont", "TextField.font", "Label.font", "List.font")
                .forEach(k -> UIManager.put(k, MyUtil.FONT_18));

        var pane0 = new JPanel();
        pane0.setLayout(new BoxLayout(pane0, BoxLayout.PAGE_AXIS));
        var paneUp = new JPanel(new GridLayout(1, 2, 10, 10));
        var paneLeft = new JPanel(new FlowLayout());
        paneLeft.add(new JLabel("Labels"));
        labelList = new JList<>();
        var labelListScroll = makeScrollPane(labelList, listWidth, listHeight);
        paneLeft.add(labelListScroll);
        paneUp.add(paneLeft);
        var paneRight = new JPanel(new FlowLayout());
        paneRight.add(new JLabel("Notes"));
        notesList = new JList<>();
        var notesListScroll = makeScrollPane(notesList, listWidth, listHeight);
        paneRight.add(notesListScroll);
        paneUp.add(paneRight);
        pane0.add(paneUp);
        var paneDown = new JPanel(new FlowLayout());
        textField = new JTextField(20);
        var copyBtn = new JButton("Copy");
        copyBtn.addActionListener(_ -> {
            var text = textField.getText().strip();
            try {
                clipboard.setContents(new StringSelection(text), null);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(frame, "已複製到剪貼板：\n" + text);
        });
        paneDown.add(textField);
        paneDown.add(copyBtn);
        pane0.add(paneDown);
        frame.add(pane0, BorderLayout.CENTER);

        frame.setSize(900, 650);
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }

    private JScrollPane makeScrollPane(JList<String> list, int listWidth, int height) {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellWidth(listWidth);
        var scrollPane = new JScrollPane(
                list,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(listWidth+10, height));
        return scrollPane;
    }

    class DoubleClickAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                String text;
                var list = event.getComponent();
                if (list == labelList) {
                    text = labelList.getSelectedValue();
                } else if (list == notesList) {
                    text = notesList.getSelectedValue();
                } else {
                    text = "";
                }
                textField.setText(text);
            }
        }
    }
}
