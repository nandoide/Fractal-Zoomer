
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hrkalona
 */
public abstract class Fractal {
  protected double xCenter;
  protected double yCenter;
  protected double size;
  protected int max_iterations;
  protected double bailout;
  protected double bailout_squared;
  protected OutColorAlgorithm out_color_algorithm;
  protected InColorAlgorithm in_color_algorithm;
  protected BailoutTest bailout_algorithm;
  protected Plane plane;
  protected Rotation rotation;
  protected InitialValue init_val;
  protected boolean periodicity_checking;
  protected ArrayList<Complex> complex_orbit;
  protected Complex pixel_orbit;
  protected int check;
  protected int check_counter;
  protected int update;
  protected int update_counter;
  protected Complex period;


    public Fractal(double xCenter, double yCenter, double size, int max_iterations, int bailout_test_algorithm, double bailout, boolean periodicity_checking, int plane_type, double[] rotation_vals) {

        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.size = size;
        this.max_iterations = max_iterations;
        this.bailout = bailout;
        bailout_squared = bailout * bailout;
        this.periodicity_checking = periodicity_checking;
   
        rotation = new Rotation(rotation_vals[0], rotation_vals[1]);

        switch (plane_type) {
            case MainWindow.MU_PLANE:
                plane = new MuPlane();
                break;
            case MainWindow.MU_SQUARED_PLANE:
                plane = new MuSquaredPlane();
                break;
            case MainWindow.INVERSED_MU_PLANE:
                plane = new InversedMuPlane();
                break;
            case MainWindow.INVERSED_MU2_PLANE:
                plane = new InversedMu2Plane();
                break;
            case MainWindow.INVERSED_MU3_PLANE:
                plane = new InversedMu3Plane();
                break;
            case MainWindow.INVERSED_MU4_PLANE:
                plane = new InversedMu4Plane();
                break;
            case MainWindow.LAMBDA_PLANE:
                plane = new LambdaPlane();
                break;
            case MainWindow.INVERSED_LAMBDA_PLANE:
                plane = new InversedLambdaPlane();
                break;
            case MainWindow.EXP_PLANE:
                plane = new ExpPlane();
                break;
            case MainWindow.LOG_PLANE:
                plane = new LogPlane();
                break;
            case MainWindow.SIN_PLANE:
                plane = new SinPlane();
                break;
            case MainWindow.COS_PLANE:
                plane = new CosPlane();
                break;
            case MainWindow.TAN_PLANE:
                plane = new TanPlane();
                break;
            case MainWindow.COT_PLANE:
                plane = new CotPlane();
                break;
            case MainWindow.SINH_PLANE:
                plane = new SinhPlane();
                break;
            case MainWindow.COSH_PLANE:
                plane = new CoshPlane();
                break;
            case MainWindow.TANH_PLANE:
                plane = new TanhPlane();
                break;
            case MainWindow.COTH_PLANE:
                plane = new CothPlane();
                break;
            case MainWindow.SQRT_PLANE:
                plane = new SqrtPlane();
                break;
            case MainWindow.ABS_PLANE:
                plane = new AbsPlane();
                break;
                
        }
        
   
        switch (bailout_test_algorithm) {
            
            case MainWindow.BAILOUT_TEST_CIRCLE:
                bailout_algorithm = new CircleBailoutTest(bailout_squared);
                break;
            case MainWindow.BAILOUT_TEST_SQUARE:
                bailout_algorithm = new SquareBailoutTest(bailout);
                break;
            case MainWindow.BAILOUT_TEST_STRIP:
                bailout_algorithm = new StripBailoutTest(bailout);
                break;
            case MainWindow.BAILOUT_TEST_HALFPLANE:
                bailout_algorithm = new HalfplaneBailoutTest(bailout);
                break;
                
        }
        
        

    }

    //orbit
    public Fractal(double xCenter, double yCenter, double size, int max_iterations, ArrayList<Complex> complex_orbit, int plane_type, double[] rotation_vals) {

        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.size = size;
        this.max_iterations = max_iterations;
        this.complex_orbit = complex_orbit;
        pixel_orbit = this.complex_orbit.get(0);
        
        rotation = new Rotation(rotation_vals[0], rotation_vals[1]);
        
        switch (plane_type) {
            case MainWindow.MU_PLANE:
                plane = new MuPlane();
                break;
            case MainWindow.MU_SQUARED_PLANE:
                plane = new MuSquaredPlane();
                break;
            case MainWindow.INVERSED_MU_PLANE:
                plane = new InversedMuPlane();
                break;
            case MainWindow.INVERSED_MU2_PLANE:
                plane = new InversedMu2Plane();
                break;
            case MainWindow.INVERSED_MU3_PLANE:
                plane = new InversedMu3Plane();
                break;
            case MainWindow.INVERSED_MU4_PLANE:
                plane = new InversedMu4Plane();
                break;
            case MainWindow.LAMBDA_PLANE:
                plane = new LambdaPlane();
                break;
            case MainWindow.INVERSED_LAMBDA_PLANE:
                plane = new InversedLambdaPlane();
                break;
            case MainWindow.EXP_PLANE:
                plane = new ExpPlane();
                break;
            case MainWindow.LOG_PLANE:
                plane = new LogPlane();
                break;
            case MainWindow.SIN_PLANE:
                plane = new SinPlane();
                break;
            case MainWindow.COS_PLANE:
                plane = new CosPlane();
                break;
            case MainWindow.TAN_PLANE:
                plane = new TanPlane();
                break;
            case MainWindow.COT_PLANE:
                plane = new CotPlane();
                break;
            case MainWindow.SINH_PLANE:
                plane = new SinhPlane();
                break;
            case MainWindow.COSH_PLANE:
                plane = new CoshPlane();
                break;
            case MainWindow.TANH_PLANE:
                plane = new TanhPlane();
                break;
            case MainWindow.COTH_PLANE:
                plane = new CothPlane();
                break;
            case MainWindow.SQRT_PLANE:
                plane = new SqrtPlane();
                break;
            case MainWindow.ABS_PLANE:
                plane = new AbsPlane();
                break;
                
        }
        
        pixel_orbit = plane.getPixel(rotation.getPixel(pixel_orbit, false));

    }

    protected abstract void function(Complex[] complex);

    protected double getPeriodSize() {

        return 1e-13;

    }

    protected boolean periodicityCheck(Complex z) {

        
        //Check for period
        if(z.sub(period).norm_squared() < getPeriodSize()) {
            return true;
        }

                //Update history
        if(check == check_counter) {
            check_counter = 0;

            //Double the value of check
            if(update == update_counter) {
                update_counter = 0;
                check <<= 1;
            }
            update_counter++;

            period = z;  
        } //End of update history

        check_counter++;

        return false;

    }

    public double calculateFractal(Complex pixel) {
        
        return periodicity_checking ? calculateFractalWithPeriodicity(plane.getPixel(rotation.getPixel(pixel, false))) : calculateFractalWithoutPeriodicity(plane.getPixel(rotation.getPixel(pixel, false)));

    }

    protected double calculateFractalWithPeriodicity(Complex pixel) {

        int iterations = 0;

        check = 3;
        check_counter = 0;

        update = 10;
        update_counter = 0;

        period = new Complex(0, 0);

        Complex tempz = init_val.getPixel(pixel);
        
        Complex[] complex = new Complex[2];
        complex[0] = tempz;//z
        complex[1] = pixel;//c

        Complex zold = new Complex(0, 0);

        for (; iterations < max_iterations; iterations++) {
            if(bailout_algorithm.escaped(complex[0])) {
                Object[] object = {iterations, complex[0], zold};
                return out_color_algorithm.getResult(object);
            }
            zold = complex[0];
            function(complex);

            if(periodicityCheck(complex[0])) {
                return max_iterations;
            }

        }

        return max_iterations;

    }

    
    protected double calculateFractalWithoutPeriodicity(Complex pixel) {
        
        int iterations = 0;

        Complex tempz = init_val.getPixel(pixel);
        
        Complex[] complex = new Complex[2];
        complex[0] = tempz;//z
        complex[1] = pixel;//c
        

        Complex zold = new Complex(0, 0);


        for (; iterations < max_iterations; iterations++) {
                  
            if(bailout_algorithm.escaped(complex[0])) {
                Object[] object = {iterations, complex[0], zold};
                return out_color_algorithm.getResult(object);                
            }
            zold = complex[0];
            function(complex);
            
            
            
        }

        
        Object[] object = {max_iterations, complex[0]};
        return in_color_algorithm.getResult(object);
        
       /* int iterations = 0; 
        
        int exp = 28;
        long  dx = (long)Math.pow(2, exp);//(long)(Math.pow(2, 16) / (size) + 0.5);
        
          
        
        long real = (long)(pixel.getRe() * dx + 0.5);
        long imag = (long)(pixel.getIm() * dx + 0.5);

        long z_real = real;
        long z_imag = imag;
        long temp;
        
        long re_sqr;
        long im_sqr;
        
        long bail = dx << 2;
        
        int exp_1 = exp - 1;

        for(;iterations < max_iterations; iterations++) {
            
            re_sqr = (z_real * z_real) >> exp;
            im_sqr = (z_imag * z_imag) >> exp;
            
            
            if((re_sqr + im_sqr) >= bail) {
                return iterations;
            }
            
           
            temp = re_sqr - im_sqr + real;
            z_imag = ((z_real * z_imag) >> exp_1)  + imag;
            z_real = temp;
            
          
                
            
        }
        
        return max_iterations;*/

    }
    
    /*
     int iterations = 0; 
        
        int exp = 60;
        long  dx = (long)Math.pow(2, exp);//(long)(Math.pow(2, 16) / (size) + 0.5);
        
          
        BigInteger real = new BigInteger("" + (long)(pixel.getRe() * dx + 0.5));
        BigInteger imag = new BigInteger("" + (long)(pixel.getIm() * dx + 0.5));

        BigInteger z_real = real;
        BigInteger z_imag = imag;
        BigInteger temp;
        
        BigInteger re_sqr;
        BigInteger im_sqr;
        
        BigInteger bail = new BigInteger("" + (dx << 2));

        for(;iterations < max_iterations; iterations++) {
            
            re_sqr = z_real.multiply(z_real).shiftRight(exp);
            im_sqr = z_imag.multiply(z_imag).shiftRight(exp);
            
            
            if(re_sqr.add(im_sqr).compareTo(bail) >= 0) {
                return iterations;
            }
            
            temp = re_sqr.subtract(im_sqr).add(real);
            z_imag = z_real.multiply(z_imag).shiftRight(exp - 1).add(imag);
            z_real = temp;
            
          
                
            
        }
        
        return max_iterations;
     */
  
    public void calculateFractalOrbit() {
      int iterations = 0;

        Complex[] complex = new Complex[2];
        complex[0] = init_val.getPixel(pixel_orbit);//z
        complex[1] = pixel_orbit;//c
        

        for (; iterations < max_iterations; iterations++) {
           function(complex);
           complex_orbit.add(rotation.getPixel(complex[0], true));
           //if(z.getRe() >= (xCenter + size) / 2 || z.getRe() <= (xCenter - size) / 2 || z.getIm() >= (yCenter + size) / 2 || z.getIm() <= (yCenter - size) / 2) {
               //return;  //keep only the visible ones
           //}
        }

    }

    protected abstract void calculateJuliaOrbit();
    
    protected abstract double calculateJulia(Complex pixel);

    protected double getXCenter() {

        return xCenter;

    }

    protected double getYCenter() {

        return yCenter;

    }

    protected double getSize() {

        return size;

    }

    protected int getMaxIterations() {

        return max_iterations;

    }
 

}
