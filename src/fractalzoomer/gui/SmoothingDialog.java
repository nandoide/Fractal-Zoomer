
package fractalzoomer.gui;

import fractalzoomer.main.Constants;
import fractalzoomer.main.MainWindow;
import fractalzoomer.main.app_settings.Settings;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import static fractalzoomer.main.Constants.color_interp_str;

/**
 *
 * @author hrkalona2
 */
public class SmoothingDialog extends JDialog {

    private MainWindow ptra;
    private JOptionPane optionPane;

    class Item {
        public String label;
        public int value;
        public Item(String label, int value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public SmoothingDialog(MainWindow ptr, Settings s) {
        
        super(ptr);

        ptra = ptr;

        setTitle("Smoothing");
        setModal(true);
        setIconImage(MainWindow.getIcon("mandel2.png").getImage());

        final JCheckBox enable_smoothing = new JCheckBox("Smoothing");
        enable_smoothing.setSelected(s.fns.smoothing);
        enable_smoothing.setFocusable(false);

        final JCheckBox enable_flat = new JCheckBox("Banding");
        enable_flat.setSelected(s.fns.banded);
        enable_flat.setFocusable(false);

        JComboBox<String> fractional_transfer = new JComboBox<>(Constants.smoothingFractionalTransfer);
        fractional_transfer.setSelectedIndex(s.fns.smoothing_fractional_transfer_method);
        fractional_transfer.setFocusable(false);

        String[] escaping_algorithm_str = {"Algorithm 1", "Algorithm 2", "Algorithm 3"};

        JComboBox<String> escaping_alg_combo = new JComboBox<>(escaping_algorithm_str);
        escaping_alg_combo.setSelectedIndex(s.fns.escaping_smooth_algorithm);
        escaping_alg_combo.setFocusable(false);
        escaping_alg_combo.setToolTipText("Sets the smoothing algorithm for escaping functions.");

        String[] converging_algorithm_str = {"Algorithm 1", "Algorithm 2"};

        JComboBox<String> converging_alg_combo = new JComboBox<>(converging_algorithm_str);
        converging_alg_combo.setSelectedIndex(s.fns.converging_smooth_algorithm);
        converging_alg_combo.setFocusable(false);
        converging_alg_combo.setToolTipText("Sets the smoothing algorithm for converging functions.");

        final JComboBox<String> combo_box_color_interp = new JComboBox<>(color_interp_str);
        combo_box_color_interp.setSelectedIndex(s.color_smoothing_method);
        combo_box_color_interp.setFocusable(false);
        combo_box_color_interp.setToolTipText("Sets the color interpolation method.");

        ArrayList<Item> items = new ArrayList<>();
        for(int i = 0; i < Constants.colorSpaces.length; i++) {
            if(i != Constants.COLOR_SPACE_BEZIER_RGB && i != Constants.COLOR_SPACE_BASIS_SPLINE_RGB) {
                items.add(new Item(Constants.colorSpaces[i], i));
            }
        }
        Item[] items_ar = new Item[items.size()];
        items_ar = items.toArray(items_ar);

        int selectedItemIndex = 0;
        for(int i = 0; i < items_ar.length; i++) {
            if(s.color_space == items_ar[i].value) {
                selectedItemIndex = i;
            }
        }
        final JComboBox<Item> combo_box_color_space = new JComboBox<>(items_ar);
        combo_box_color_space.setSelectedIndex(selectedItemIndex);
        combo_box_color_space.setFocusable(false);
        combo_box_color_space.setToolTipText("Sets the color space.");

        JComboBox<String> color_selection = new JComboBox<>(new String[] {"n - 1, n", "n, n + 1"});
        color_selection.setSelectedIndex(s.fns.smoothing_color_selection);
        color_selection.setFocusable(false);

//        final JCheckBox apply_offset_of_1 = new JCheckBox("Apply offset of 1");
//        apply_offset_of_1.setSelected(s.fns.apply_offset_in_smoothing);
//        apply_offset_of_1.setFocusable(false);
//        apply_offset_of_1.setToolTipText("Adds an offset of 1 to the calculated fractional value for smoothing.");

        if (s.ds.domain_coloring) {
            escaping_alg_combo.setEnabled(false);
            converging_alg_combo.setEnabled(false);
        }

        if (s.ds.domain_coloring && s.ds.domain_coloring_mode != 1) {
            enable_smoothing.setEnabled(false);
            fractional_transfer.setEnabled(false);
        }

        Object[] message = {
            " ",
            enable_smoothing,
                " ",
                enable_flat,
            " ",
                "Fractional Transfer:",
                fractional_transfer,
                " ",
                "Set the color indexing parameters.",
                "Color Index Selection:",
                color_selection,
               // apply_offset_of_1,
                " ",
            "Set the smoothing algorithm for escaping and converging functions.",
            "Escaping:", escaping_alg_combo,
            "Converging:", converging_alg_combo,
            " ",
            "Set the color interpolation method.",
                "Interpolation Method:",
            combo_box_color_interp,
                " ",
                "Set the color space.",
                "Color Space:",
                combo_box_color_space,
            " ",};

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

                        boolean recalculate = false;
                        try {
                            s.fns.smoothing = enable_smoothing.isSelected();
                            s.fns.banded = enable_flat.isSelected();

                            if(s.fns.escaping_smooth_algorithm != escaping_alg_combo.getSelectedIndex()
                             || s.fns.converging_smooth_algorithm != converging_alg_combo.getSelectedIndex()
                           // || s.fns.apply_offset_in_smoothing != apply_offset_of_1.isSelected()
                            ) {
                                recalculate = true;
                            }

                           // s.fns.apply_offset_in_smoothing = apply_offset_of_1.isSelected();
                            s.fns.smoothing_color_selection = color_selection.getSelectedIndex();
                            s.fns.escaping_smooth_algorithm = escaping_alg_combo.getSelectedIndex();
                            s.fns.converging_smooth_algorithm = converging_alg_combo.getSelectedIndex();
                            s.color_smoothing_method = combo_box_color_interp.getSelectedIndex();
                            s.fns.smoothing_fractional_transfer_method = fractional_transfer.getSelectedIndex();
                            s.color_space = ((Item)combo_box_color_space.getSelectedItem()).value;
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(ptra, "Illegal Argument: " + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        dispose();
                        ptra.setSmoothingPost(recalculate);
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
