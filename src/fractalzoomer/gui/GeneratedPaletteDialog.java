
package fractalzoomer.gui;

import fractalzoomer.main.Constants;
import fractalzoomer.main.MainWindow;
import fractalzoomer.main.app_settings.GeneratedPaletteSettings;
import fractalzoomer.main.app_settings.Settings;
import fractalzoomer.utils.Multiwave;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author hrkalona2
 */
public class GeneratedPaletteDialog extends JDialog {

    private MainWindow ptra;
    private JOptionPane optionPane;
    private GeneratedPaletteSettings currentGps;

    public GeneratedPaletteDialog(MainWindow ptr, Settings s, boolean outcoloring) {
        
        super(ptr);

        ptra = ptr;
        currentGps = new GeneratedPaletteSettings(s.gps);

        setTitle("Generated Palette");
        setModal(true);
        setIconImage(MainWindow.getIcon("mandel2.png").getImage());


        JTextField generated_palette_restart_field = new JTextField();
        generated_palette_restart_field.setText("" + (outcoloring ? s.gps.restartGeneratedOutColoringPaletteAt : s.gps.restartGeneratedInColoringPaletteAt));

        final JCheckBox enable_generated_palette = new JCheckBox("Generated Palette");
        enable_generated_palette.setSelected(outcoloring ? s.gps.useGeneratedPaletteOutColoring : s.gps.useGeneratedPaletteInColoring);
        enable_generated_palette.setFocusable(false);

        final JComboBox<String> generated_palettes_combon = new JComboBox<>(Constants.generatedPalettes);
        generated_palettes_combon.setSelectedIndex(outcoloring ? s.gps.generatedPaletteOutColoringId : s.gps.generatedPaletteInColoringId);
        generated_palettes_combon.setFocusable(false);
        generated_palettes_combon.setToolTipText("Sets the generated palette algorithm.");

        JPanel buttons_panel = new JPanel();

        JButton multiwave_edit = new JButton("Multiwave Palette");
        multiwave_edit.setIcon(MainWindow.getIcon("multiwave.png"));
        multiwave_edit.setFocusable(false);
        multiwave_edit.setEnabled(generated_palettes_combon.getSelectedIndex() == 4);
        multiwave_edit.addActionListener(e -> new MultiwaveDialog(this, outcoloring, currentGps));

        JButton iq_edit = new JButton("IQ Cosine Palette");
        iq_edit.setIcon(MainWindow.getIcon("sine.png"));
        iq_edit.setFocusable(false);
        iq_edit.setEnabled(generated_palettes_combon.getSelectedIndex() == 3);
        iq_edit.addActionListener(e -> new IQPaletteDialog(this, outcoloring, currentGps));

        buttons_panel.add(iq_edit);
        buttons_panel.add(multiwave_edit);

        generated_palettes_combon.addActionListener(e -> {
            iq_edit.setEnabled(generated_palettes_combon.getSelectedIndex() == 3);
            multiwave_edit.setEnabled(generated_palettes_combon.getSelectedIndex() == 4);
            if(generated_palettes_combon.getSelectedIndex() == 3) {
                try {
                    int length = Integer.parseInt(generated_palette_restart_field.getText());

                    if(length == GeneratedPaletteSettings.DEFAULT_LARGE_LENGTH) {
                        generated_palette_restart_field.setText("" + GeneratedPaletteSettings.DEFAULT_SMALL_LENGTH);
                    }
                }
                catch (Exception ex) {

                }
            }
        });

        JPanel panel2 = new JPanel();

        JButton overview = new JButton("Preview");
        overview.setIcon(MainWindow.getIcon("preview.png"));
        overview.setFocusable(false);
        overview.addActionListener(e -> {
            try {
                int temp2 = Integer.parseInt(generated_palette_restart_field.getText());

                if (temp2 < 0) {
                    JOptionPane.showMessageDialog(ptra, "The generated palette length value must be greater than -1.", "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (temp2 > 2100000000) {
                    JOptionPane.showMessageDialog(ptra, "The generated palette length value must be lower than 2100000001.", "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                new GeneratedPaletteOverviewDialog(this, outcoloring, currentGps, generated_palettes_combon.getSelectedIndex(), temp2);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ptra, "Illegal Argument: " + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel2.add(overview);

        Object[] message = {
            " ",
                enable_generated_palette,
                " ",
                "Set the generated palette algorithm.",
                "Generated Palette algorithm:", generated_palettes_combon,
                buttons_panel,
            " ",
            "Set the Palette Length.",
            "Palette Length:", generated_palette_restart_field,
                " ",
                panel2,
            " "};

        optionPane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, null);

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
                            dispose();
                            return;
                        }

                        try {
                            int temp2 = Integer.parseInt(generated_palette_restart_field.getText());

                            if (temp2 < 0) {
                                JOptionPane.showMessageDialog(ptra, "The generated palette length value must be greater than -1.", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if (temp2 > 2100000000) {
                                JOptionPane.showMessageDialog(ptra, "The generated palette length value must be lower than 2100000001.", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            s.gps = currentGps;

                            if(outcoloring) {
                                s.gps.useGeneratedPaletteOutColoring = enable_generated_palette.isSelected();
                                s.gps.restartGeneratedOutColoringPaletteAt = temp2;
                                s.gps.generatedPaletteOutColoringId = generated_palettes_combon.getSelectedIndex();
                            }
                            else {
                                s.gps.useGeneratedPaletteInColoring = enable_generated_palette.isSelected();
                                s.gps.restartGeneratedInColoringPaletteAt = temp2;
                                s.gps.generatedPaletteInColoringId = generated_palettes_combon.getSelectedIndex();
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(ptra, "Illegal Argument: " + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        dispose();
                        ptra.setGeneratedPalettePost(outcoloring);
                    }
                });

        //Make this dialog display it.
        setContentPane(optionPane);

        pack();

        setResizable(false);
        setLocation((int) (ptra.getLocation().getX() + ptra.getSize().getWidth() / 2) - (getWidth() / 2), (int) (ptra.getLocation().getY() + ptra.getSize().getHeight() / 2) - (getHeight() / 2));
        setVisible(true);

    }

    public void setParams(GeneratedPaletteSettings gps) {
        currentGps = gps;
    }
}
