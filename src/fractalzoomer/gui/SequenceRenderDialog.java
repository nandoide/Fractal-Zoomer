/*
 * Copyright (C) 2020 hrkalona2
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fractalzoomer.gui;

import fractalzoomer.core.MyApfloat;
import fractalzoomer.functions.Fractal;
import fractalzoomer.main.ImageExpanderWindow;
import fractalzoomer.main.MainWindow;
import fractalzoomer.main.app_settings.Settings;
import org.apfloat.Apfloat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static fractalzoomer.gui.CenterSizeDialog.TEMPLATE_TFIELD;

/**
 *
 * @author hrkalona2
 */
public class SequenceRenderDialog extends JDialog {

    private ImageExpanderWindow ptra;
    private JOptionPane optionPane;

    private JFileChooser file_chooser;

    public SequenceRenderDialog(ImageExpanderWindow ptr, Settings s, double zoom_factor, int zooming_mode, Apfloat size, double rotation_adjusting_value, int color_cycling_adjusting_value, double light_light_direction_adjusting_value, double bump_light_direction_adjusting_value, int zoom_every_n_frame, int gradient_color_cycling_adjusting_value) {

        super(ptr);
        
        ptra = ptr;

        setTitle("Zoom Sequence Render");
        setModal(true);
        setIconImage(MainWindow.getIcon("mandel2.png").getImage());

        final JComboBox<String> zoooming_mode = new JComboBox<>(new String[] {"Zoom-In", "Zoom-Out"});
        zoooming_mode.setSelectedIndex(zooming_mode);
        zoooming_mode.setFocusable(false);
        zoooming_mode.setToolTipText("Sets zooming mode.");
        zoooming_mode.setPreferredSize(new Dimension(150, 20));



        JTextArea field_size = new JTextArea(6, 50);
        field_size.setLineWrap(true);
        field_size.setFont(TEMPLATE_TFIELD.getFont());
        field_size.setText("" + size);

        JScrollPane scrollSize = new JScrollPane (field_size,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        disableKeys(field_size.getInputMap());
        disableKeys(scrollSize.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));


        JTextArea settings_size = new JTextArea(6, 50);
        settings_size.setLineWrap(true);
        settings_size.setFont(TEMPLATE_TFIELD.getFont());
        settings_size.setEditable(false);
        settings_size.setForeground(Color.GRAY);
        settings_size.setText("" + s.size);

        JScrollPane scrollSettingsSize = new JScrollPane (settings_size,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        disableKeys(settings_size.getInputMap());
        disableKeys(scrollSettingsSize.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

        SwingUtilities.invokeLater(() -> {
            scrollSize.getVerticalScrollBar().setValue(0);
            scrollSettingsSize.getVerticalScrollBar().setValue(0);
        });

        JTextField fieldZoom = new JTextField();
        fieldZoom.setText("" + zoom_factor);

        JTextField fieldRotation = new JTextField();
        fieldRotation.setText("" + rotation_adjusting_value);

        JTextField fieldColorCycling = new JTextField();
        fieldColorCycling.setText("" + color_cycling_adjusting_value);

        JTextField fieldGradientColorCycling = new JTextField();
        fieldGradientColorCycling.setText("" + gradient_color_cycling_adjusting_value);

        JTextField fieldLightCycling = new JTextField();
        fieldLightCycling.setText("" + light_light_direction_adjusting_value);

        JTextField filedBumpLightCycling = new JTextField();
        filedBumpLightCycling.setText("" + bump_light_direction_adjusting_value);

        JTextField fieldZoomEveryNFrame = new JTextField();
        fieldZoomEveryNFrame.setText("" + zoom_every_n_frame);


        Object[] message = {
            " ",
                "Set the Zooming Mode.",
                "Zooming Mode:",
                zoooming_mode,
                " ",
                "Set the Initial (Zoom-In) or End (Zoom-Out) size of the sequence.",
                "Size:",
                scrollSize,
                "Settings Size:",
                scrollSettingsSize,
                " ",
                "Set the zooming factor.",
                "Zooming Factor:",
                fieldZoom,
                " ",
                "Set the zoom frequency.",
                "Zoom Every N Frame:",
                fieldZoomEveryNFrame,
                " ",
                "Set the rotation adjusting value in degrees.",
                "Rotation Adjusting Value:",
                fieldRotation,
                " ",
                "Set the color cycling adjusting value.",
                "Color Cycling Adjusting Value:",
                fieldColorCycling,
                " ",
                "Set the gradient color cycling adjusting value.",
                "Gradient Color Cycling Adjusting Value:",
                fieldGradientColorCycling,
                " ",
                "Set the light direction adjusting value in degrees. (When Light is Enabled)",
                "Light Direction Adjusting Value:",
                fieldLightCycling,
                " ",
                "Set the bump mapping light direction adjusting value in degrees. (When Bump Mapping is Enabled)",
                "Bump Mapping Light Direction Adjusting Value:",
                filedBumpLightCycling,

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
                            double tempZoomFactor = Double.parseDouble(fieldZoom.getText());
                            double tempRotation = Double.parseDouble(fieldRotation.getText());
                            int tempColorCycling = Integer.parseInt(fieldColorCycling.getText());
                            double tempLight = Double.parseDouble(fieldLightCycling.getText());
                            double tempBumpLight = Double.parseDouble(filedBumpLightCycling.getText());
                            int tempZoomNFrame = Integer.parseInt(fieldZoomEveryNFrame.getText());
                            int tempGradientColorCycling = Integer.parseInt(fieldGradientColorCycling.getText());
                            if(MyApfloat.setAutomaticPrecision) {
                                long precision = MyApfloat.getAutomaticPrecision(new String[]{field_size.getText()}, new boolean[]{true});

                                if (MyApfloat.shouldSetPrecision(precision, false)) {
                                    Fractal.clearReferences(true);
                                    MyApfloat.setPrecision(precision, s);
                                }
                            }

                            Apfloat tempSize = new MyApfloat(field_size.getText());

                            if(tempSize.compareTo(s.size) <= 0) {
                                JOptionPane.showMessageDialog(ptra, "The value entered in the size must be greater than the settings size.", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if (tempZoomFactor <= 1.05) {
                                JOptionPane.showMessageDialog(ptra, "Zooming factor must be greater than 1.05.", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            } else if (tempZoomFactor > 32) {
                                JOptionPane.showMessageDialog(ptra, "Zooming factor must be less than 33.", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if(tempColorCycling < 0 || tempColorCycling > 10000) {
                                JOptionPane.showMessageDialog(ptra, "The color cycling adjusting value must be in the range [0, 10000].", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if(tempGradientColorCycling < 0 || tempGradientColorCycling > 10000) {
                                JOptionPane.showMessageDialog(ptra, "The gradient color cycling adjusting value must be in the range [0, 10000].", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if(tempRotation > 90 || tempRotation < -90) {
                                JOptionPane.showMessageDialog(ptra, "The rotation adjusting value must be a number in the range [-90, 90] degrees.", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if(tempLight > 90 || tempLight < -90) {
                                JOptionPane.showMessageDialog(ptra, "The light direction adjusting value must be a number in the range [-90, 90] degrees.", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if(tempBumpLight > 90 || tempBumpLight < -90) {
                                JOptionPane.showMessageDialog(ptra, "The bump mapping light direction adjusting value must be a number in the range [-90, 90] degrees.", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if(tempZoomNFrame < 1 || tempZoomNFrame > 20) {
                                JOptionPane.showMessageDialog(ptra, "The \"zoom every N frame\" value must be a number in the range [1, 20].", "Error!", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            ptr.startSequenceRender(tempSize, tempZoomFactor, zoooming_mode.getSelectedIndex(), tempRotation, tempColorCycling, tempLight, tempBumpLight, tempZoomNFrame, tempGradientColorCycling);

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(ptra, "Illegal Argument: " + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
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

    public static void disableKeys(InputMap inputMap) {
        String[] keys = {"ENTER"};
        for (String key : keys) {
            inputMap.put(KeyStroke.getKeyStroke(key), "none");
        }
    }

}
