
package fractalzoomer.gui;

import fractalzoomer.core.MyApfloat;
import fractalzoomer.functions.Fractal;
import fractalzoomer.main.Constants;
import fractalzoomer.main.MainWindow;
import fractalzoomer.main.MinimalRendererWindow;
import fractalzoomer.main.app_settings.Settings;
import org.apfloat.Apfloat;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static fractalzoomer.gui.CenterSizeDialog.TEMPLATE_TFIELD;

/**
 *
 * @author hrkalona2
 */
public class SequenceMagnificationDialog extends JDialog {

    private MinimalRendererWindow ptra;
    private JOptionPane optionPane;

    public SequenceMagnificationDialog(MinimalRendererWindow ptr, Settings s, JTextArea field_size, JTextArea field_size_readonly) {

        super(ptr);
        
        ptra = ptr;

        setTitle("Magnification/Zoom");
        setModal(true);
        setIconImage(MainWindow.getIcon("mandel2.png").getImage());

        Apfloat tempSize;
        Apfloat tempSizeReadOnly;
        try {

            if(MyApfloat.setAutomaticPrecision) {
                long precision = MyApfloat.getAutomaticPrecision(new String[]{field_size.getText(), field_size_readonly.getText()}, new boolean[] {true, true}, s.fns.function);

                if (MyApfloat.shouldSetPrecision(precision, MyApfloat.alwaysCheckForDecrease, s.fns.function)) {
                    Fractal.clearReferences(true, true);
                    MyApfloat.setPrecision(precision, s);
                }
            }

            tempSize = new MyApfloat(field_size.getText());
            tempSizeReadOnly = new MyApfloat(field_size_readonly.getText());
        } catch (Exception ex) {
            tempSize = s.size;
            tempSizeReadOnly = s.size;
        }

        Apfloat magnificationVal = MyApfloat.fp.divide(Constants.DEFAULT_MAGNIFICATION, tempSize);
        Apfloat magnificationValReadOnly = MyApfloat.fp.divide(Constants.DEFAULT_MAGNIFICATION, tempSizeReadOnly);


        JTextArea magnification = new JTextArea(6, 50);
        magnification.setFont(TEMPLATE_TFIELD.getFont());
        magnification.setLineWrap(true);

        JScrollPane magnScroll = new JScrollPane (magnification,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        CenterSizeDialog.disableKeys(magnification.getInputMap());
        CenterSizeDialog.disableKeys(magnScroll.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

        magnification.setText("" + magnificationVal);

        JTextArea magnificationReadOnly = new JTextArea(6, 50);
        magnificationReadOnly.setFont(TEMPLATE_TFIELD.getFont());
        magnificationReadOnly.setLineWrap(true);
        magnificationReadOnly.setEnabled(false);

        JScrollPane magnScroll2 = new JScrollPane (magnificationReadOnly,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        CenterSizeDialog.disableKeys(magnificationReadOnly.getInputMap());
        CenterSizeDialog.disableKeys(magnScroll2.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

        magnificationReadOnly.setText("" + magnificationValReadOnly);

        SwingUtilities.invokeLater(() -> {
            magnScroll.getVerticalScrollBar().setValue(0);
            magnScroll2.getVerticalScrollBar().setValue(0);
        });

        Object[] message = {
            " ",
            "Set the magnification/zoom.",
            "Magnification/Zoom:",
                magnScroll,
                "Settings Magnification/Zoom:",
                magnScroll2,
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

                            if(MyApfloat.setAutomaticPrecision) {
                                long precision = MyApfloat.getAutomaticPrecision(new String[]{magnification.getText(), magnificationReadOnly.getText()}, new boolean[] {true, true}, s.fns.function);

                                if (MyApfloat.shouldSetPrecision(precision, MyApfloat.alwaysCheckForDecrease, s.fns.function)) {
                                    Fractal.clearReferences(true, true);
                                    MyApfloat.setPrecision(precision, s);
                                }
                            }

                            Apfloat tempMagn = new MyApfloat(magnification.getText());
                            field_size.setText("" + MyApfloat.fp.divide(Constants.DEFAULT_MAGNIFICATION, tempMagn));
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

}