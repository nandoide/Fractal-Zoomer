
package fractalzoomer.core.interpolation;

import fractalzoomer.main.Constants;
import fractalzoomer.main.MainWindow;

import java.awt.*;

/**
 *
 * @author hrkalona2
 */
public abstract class InterpolationMethod {
    protected int color_space;
    
    protected InterpolationMethod() {
        color_space = Constants.COLOR_SPACE_RGB;
    }

    public void setColorSpace(int color_space) {
        this.color_space = color_space;
    }

    public static InterpolationMethod create(int method) {
        switch(method) {
            case MainWindow.INTERPOLATION_LINEAR:
                return new LinearInterpolation();
            case MainWindow.INTERPOLATION_COSINE:
                return new CosineInterpolation();
            case MainWindow.INTERPOLATION_ACCELERATION:
                return new AccelerationInterpolation();
            case MainWindow.INTERPOLATION_DECELERATION:
                return new DecelerationInterpolation();
            case MainWindow.INTERPOLATION_EXPONENTIAL:
                return new ExponentialInterpolation();
            case MainWindow.INTERPOLATION_CATMULLROM:
                return new CatmullRomInterpolation();
            case MainWindow.INTERPOLATION_CATMULLROM2:
                return new CatmullRom2Interpolation();
            case MainWindow.INTERPOLATION_SIGMOID:
                return new SigmoidInterpolation();
            case MainWindow.INTERPOLATION_QUADRATIC:
                return new QuadraticInterpolation();
            case MainWindow.INTERPOLATION_CUBIC:
                return new CubicInterpolation();
            case MainWindow.INTERPOLATION_SQRT_VALUES:
                return new SqrtValuesInterpolation();
            case MainWindow.INTERPOLATION_SINE:
                return new SineInterpolation();
            case MainWindow.INTERPOLATION_SQRT:
                return new SqrtInterpolation();
            case MainWindow.INTERPOLATION_THIRD_POLY:
                return new ThirdPolynomialInterpolation();
            case MainWindow.INTERPOLATION_FIFTH_POLY:
                return new FifthPolynomialInterpolation();
            case MainWindow.INTERPOLATION_EXPONENTIAL_2:
                return new Exponential2Interpolation();
            case MainWindow.INTERPOLATION_CUBE_ROOT:
                return new CbrtInterpolation();
            case MainWindow.INTERPOLATION_FOURTH_ROOT:
                return new FrthrootInterpolation();
            case MainWindow.INTERPOLATION_SMOOTH_TRANSITION_STEP:
                return new SmoothTransitionFunctionInterpolation();
            case MainWindow.INTERPOLATION_QUARTER_SIN:
                return new QuarterSinInterpolation();
        }

        return null;
    }

    public int interpolateColors(int r1, int g1, int b1, int r2, int g2, int b2, double coef) {
        if(color_space == Constants.COLOR_SPACE_RGB) {
            return interpolateColorsInternal(r1, g1, b1, r2, g2, b2, coef);
        }
        int[] c1 = new int[] {0, r1, g1, b1};
        int[] c2 = new int[] {0, r2, g2, b2};
        return interpolateColors(color_space, this, coef, 0, c1, c2);
    }
    
    protected abstract int interpolateColorsInternal(int r1, int g1, int b1, int r2, int g2, int b2, double coef);
    public abstract double interpolate(double a, double b, double coef);
    public abstract int interpolate(int a, int b, double coef);


    public Color interpolateColors(Color c1, Color c2, double coef) {

        return new Color(interpolateColorsInternal(c1.getRed(), c1.getGreen(), c1.getBlue(), c2.getRed(), c2.getGreen(), c2.getBlue(), coef));

    }

    public abstract double getCoef(double coef);

    public static int interpolateColors(int color_space, InterpolationMethod method, double coef, int i, int[] c1, int[] c2) {
        if (color_space == MainWindow.COLOR_SPACE_HSB || color_space == MainWindow.COLOR_SPACE_HSB_LONG) {
            return ColorSpaceInterpolation.HSBInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_HSB);
        } else if (color_space == MainWindow.COLOR_SPACE_HSL || color_space == MainWindow.COLOR_SPACE_HSL_LONG) {
            return ColorSpaceInterpolation.HSLInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_HSL);
        }
        else if(color_space == MainWindow.COLOR_SPACE_CUBEHELIX || color_space == MainWindow.COLOR_SPACE_CUBEHELIX_LONG) {
            return ColorSpaceInterpolation.CubehelixInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_CUBEHELIX);
        }
        else if (color_space == MainWindow.COLOR_SPACE_RGB) {
            return ColorSpaceInterpolation.RGBInterpolation(method, coef, c1, c2);
            //System.out.print((0xff000000 | (red << 16) | (green << 8) | blue) + ", ");
        } else if (color_space == MainWindow.COLOR_SPACE_EXP) {
            return ColorSpaceInterpolation.ExpInterpolation(method, coef, c1, c2);
        } else if (color_space == MainWindow.COLOR_SPACE_SQUARE) {
            return ColorSpaceInterpolation.SquareInterpolation(method, coef, c1, c2);
        } else if (color_space == MainWindow.COLOR_SPACE_SQRT) {
            return ColorSpaceInterpolation.SqrtInterpolation(method, coef, c1, c2);
        }
        else if (color_space == MainWindow.COLOR_SPACE_YCBCR) {
            return ColorSpaceInterpolation.YCbCrInterpolation(method, coef, c1, c2);
        }
        else if (color_space == MainWindow.COLOR_SPACE_RYB) {
            return ColorSpaceInterpolation.RYBInterpolation(method, coef, c1, c2);
        } else if (color_space == MainWindow.COLOR_SPACE_LAB) {
            return ColorSpaceInterpolation.LABInterpolation(method, coef, c1, c2);
        } else if (color_space == MainWindow.COLOR_SPACE_XYZ) {
            return ColorSpaceInterpolation.XYZInterpolation(method, coef, c1, c2);
        } else if (color_space == MainWindow.COLOR_SPACE_LCH_ab || color_space == MainWindow.COLOR_SPACE_LCH_ab_LONG) {
            return ColorSpaceInterpolation.LCHabInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_LCH_ab);
        } else if (color_space == MainWindow.COLOR_SPACE_BEZIER_RGB) {
            return ColorSpaceInterpolation.BezierRGBInterpolation(method, i, coef, c1, c2);
        }
        else if (color_space == MainWindow.COLOR_SPACE_LUV) {
            return ColorSpaceInterpolation.LUVInterpolation(method, coef, c1, c2);
        }
        else if (color_space == MainWindow.COLOR_SPACE_LCH_uv || color_space == MainWindow.COLOR_SPACE_LCH_uv_LONG) {
            return ColorSpaceInterpolation.LCHuvInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_LCH_uv);
        }
        else if (color_space == MainWindow.COLOR_SPACE_OKLAB) {
            return ColorSpaceInterpolation.OKLABInterpolation(method, coef, c1, c2);
        }
        else if (color_space == MainWindow.COLOR_SPACE_LCH_oklab || color_space == MainWindow.COLOR_SPACE_LCH_oklab_LONG) {
            return ColorSpaceInterpolation.LCHOklabInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_LCH_oklab);
        }
        else if (color_space == MainWindow.COLOR_SPACE_JZAZBZ) {
            return ColorSpaceInterpolation.JzAzBzInterpolation(method, coef, c1, c2);
        }
        else if (color_space == MainWindow.COLOR_SPACE_LCH_JzAzBz || color_space == MainWindow.COLOR_SPACE_LCH_JzAzBz_LONG) {
            return ColorSpaceInterpolation.LCHJzAzBzInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_LCH_JzAzBz);
        }
        else if (color_space == MainWindow.COLOR_SPACE_HSL_uv || color_space == MainWindow.COLOR_SPACE_HSL_uv_LONG) {
            return ColorSpaceInterpolation.HSLuvInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_HSL_uv);
        }
        else if (color_space == MainWindow.COLOR_SPACE_HPL_uv || color_space == MainWindow.COLOR_SPACE_HPL_uv_LONG) {
            return ColorSpaceInterpolation.HPLuvInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_HPL_uv);
        }
        else if (color_space == MainWindow.COLOR_SPACE_HWB || color_space == MainWindow.COLOR_SPACE_HWB_LONG) {
            return ColorSpaceInterpolation.HWBInterpolation(method, coef, c1, c2, color_space == MainWindow.COLOR_SPACE_HWB);
        }
        else if (color_space == MainWindow.COLOR_SPACE_LINEAR_RGB) {
            return ColorSpaceInterpolation.LinearRGBInterpolation(method, coef, c1, c2);
        }
        return 0xff000000;
    }
}
