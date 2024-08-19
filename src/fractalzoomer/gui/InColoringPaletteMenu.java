
package fractalzoomer.gui;

import fractalzoomer.main.MainWindow;
import fractalzoomer.main.app_settings.PaletteSettings;
import fractalzoomer.main.app_settings.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 *
 * @author kaloch
 */
public class InColoringPaletteMenu extends MyMenu {
    private static final long serialVersionUID = -6910423535L;
    private MainWindow ptr;
    private PaletteMenu palette_menu;
    private JMenu roll_palette_menu;
    private JMenuItem roll_palette;
    private JMenuItem increase_roll_palette;
    private JMenuItem decrease_roll_palette;
    private JMenuItem color_intensity_opt;
    private ColorTransferMenu color_transfer_menu;

    private JMenuItem generated_palette_opt;
    private JCheckBoxMenuItem usePaletteForInColoring_opt;
    
    public InColoringPaletteMenu(MainWindow ptr2, String name, PaletteSettings ps, boolean smoothing, int temp_color_cycling_location) {
        super(name);

        this.ptr = ptr2;
        
        setIcon(MainWindow.getIcon("palette_incoloring.png"));
        
        palette_menu = new PaletteMenu(ptr, "Palette", ps.color_choice, smoothing, ps.custom_palette, ps.color_interpolation, ps.color_space, ps.reversed_palette, ps.color_cycling_location, ps.scale_factor_palette_val, ps.processing_alg, false, temp_color_cycling_location);
        
        roll_palette_menu = new MyMenu("Palette Shifting");
        roll_palette_menu.setIcon(MainWindow.getIcon("shift_palette.png"));
        
        roll_palette = new MyMenuItem("Shift Palette", MainWindow.getIcon("shift_palette.png"));

        increase_roll_palette = new MyMenuItem("Shift Palette Forward", MainWindow.getIcon("plus.png"));

        decrease_roll_palette = new MyMenuItem("Shift Palette Backward", MainWindow.getIcon("minus.png"));
        
        color_transfer_menu = new ColorTransferMenu(ptr, "Transfer Functions", ps.transfer_function, false);
        
        color_intensity_opt = new MyMenuItem("Color Intensity", MainWindow.getIcon("color_intensity.png"));

        generated_palette_opt = new MyMenuItem("Generated Palette", MainWindow.getIcon("palette.png"));
        
        usePaletteForInColoring_opt = new MyCheckBoxMenuItem("Use In-Coloring Palette");
        
        roll_palette.setToolTipText("Shifts the chosen palette by a number.");
        increase_roll_palette.setToolTipText("Shifts the chosen palette forward by one.");
        decrease_roll_palette.setToolTipText("Shifts the chosen palette backward by one.");
        
        color_intensity_opt.setToolTipText("Changes the color intensity of the in-coloring palette.");
        usePaletteForInColoring_opt.setToolTipText("Enables the use of a secondary palette for in-coloring.");

        generated_palette_opt.setToolTipText("Uses palette generated by formulas.");

        generated_palette_opt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.SHIFT_MASK | ActionEvent.ALT_MASK));
        roll_palette.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        increase_roll_palette.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
        decrease_roll_palette.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
        color_intensity_opt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.SHIFT_MASK));
        usePaletteForInColoring_opt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.ALT_MASK));
        
        roll_palette.addActionListener(e -> ptr.shiftPalette(false));

        increase_roll_palette.addActionListener(e -> ptr.shiftPaletteForward(false));

        decrease_roll_palette.addActionListener(e -> ptr.shiftPaletteBackward(false));
        
        color_intensity_opt.addActionListener(e -> ptr.setColorIntensity(false));
        
        usePaletteForInColoring_opt.addActionListener(e -> ptr.setUsePaletteForInColoring());

        generated_palette_opt.addActionListener(e -> ptr.setGeneratedPalette(false));
        
        roll_palette_menu.add(roll_palette);
        roll_palette_menu.add(increase_roll_palette);
        roll_palette_menu.add(decrease_roll_palette);
        
        add(usePaletteForInColoring_opt);
        addSeparator();
        add(palette_menu);
        add(generated_palette_opt);
        add(roll_palette_menu);
        addSeparator();
        add(color_transfer_menu);    
        addSeparator();
        add(color_intensity_opt);
        
    }
    
    public JRadioButtonMenuItem[] getPalette() {
        
        return palette_menu.getPalette();
        
    }
    
    public JMenu getRollPaletteMenu() {
        
        return roll_palette_menu;
        
    }
    
    public PaletteMenu getPaletteMenu() {
        
        return palette_menu;
        
    }
    
    public JRadioButtonMenuItem[] getInColoringTranferFunctions() {
        
        return color_transfer_menu.getTranferFunctions();
        
    }
    
    public JCheckBoxMenuItem getUsePaletteForInColoring() {
        
        return usePaletteForInColoring_opt;
        
    }

    public JMenuItem getGeneratedPaletteOpt() {
        return generated_palette_opt;
    }

    public void updateIcons(Settings s) {

        if(s.gps.useGeneratedPaletteInColoring) {
            generated_palette_opt.setIcon(MainWindow.getIcon("palette_enabled.png"));
        }
        else {
            generated_palette_opt.setIcon(MainWindow.getIcon("palette.png"));
        }

    }

}
