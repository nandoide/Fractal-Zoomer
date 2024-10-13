package fractalzoomer.gui;

import fractalzoomer.main.MainWindow;
import fractalzoomer.main.app_settings.PlaneInfluenceSettings;
import fractalzoomer.main.app_settings.Settings;
import fractalzoomer.parser.ParserException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UserPlaneInfluenceDialog extends JDialog {

    private MainWindow ptra;
    private JOptionPane optionPane;

    public UserPlaneInfluenceDialog(MainWindow ptr, Settings s, int oldSelected, PlaneInfluenceSettings ips, JRadioButtonMenuItem[] plane_influences) {

        super(ptr);

        ptra = ptr;

        setTitle("User Plane Influence");
        setModal(true);
        setIconImage(MainWindow.getIcon("mandel2.png").getImage());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(495, 190));
        //tabbedPane.setPreferredSize(new Dimension(550, 210));
        tabbedPane.setFocusable(false);

        JTextField field_formula = new JTextField(MainWindow.runsOnWindows && !MainWindow.useCustomLaf ? 50 : 45);//48
        field_formula.setText(ips.userFormulaPlaneInfluence);

        JPanel formula_panel = new JPanel();
        formula_panel.setLayout(new FlowLayout());

        formula_panel.add(new JLabel("c ="));
        formula_panel.add(field_formula);

        tabbedPane.addTab("Normal", formula_panel);

        JPanel formula_panel_cond1 = new JPanel();
        formula_panel_cond1.setLayout(new GridLayout(2, 2));

        JTextField field_condition = new JTextField(24);
        field_condition.setText(ips.user_plane_influence_conditions[0]);

        JTextField field_condition2 = new JTextField(24);
        field_condition2.setText(ips.user_plane_influence_conditions[1]);

        formula_panel_cond1.add(new JLabel("Left operand:", SwingConstants.HORIZONTAL));
        formula_panel_cond1.add(new JLabel("Right operand:", SwingConstants.HORIZONTAL));
        formula_panel_cond1.add(field_condition);
        formula_panel_cond1.add(field_condition2);

        JTextField field_formula_cond1 = new JTextField(MainWindow.runsOnWindows && !MainWindow.useCustomLaf ? 45 : 40);//35
        field_formula_cond1.setText(ips.user_plane_influence_condition_formula[0]);

        JTextField field_formula_cond2 = new JTextField(MainWindow.runsOnWindows && !MainWindow.useCustomLaf ? 45 : 40);//35
        field_formula_cond2.setText(ips.user_plane_influence_condition_formula[1]);

        JTextField field_formula_cond3 = new JTextField(MainWindow.runsOnWindows && !MainWindow.useCustomLaf ? 45 : 40);//35
        field_formula_cond3.setText(ips.user_plane_influence_condition_formula[2]);

        JPanel formula_panel_cond11 = new JPanel();

        formula_panel_cond11.add(new JLabel("left > right, c ="));
        formula_panel_cond11.add(field_formula_cond1);

        JPanel formula_panel_cond12 = new JPanel();

        formula_panel_cond12.add(new JLabel("left < right, c ="));
        formula_panel_cond12.add(field_formula_cond2);

        JPanel formula_panel_cond13 = new JPanel();

        formula_panel_cond13.add(new JLabel("left = right, c ="));
        formula_panel_cond13.add(field_formula_cond3);

        JPanel panel_cond = new JPanel();
        panel_cond.setLayout(new FlowLayout());

        panel_cond.add(formula_panel_cond1);
        panel_cond.add(formula_panel_cond11);
        panel_cond.add(formula_panel_cond12);
        panel_cond.add(formula_panel_cond13);

        tabbedPane.addTab("Conditional", panel_cond);

        Object[] labels33 = ptra.createUserFormulaLabels("z, c, s, c0, p, pp, n, maxn, center, size, sizei, width, height, v1 - v30, point");

        tabbedPane.setSelectedIndex(ips.user_plane_influence_algorithm);

        Object[] message3 = {
                labels33,
                " ",
                "Set the plane influence formula, which is applied on every iteration.",
                tabbedPane,};

        optionPane = new JOptionPane(message3, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, null);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
            }
        });

        optionPane.addPropertyChangeListener(
                e -> {
                    String prop = e.getPropertyName();

                    if (isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {

                        Object value = optionPane.getValue();

                        if (value == JOptionPane.UNINITIALIZED_VALUE) {
                            //ignore reset
                            return;
                        }

                        //Reset the JOptionPane's value.
                        //If you don't do this, then if the user
                        //presses the same button next time, no
                        //property change event will be fired.
                        optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

                        if ((Integer) value == JOptionPane.CANCEL_OPTION || (Integer) value == JOptionPane.NO_OPTION || (Integer) value == JOptionPane.CLOSED_OPTION) {
                            plane_influences[oldSelected].setSelected(true);
                            ips.influencePlane = oldSelected;
                            dispose();
                            return;
                        }

                        try {
                            if (tabbedPane.getSelectedIndex() == 0) {
                                s.parser.parse(field_formula.getText());

                                if (s.parser.foundBail() || s.parser.foundCbail() || s.parser.foundR() || s.parser.foundStat() || s.parser.foundTrap()) {
                                    JOptionPane.showMessageDialog(ptra, "The variables: bail, cbail, r, stat, trap cannot be used in the z formula.", "Error!", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            } else {
                                s.parser.parse(field_condition.getText());

                                if (s.parser.foundBail() || s.parser.foundCbail() || s.parser.foundR()  || s.parser.foundStat() || s.parser.foundTrap()) {
                                    JOptionPane.showMessageDialog(ptra, "The variables: bail, cbail, r, stat, trap cannot be used in the left condition formula.", "Error!", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                s.parser.parse(field_condition2.getText());

                                if (s.parser.foundBail() || s.parser.foundCbail() || s.parser.foundR() || s.parser.foundStat() || s.parser.foundTrap()) {
                                    JOptionPane.showMessageDialog(ptra, "The variables: bail, cbail, r, stat, trap cannot be used in the right condition formula.", "Error!", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                s.parser.parse(field_formula_cond1.getText());

                                if (s.parser.foundBail() || s.parser.foundCbail() || s.parser.foundR() || s.parser.foundStat() || s.parser.foundTrap()) {
                                    JOptionPane.showMessageDialog(ptra, "The variables: bail, cbail, r, stat, trap cannot be used in the left > right z formula.", "Error!", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                s.parser.parse(field_formula_cond2.getText());

                                if (s.parser.foundBail() || s.parser.foundCbail() || s.parser.foundR() || s.parser.foundStat() || s.parser.foundTrap()) {
                                    JOptionPane.showMessageDialog(ptra, "The variables: bail, cbail, r, stat, trap cannot be used in the left < right z formula.", "Error!", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                s.parser.parse(field_formula_cond3.getText());

                                if (s.parser.foundBail() || s.parser.foundCbail() || s.parser.foundR() || s.parser.foundStat() || s.parser.foundTrap()) {
                                    JOptionPane.showMessageDialog(ptra, "The variables: bail, cbail, r, stat, trap cannot be used in the left = right z formula.", "Error!", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }

                            ips.user_plane_influence_algorithm = tabbedPane.getSelectedIndex();

                            if (ips.user_plane_influence_algorithm == 0) {
                                ips.userFormulaPlaneInfluence = field_formula.getText();
                            } else {
                                ips.user_plane_influence_conditions[0] = field_condition.getText();
                                ips.user_plane_influence_conditions[1] = field_condition2.getText();
                                ips.user_plane_influence_condition_formula[0] = field_formula_cond1.getText();
                                ips.user_plane_influence_condition_formula[1] = field_formula_cond2.getText();
                                ips.user_plane_influence_condition_formula[2] = field_formula_cond3.getText();
                            }

                            ptra.defaultFractalSettings(true, false);
                        } catch (ParserException ex) {
                            JOptionPane.showMessageDialog(ptra, ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        dispose();
                    }
                });

        //Make this dialog display it.
        setContentPane(optionPane);

        pack();

        setResizable(false);
        setLocation((int) (ptra.getLocation().getX() + ptra.getSize().getWidth() / 2) - (getWidth() / 2), (int) (ptra.getLocation().getY() + ptra.getSize().getHeight() / 2) - (getHeight() / 2));
        setVisible(true);

    }
}
