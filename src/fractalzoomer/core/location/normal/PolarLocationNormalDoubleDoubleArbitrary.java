package fractalzoomer.core.location.normal;

import fractalzoomer.core.DDComplex;
import fractalzoomer.core.DoubleDouble;
import fractalzoomer.core.GenericComplex;
import fractalzoomer.core.location.Location;
import fractalzoomer.fractal_options.Rotation;
import fractalzoomer.functions.Fractal;
import fractalzoomer.main.app_settings.JitterSettings;
import org.apfloat.Apfloat;

public class PolarLocationNormalDoubleDoubleArbitrary extends Location {
    protected Rotation rotation;

    protected int image_size;

    protected DoubleDouble ddxcenter;
    protected DoubleDouble ddycenter;

    private DoubleDouble[] ddantialiasing_x;

    private DoubleDouble ddmuly;
    protected DoubleDouble ddmulx;
    private DoubleDouble ddstart;
    protected DoubleDouble ddcenter;
    private DoubleDouble[] ddantialiasing_y_sin;
    private DoubleDouble[] ddantialiasing_y_cos;


    //Dont copy those
    private DoubleDouble temp_ddsf;
    private DoubleDouble temp_ddcf;
    private DoubleDouble temp_ddr;

    private DoubleDouble ddemulx;
    private DoubleDouble ddInvemulx;

    private DoubleDouble ddcosmuly;
    private DoubleDouble ddsinmuly;

    private JitterSettings js;

    public PolarLocationNormalDoubleDoubleArbitrary(Apfloat xCenter, Apfloat yCenter, Apfloat size, double height_ratio, int image_size, double circle_period, Apfloat[] rotation_center, Apfloat[] rotation_vals, Fractal fractal, JitterSettings js) {

        super();

        this.fractal = fractal;
        this.image_size = image_size;

        ddxcenter = new DoubleDouble(xCenter);
        ddycenter = new DoubleDouble(yCenter);

        ddcenter = new DoubleDouble(size).log();


        ddmuly =  DoubleDouble.PI.multiply(2.0 * circle_period).divide(image_size);


        ddmulx = ddmuly.multiply(height_ratio);

        ddstart = ddcenter.subtract(ddmulx.multiply(image_size).multiply(0.5));

        rotation = new Rotation(new DDComplex(rotation_vals[0], rotation_vals[1]), new DDComplex(rotation_center[0], rotation_center[1]));

        ddemulx = ddmulx.exp();
        ddInvemulx = ddemulx.reciprocal();

        ddcosmuly = ddmuly.cos();
        ddsinmuly = ddmuly.sin();

        this.js = js;
    }

    public PolarLocationNormalDoubleDoubleArbitrary(PolarLocationNormalDoubleDoubleArbitrary other) {

        super();

        fractal = other.fractal;

        ddcenter = other.ddcenter;

        ddxcenter = other.ddxcenter;
        ddycenter = other.ddycenter;

        ddmulx = other.ddmulx;
        ddmuly = other.ddmuly;
        ddstart = other.ddstart;

        ddemulx = other.ddemulx;
        ddInvemulx = other.ddInvemulx;
        ddcosmuly = other.ddcosmuly;
        ddsinmuly = other.ddsinmuly;

        image_size = other.image_size;

        rotation = other.rotation;

        ddantialiasing_y_cos = other.ddantialiasing_y_cos;
        ddantialiasing_y_sin = other.ddantialiasing_y_sin;
        ddantialiasing_x = other.ddantialiasing_x;

        js = other.js;

    }

    @Override
    public GenericComplex getComplex(int x, int y) {
        return getComplexBase(x, y);
    }

    protected DDComplex getComplexBase(int x, int y) {

        if(js.enableJitter) {
            double[] res = GetPixelOffset(y, x, js.jitterSeed, js.jitterShape, js.jitterScale);

            temp_ddr = ddstart.add(ddmulx.multiply(x + res[1])).exp();

            DoubleDouble f = ddmuly.multiply(y + res[0]);
            temp_ddsf = f.sin();
            temp_ddcf = f.cos();
        }
        else {
            if (x == indexX + 1) {
                temp_ddr = temp_ddr.multiply(ddemulx);
            } else if (x == indexX - 1) {
                temp_ddr = temp_ddr.multiply(ddInvemulx);
            } else {
                temp_ddr = ddstart.add(ddmulx.multiply(x)).exp();
            }

            if (y == indexY + 1) {
                DoubleDouble tempSin = temp_ddsf.multiply(ddcosmuly).add(temp_ddcf.multiply(ddsinmuly));
                DoubleDouble tempCos = temp_ddcf.multiply(ddcosmuly).subtract(temp_ddsf.multiply(ddsinmuly));

                temp_ddsf = tempSin;
                temp_ddcf = tempCos;
            } else if (y == indexY - 1) {
                DoubleDouble tempSin = temp_ddsf.multiply(ddcosmuly).subtract(temp_ddcf.multiply(ddsinmuly));
                DoubleDouble tempCos = temp_ddcf.multiply(ddcosmuly).add(temp_ddsf.multiply(ddsinmuly));

                temp_ddsf = tempSin;
                temp_ddcf = tempCos;
            } else {
                DoubleDouble f = ddmuly.multiply(y);
                temp_ddsf = f.sin();
                temp_ddcf = f.cos();
            }
        }

        indexX = x;
        indexY = y;

        //As alternative as it works with low prec as well sin/cos

        DDComplex temp = new DDComplex(ddxcenter.add(temp_ddr.multiply(temp_ddcf)), ddycenter.add(temp_ddr.multiply(temp_ddsf)));

        temp = rotation.rotate(temp);

        temp = fractal.getPlaneTransformedPixel(temp);

        return temp;
    }

    @Override
    public void precalculateY(int y) {

        if(!js.enableJitter) {
            if (y == indexY + 1) {
                DoubleDouble tempSin = temp_ddsf.multiply(ddcosmuly).add(temp_ddcf.multiply(ddsinmuly));
                DoubleDouble tempCos = temp_ddcf.multiply(ddcosmuly).subtract(temp_ddsf.multiply(ddsinmuly));

                temp_ddsf = tempSin;
                temp_ddcf = tempCos;
            } else if (y == indexY - 1) {
                DoubleDouble tempSin = temp_ddsf.multiply(ddcosmuly).subtract(temp_ddcf.multiply(ddsinmuly));
                DoubleDouble tempCos = temp_ddcf.multiply(ddcosmuly).add(temp_ddsf.multiply(ddsinmuly));

                temp_ddsf = tempSin;
                temp_ddcf = tempCos;
            } else {
                DoubleDouble f = ddmuly.multiply(y);
                temp_ddsf = f.sin();
                temp_ddcf = f.cos();
            }
        }

        indexY = y;

    }

    @Override
    public void precalculateX(int x) {

        if(!js.enableJitter) {
            if (x == indexX + 1) {
                temp_ddr = temp_ddr.multiply(ddemulx);
            } else if (x == indexX - 1) {
                temp_ddr = temp_ddr.multiply(ddInvemulx);
            } else {
                temp_ddr = ddstart.add(ddmulx.multiply(x)).exp();
            }
        }

        indexX = x;

    }

    @Override
    public GenericComplex getComplexWithX(int x) {
        return getComplexWithXBase(x);
    }

    protected DDComplex getComplexWithXBase(int x) {

        if(js.enableJitter) {
            return getComplexBase(x, indexY);
        }

        if (x == indexX + 1) {
            temp_ddr = temp_ddr.multiply(ddemulx);
        } else if (x == indexX - 1) {
            temp_ddr = temp_ddr.multiply(ddInvemulx);
        } else {
            temp_ddr = ddstart.add(ddmulx.multiply(x)).exp();
        }

        indexX = x;

        DDComplex temp = new DDComplex(ddxcenter.add(temp_ddr.multiply(temp_ddcf)), ddycenter.add(temp_ddr.multiply(temp_ddsf)));

        temp = rotation.rotate(temp);

        temp = fractal.getPlaneTransformedPixel(temp);

        return temp;
    }

    @Override
    public boolean isPolar() {return true;}

    @Override
    public GenericComplex getComplexWithY(int y) {
        return getComplexWithYBase(y);
    }

    protected DDComplex getComplexWithYBase(int y) {

        if(js.enableJitter) {
            return getComplexBase(indexX, y);
        }

        if (y == indexY + 1) {
            DoubleDouble tempSin = temp_ddsf.multiply(ddcosmuly).add(temp_ddcf.multiply(ddsinmuly));
            DoubleDouble tempCos = temp_ddcf.multiply(ddcosmuly).subtract(temp_ddsf.multiply(ddsinmuly));

            temp_ddsf = tempSin;
            temp_ddcf = tempCos;
        } else if (y == indexY - 1) {
            DoubleDouble tempSin = temp_ddsf.multiply(ddcosmuly).subtract(temp_ddcf.multiply(ddsinmuly));
            DoubleDouble tempCos = temp_ddcf.multiply(ddcosmuly).add(temp_ddsf.multiply(ddsinmuly));

            temp_ddsf = tempSin;
            temp_ddcf = tempCos;
        } else {
            DoubleDouble f = ddmuly.multiply(y);
            temp_ddsf = f.sin();
            temp_ddcf = f.cos();
        }

        indexY = y;

        DDComplex temp = new DDComplex(ddxcenter.add(temp_ddr.multiply(temp_ddcf)), ddycenter.add(temp_ddr.multiply(temp_ddsf)));

        temp = rotation.rotate(temp);

        temp = fractal.getPlaneTransformedPixel(temp);

        return temp;
    }

    @Override
    public void createAntialiasingSteps(boolean adaptive) {
        DoubleDouble[][] steps = createAntialiasingPolarStepsDoubleDouble(ddmulx, ddmuly, adaptive);
        ddantialiasing_x = steps[0];
        ddantialiasing_y_sin = steps[1];
        ddantialiasing_y_cos = steps[2];
    }

    @Override
    public GenericComplex getAntialiasingComplex(int sample) {
        return getAntialiasingComplexBase(sample);
    }

    protected DDComplex getAntialiasingComplexBase(int sample) {

        DoubleDouble ddsf2 = temp_ddsf.multiply(ddantialiasing_y_cos[sample]).add(temp_ddcf.multiply(ddantialiasing_y_sin[sample]));
        DoubleDouble ddcf2 = temp_ddcf.multiply(ddantialiasing_y_cos[sample]).subtract(temp_ddsf.multiply(ddantialiasing_y_sin[sample]));

        DoubleDouble ddr2 = temp_ddr.multiply(ddantialiasing_x[sample]);

        DDComplex temp = new DDComplex(ddxcenter.add(ddr2.multiply(ddcf2)), ddycenter.add(ddr2.multiply(ddsf2)));

        temp = rotation.rotate(temp);

        temp = fractal.getPlaneTransformedPixel(temp);

        return temp;
    }
}
