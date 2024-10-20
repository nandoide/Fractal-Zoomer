
package fractalzoomer.gui;

import fractalzoomer.core.MyApfloat;
import fractalzoomer.core.TaskRender;
import fractalzoomer.functions.Fractal;
import fractalzoomer.main.Constants;
import fractalzoomer.main.MainWindow;
import fractalzoomer.main.app_settings.GeneratedPaletteSettings;
import fractalzoomer.main.app_settings.Settings;
import fractalzoomer.utils.Multiwave;
import fractalzoomer.utils.TaskStatistic;
import org.apfloat.Apfloat;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static fractalzoomer.gui.CenterSizeDialog.TEMPLATE_TFIELD;

/**
 *
 * @author hrkalona2
 */
public class MultiwaveDialog extends JDialog {
    private JOptionPane optionPane;
    private GeneratedPaletteDialog ptra;

    public MultiwaveDialog(GeneratedPaletteDialog ptr, boolean outcoloring, GeneratedPaletteSettings gps) {

        super(ptr);
        
        ptra = ptr;

        Multiwave.WaveColorParams[] inputParams;

        try {
            if (outcoloring) {
                inputParams = Multiwave.jsonToParams(gps.outcoloring_multiwave_user_palette);
            } else {
                inputParams = Multiwave.jsonToParams(gps.incoloring_multiwave_user_palette);
            }
        }
        catch (Exception ex) {
            inputParams = Multiwave.empty;
        }

        setTitle("Multiwave Palette");
        setModal(true);
        setIconImage(MainWindow.getIcon("mandel2.png").getImage());

        JComboBox<String> templates = new JComboBox<>(Constants.multiwavePalettes);
        templates.setSelectedIndex(0);
        templates.setFocusable(false);

        RSyntaxTextArea multiwaveTextArea = new RSyntaxTextArea(20, 60);
        multiwaveTextArea.setFont(TEMPLATE_TFIELD.getFont());
        multiwaveTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        multiwaveTextArea.setCodeFoldingEnabled(true);
        multiwaveTextArea.setAntiAliasingEnabled(true);
        multiwaveTextArea.setAnimateBracketMatching(true);
        multiwaveTextArea.setLineWrap(true);
        multiwaveTextArea.setAutoIndentEnabled(true);
        multiwaveTextArea.setMarkOccurrences(true);

        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
            theme.apply(multiwaveTextArea);
        } catch (IOException ioe) { // Never happens

        }

        boolean showNulls = false;

        templates.addActionListener(e -> {
            String json = "";
            try {
                if(templates.getSelectedIndex() == 1) {
                    json = Multiwave.paramsToJson(Multiwave.default_params, showNulls);
                }
                else if(templates.getSelectedIndex() == 2) {
                    json = Multiwave.paramsToJson(Multiwave.g_spdz2_params, showNulls);
                }
                else if(templates.getSelectedIndex() == 3) {
                    json = Multiwave.paramsToJson(Multiwave.g_spdz2_params, showNulls);
                }
                else {
                    json = Multiwave.paramsToJson(Multiwave.empty, true);
                }
            }
            catch (Exception ex) {

            }

            if(!json.isEmpty()) {
                multiwaveTextArea.setText(json);
            }
        });


        try {
            multiwaveTextArea.setText(Multiwave.paramsToJson(inputParams, inputParams == Multiwave.empty ? true : showNulls));
            multiwaveTextArea.setCaretPosition(0);
        }
        catch (Exception ex) {

        }

        RTextScrollPane sp = new RTextScrollPane(multiwaveTextArea, true);

        SwingUtilities.invokeLater(() -> {
            sp.getVerticalScrollBar().setValue(0);
            multiwaveTextArea.setCaretPosition(0);
        });

        JPanel buttons_panel = new JPanel();

        JButton view_colors = new JButton("View Colors");
        //view_colors.setIcon(MainWindow.getIcon("multiwave.png"));
        view_colors.setFocusable(false);
        view_colors.addActionListener(e -> {
            Multiwave.WaveColorParams[] params = null;
            try {
                params = Multiwave.jsonToParams(multiwaveTextArea.getText());
                Multiwave.WaveColorParams.build(params, 0);
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(ptra, "Illegal Argument: " + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(params != null) {
                viewColors(this, params);
            }
        });

        buttons_panel.add(view_colors);

        Object[] message = {
            " ",
                "Templates:",
                templates,
                " ",
                buttons_panel,
                " ",
            "Set the multiwave parameters.",
            "Parameters:",
                sp,
            " ",
            };

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
                            Multiwave.WaveColorParams[] params = Multiwave.jsonToParams(multiwaveTextArea.getText());
                            Multiwave.WaveColorParams.build(params, 0);
                            if(outcoloring) {
                                try {
                                    gps.outcoloring_multiwave_user_palette = Multiwave.paramsToJson(params, false);
                                }
                                catch (Exception ex) {
                                    gps.outcoloring_multiwave_user_palette = "";
                                }
                            }
                            else {
                                try {
                                    gps.incoloring_multiwave_user_palette = Multiwave.paramsToJson(params, false);
                                }
                                catch (Exception ex) {
                                    gps.incoloring_multiwave_user_palette = "";
                                }
                            }
                            ptr.setParams(gps);
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

    public void viewColors(JDialog ptr, Multiwave.WaveColorParams[] params) {
        JEditorPane textArea = new JEditorPane();

        textArea.setEditable(false);
        textArea.setContentType("text/html");
        textArea.setPreferredSize(new Dimension(500, 300));
        //textArea.setLineWrap(false);
        //textArea.setWrapStyleWord(false);

        JScrollPane scroll_pane_2 = new JScrollPane(textArea);
        scroll_pane_2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        String data = "";
        data += "<ul>";
        for(int i = 0; i < params.length; i++) {
            Color[] tricubic_rgb = params[i].getTricubicRGBColors();
            Color[] tricubic_hsl = params[i].getTricubicHSLColors();
            Color[] linear_rgb = params[i].getLinearRGBColors();
            Color[] linear_hsl = params[i].getLinearHSLColors();
            Color[][] metatricubic_rgb = params[i].getMetaTricubicRGBColors();
            Color[][] metatricubic_hsl = params[i].getMetaTricubicHSLColors();

            Color[] colors = null;
            Color[][] meta_colors = null;

            String type = "";

            if(tricubic_rgb != null) {
                colors = tricubic_rgb;
                type = "Tricubic RGB";
            }
            else if(tricubic_hsl != null) {
                colors = tricubic_hsl;
                type = "Tricubic HSL";
            }
            else if(linear_rgb != null) {
                colors = linear_rgb;
                type = "Linear RGB";
            }
            else if(linear_hsl != null) {
                colors = linear_hsl;
                type = "Linear HSL";
            }
            else if(metatricubic_rgb != null) {
                meta_colors = metatricubic_rgb;
                type = "Meta-Tricubic RGB";
            }
            else if(metatricubic_hsl != null) {
                meta_colors = metatricubic_hsl;
                type = "Meta-Tricubic HSL";
            }

            data += "<li> Color Wave: <b>" + (i + 1) + " (" + type + ")</b>";

            data += "<ul>";

            if(colors != null) {
                data += "<li>{";
                for(int k = 0; k < colors.length; k++) {
                    if(k == 0) {
                        data += "<span style='font-size:20px;color: " + String.format("#%02x%02x%02x", colors[k].getRed(), colors[k].getGreen(), colors[k].getBlue()) + ";'>&#9632;</span>";
                    }
                    else {
                        data += ", <span style='font-size:20px;color: " + String.format("#%02x%02x%02x", colors[k].getRed(), colors[k].getGreen(), colors[k].getBlue()) + ";'>&#9632;</span>";
                    }
                }
                data += "}</li>";
            }
            else if(meta_colors != null) {
                for(int l = 0; l < meta_colors.length; l++) {
                    if(l == 0) {
                        data += "<li>[{";
                    }
                    else {
                        data += "<li>{";
                    }
                    for (int k = 0; k < meta_colors[l].length; k++) {
                        if (k == 0) {
                            data += "<span style='font-size:20px;color: " + String.format("#%02x%02x%02x", meta_colors[l][k].getRed(), meta_colors[l][k].getGreen(), meta_colors[l][k].getBlue()) + ";'>&#9632;</span>";
                        } else {
                            data += ", <span style='font-size:20px;color: " + String.format("#%02x%02x%02x", meta_colors[l][k].getRed(), meta_colors[l][k].getGreen(), meta_colors[l][k].getBlue()) + ";'>&#9632;</span>";
                        }
                    }
                    if(l == meta_colors.length - 1) {
                         data += "}]</li>";
                    }
                    else {
                        data += "},";
                    }
                }
            }
            data += "</ul>";
        }
        data += "</ul>";

        textArea.setText("<html>" + "<center><b><u><font size='5' face='arial' color='blue'>Colors</font></u></b></center><br><br>" +
                "<font size='4' face='arial'>" + data + "</font></html>");

        Object[] message = {
                " ",
                scroll_pane_2,
                " "};

        textArea.setCaretPosition(0);

        JOptionPane.showMessageDialog(ptr, message,  "Colors", JOptionPane.INFORMATION_MESSAGE, MainWindow.getIcon("colors_large.png"));

    }

}
