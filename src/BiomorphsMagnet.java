/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hrkalona2
 */
public class BiomorphsMagnet extends Biomorphs {
    
    public BiomorphsMagnet(double bailout) {

        super(bailout);

    }

    @Override
    public double getResult(Object[] object) {

        double temp = ((Complex)object[1]).getRe();
        double temp2 = ((Complex)object[1]).getIm();
        return (Boolean)object[3] ? (temp > -bailout && temp < bailout || temp2 > -bailout && temp2 < bailout ?  (Double)object[0] + 100234 : (Double)object[0] + 100850) : (temp > -bailout && temp < bailout || temp2 > -bailout && temp2 < bailout ?  (Double)object[0] : (Double)object[0] + 100850);

    }
    
}
