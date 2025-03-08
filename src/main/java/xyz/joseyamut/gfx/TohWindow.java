package xyz.joseyamut.gfx;

import lombok.extern.slf4j.Slf4j;
import xyz.joseyamut.util.FixedStack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.util.Arrays;

@Slf4j
public class TohWindow extends JFrame {

    private JMenuItem instructionsItem;
    private JMenuItem restartGameItem;
    private JMenuItem exitItem;
    private JPanel stagePanel;

    private Stage stage;
    private int stackSize;

    public void launch() {
        // Window particulars
        stackSize = 4; // initial stack size
        setJMenuBar(menuBar());

        menuItemActions();
        stage = initializeStage();
        decorate();

        setSize(615, 540);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void decorate() {
        Container container = getContentPane();

        stagePanel = new JPanel();
        stagePanel.setLayout(new BorderLayout());
        stagePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        stagePanel.add("Center", stage);
        stagePanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        container.add(stagePanel, BorderLayout.CENTER);

        JLabel gameLabel = new JLabel("Tower of Hanoi", SwingConstants.CENTER);
        gameLabel.setForeground(new Color(255, 126, 64));
        gameLabel.setFont(new Font("Arial", Font.BOLD, 34));
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.CENTER);
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(flowLayout);
        titlePanel.add(gameLabel);
        container.add(titlePanel, BorderLayout.NORTH);
    }

    private Stage initializeStage() {
        FixedStack fillStack = new FixedStack(stackSize);
        for (int i = stackSize; i > 0; i--) {
            fillStack.push(i);
        }

        return new Stage(fillStack);
    }

    private JMenuBar menuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Game");
        instructionsItem = new JMenuItem("Instructions");
        restartGameItem = new JMenuItem("Restart Game");
        exitItem = new JMenuItem("Exit");
        menu.add(instructionsItem);
        menu.add(restartGameItem);
        menu.add(exitItem);
        menuBar.add(menu);

        return  menuBar;
    }

    private void menuItemActions() {
        instructionsItem.addActionListener(e -> instructionsDialog());
        restartGameItem.addActionListener(e -> restartGameDialog());
        exitItem.addActionListener(e -> System.exit(0));
    }

    private void restartGame(int stackSize) {
        this.stackSize = stackSize;

        stagePanel.remove(stage);
        stage = initializeStage();
        stagePanel.add("Center", stage);
        stagePanel.revalidate();
        stagePanel.repaint();

        log.info("Game restarted with Stack Size of {}!", stackSize);
    }

    private void instructionsDialog() {
        String instructions = "\nThe goal is to move the disks from location X to Z." +
                "\n\nEach of the disks vary in size." +
                "\n\nYou cannot place a bigger disk on top of a smaller one." +
                "\n\nThe game ends when all the disks are stacked at location Z.";

        JTextPane jTextPane = new JTextPane();
        jTextPane.setPreferredSize( new Dimension(250, 250));
        jTextPane.setEditable(false);
        jTextPane.setText(instructions);
        StyledDocument styledDocument = jTextPane.getStyledDocument();
        styledDocument.setParagraphAttributes(0, instructions.length(), paragraphStyling(), true);

        JOptionPane.showMessageDialog(this, jTextPane,
                "Instructions",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void restartGameDialog() {
        Integer[] numDisks = new Integer[] {4, 5, 6, 7, 8};

        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.CENTER);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(flowLayout);

        JLabel jLabel = new JLabel("Number of disks?");
        JComboBox<Integer> restartComboBox = new JComboBox<>(numDisks);
        restartComboBox.setSelectedIndex(Arrays.asList(numDisks).indexOf(stackSize));

        jPanel.add(jLabel, FlowLayout.LEFT);
        jPanel.add(restartComboBox, FlowLayout.CENTER);

        JDialog jDialog = new JDialog(this, "Restart Game");
        jDialog.add(jPanel);
        jDialog.setSize(250, 80);
        jDialog.setLocationRelativeTo(this);
        jDialog.setVisible(true);
        jDialog.setModal(true);

        restartComboBox.addActionListener(e -> { // TODO: Check this!
            JComboBox jComboBox = (JComboBox) e.getSource();
            Integer numDisksSelected = (Integer) jComboBox.getSelectedItem();
            log.info("Number of disks selected: {}", numDisksSelected);
            restartGame(numDisksSelected);
            jDialog.dispose();
        });
    }

    private SimpleAttributeSet paragraphStyling() {
        SimpleAttributeSet styling = new SimpleAttributeSet();
        StyleConstants.setAlignment(styling, StyleConstants.ALIGN_JUSTIFIED);
        StyleConstants.setFontFamily(styling, "Arial, Helvetica, sans-serif");
        StyleConstants.setFontSize(styling, 14);
        return styling;
    }

}
