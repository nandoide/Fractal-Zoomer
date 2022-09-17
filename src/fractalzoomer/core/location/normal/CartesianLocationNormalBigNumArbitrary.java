package fractalzoomer.core.location.normal;

import fractalzoomer.core.BigNum;
import fractalzoomer.core.BigNumComplex;
import fractalzoomer.core.GenericComplex;
import fractalzoomer.core.MyApfloat;
import fractalzoomer.core.location.Location;
import fractalzoomer.fractal_options.Rotation;
import fractalzoomer.functions.Fractal;
import fractalzoomer.main.app_settings.JitterSettings;
import org.apfloat.Apfloat;

public class CartesianLocationNormalBigNumArbitrary extends Location {
    protected BigNum ddxcenter;
    protected BigNum ddycenter;
    private BigNum bntemp_size_image_size_x;
    private BigNum bntemp_size_image_size_y;
    private BigNum bntemp_xcenter_size;
    private BigNum bntemp_ycenter_size;

    protected Rotation rotation;

    private BigNum[] bnantialiasing_y;
    private BigNum[] bnantialiasing_x;

    //Dont copy those
    private BigNum bntempX;
    private BigNum bntempY;

    private BigNum bnsize;
    protected Apfloat ddsize;

    protected Apfloat height_ratio;

    protected Apfloat point5;

    private JitterSettings js;

    public CartesianLocationNormalBigNumArbitrary(Apfloat xCenter, Apfloat yCenter, Apfloat size, double height_ratio, int image_size, Apfloat[] rotation_center, Apfloat[] rotation_vals, Fractal fractal, JitterSettings js) {

        super();

        this.fractal = fractal;

        this.height_ratio = new MyApfloat(height_ratio);

        point5 = new MyApfloat(0.5);
        Apfloat size_2_x = MyApfloat.fp.multiply(size, point5);
        Apfloat ddimage_size = new MyApfloat(image_size);

        bnsize = new BigNum(size);
        ddsize = size;

        ddxcenter = new BigNum(xCenter);
        ddycenter = new BigNum(yCenter);

        Apfloat temp = MyApfloat.fp.multiply(size, this.height_ratio);
        Apfloat size_2_y = MyApfloat.fp.multiply(temp, point5);
        bntemp_size_image_size_x = new BigNum(MyApfloat.fp.divide(size, ddimage_size));
        bntemp_size_image_size_y = new BigNum(MyApfloat.fp.divide(temp, ddimage_size));

        bntemp_xcenter_size = new BigNum(MyApfloat.fp.subtract(xCenter, size_2_x));
        bntemp_ycenter_size =  new BigNum(MyApfloat.fp.add(yCenter, size_2_y));

        rotation = new Rotation(new BigNumComplex(rotation_vals[0], rotation_vals[1]), new BigNumComplex(rotation_center[0], rotation_center[1]));
        this.js = js;

    }

    public CartesianLocationNormalBigNumArbitrary(CartesianLocationNormalBigNumArbitrary other) {

        super();

        fractal = other.fractal;

        bnsize = other.bnsize;
        ddsize = other.ddsize;
        height_ratio = other.height_ratio;

        ddxcenter = other.ddxcenter;
        ddycenter = other.ddycenter;

        bntemp_size_image_size_x = other.bntemp_size_image_size_x;
        bntemp_size_image_size_y = other.bntemp_size_image_size_y;
        bntemp_xcenter_size = other.bntemp_xcenter_size;
        bntemp_ycenter_size = other.bntemp_ycenter_size;
        rotation = other.rotation;

        bnantialiasing_x = other.bnantialiasing_x;
        bnantialiasing_y = other.bnantialiasing_y;

        point5 = other.point5;
        js = other.js;

    }

    @Override
    public GenericComplex getComplex(int x, int y) {
        return getComplexBase(x, y);
    }

    protected BigNumComplex getComplexBase(int x, int y) {

        if(js.enableJitter) {
            double[] res = GetPixelOffset(y, x, js.jitterSeed, js.jitterShape, js.jitterScale);
            bntempX = bntemp_xcenter_size.add(bntemp_size_image_size_x.mult(new BigNum(x + res[1])));
            bntempY = bntemp_ycenter_size.sub(bntemp_size_image_size_y.mult(new BigNum(y + res[0])));
        }
        else {
            if (x == indexX + 1) {
                bntempX = bntempX.add(bntemp_size_image_size_x);
            } else if (x == indexX - 1) {
                bntempX = bntempX.sub(bntemp_size_image_size_x);
            } else {
                bntempX = bntemp_xcenter_size.add(bntemp_size_image_size_x.mult(x));
            }

            if (y == indexY + 1) {
                bntempY = bntempY.sub(bntemp_size_image_size_y);
            } else if (y == indexY - 1) {
                bntempY = bntempY.add(bntemp_size_image_size_y);
            } else {
                bntempY = bntemp_ycenter_size.sub(bntemp_size_image_size_y.mult(y));
            }
        }

        indexX = x;
        indexY = y;

        BigNumComplex temp = new BigNumComplex(bntempX, bntempY);

        temp = rotation.rotate(temp);

        temp = fractal.getPlaneTransformedPixel(temp);

        return temp;
    }

    @Override
    public void precalculateY(int y) {

        if(!js.enableJitter) {
            if (y == indexY + 1) {
                bntempY = bntempY.sub(bntemp_size_image_size_y);
            } else if (y == indexY - 1) {
                bntempY = bntempY.add(bntemp_size_image_size_y);
            } else {
                bntempY = bntemp_ycenter_size.sub(bntemp_size_image_size_y.mult(y));
            }
        }

        indexY = y;

    }

    @Override
    public void precalculateX(int x) {

        if(!js.enableJitter) {
            if (x == indexX + 1) {
                bntempX = bntempX.add(bntemp_size_image_size_x);
            } else if (x == indexX - 1) {
                bntempX = bntempX.sub(bntemp_size_image_size_x);
            } else {
                bntempX = bntemp_xcenter_size.add(bntemp_size_image_size_x.mult(x));
            }
        }

        indexX = x;

    }

    @Override
    public GenericComplex getComplexWithX(int x) {
        return getComplexWithXBase(x);
    }

    protected BigNumComplex getComplexWithXBase(int x) {

        if(js.enableJitter) {
            return getComplexBase(x, indexY);
        }

        if(x == indexX + 1) {
            bntempX = bntempX.add(bntemp_size_image_size_x);
        }
        else if(x == indexX - 1) {
            bntempX = bntempX.sub(bntemp_size_image_size_x);
        }
        else {
            bntempX = bntemp_xcenter_size.add(bntemp_size_image_size_x.mult(x));
        }

        indexX = x;

        BigNumComplex temp = new BigNumComplex(bntempX, bntempY);
        temp = rotation.rotate(temp);
        temp = fractal.getPlaneTransformedPixel(temp);
        return temp;
    }

    @Override
    public GenericComplex getComplexWithY(int y) {
        return getComplexWithYBase(y);
    }

    protected BigNumComplex getComplexWithYBase(int y) {

        if(js.enableJitter) {
            return getComplexBase(indexX, y);
        }

        if(y == indexY + 1) {
            bntempY = bntempY.sub(bntemp_size_image_size_y);
        }
        else if(y == indexY - 1) {
            bntempY = bntempY.add(bntemp_size_image_size_y);
        }
        else {
            bntempY = bntemp_ycenter_size.sub(bntemp_size_image_size_y.mult(y));
        }

        indexY = y;

        BigNumComplex temp = new BigNumComplex(bntempX, bntempY);
        temp = rotation.rotate(temp);
        temp = fractal.getPlaneTransformedPixel(temp);
        return temp;
    }

    @Override
    public void createAntialiasingSteps(boolean adaptive) {
        BigNum[][] steps = createAntialiasingStepsBigNum(bntemp_size_image_size_x, bntemp_size_image_size_y, adaptive);
        bnantialiasing_x = steps[0];
        bnantialiasing_y = steps[1];
    }

    @Override
    public GenericComplex getAntialiasingComplex(int sample) {
        return getAntialiasingComplexBase(sample);
    }

    protected BigNumComplex getAntialiasingComplexBase(int sample) {
        BigNumComplex temp = new BigNumComplex(bntempX.add(bnantialiasing_x[sample]), bntempY.add(bnantialiasing_y[sample]));

        temp = rotation.rotate(temp);

        temp = fractal.getPlaneTransformedPixel(temp);

        return temp;
    }
}
