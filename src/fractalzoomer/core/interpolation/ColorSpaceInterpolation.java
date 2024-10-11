package fractalzoomer.core.interpolation;

import fractalzoomer.utils.ColorSpaceConverter;
import fractalzoomer.utils.MathUtils;

import java.awt.geom.Point2D;

public class ColorSpaceInterpolation {

    private static Point2D.Double[][] bezierControlPoints_red;
    private static Point2D.Double[][] bezierControlPoints_green;
    private static Point2D.Double[][] bezierControlPoints_blue;

    public static int HSBInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        double[] c1_hsb = ColorSpaceConverter.RGBtoHSB(c1[1], c1[2], c1[3]);
        double[] c2_hsb = ColorSpaceConverter.RGBtoHSB(c2[1], c2[2], c2[3]);

        double h;
        double s = method.interpolate(c1_hsb[1], c2_hsb[1], coef);
        double b = method.interpolate(c1_hsb[2], c2_hsb[2], coef);

        double d = c2_hsb[0] - c1_hsb[0];

        double temp;
        if (c1_hsb[0] > c2_hsb[0]) {
            temp = c1_hsb[0];
            c1_hsb[0] = c2_hsb[0];
            c2_hsb[0] = temp;
            d = -d;
            coef = 1 - coef;
        }

        boolean condition = shortPath ? d > 0.5 : d <= 0.5;
        if (condition) {
            c1_hsb[0] += 1;
            h = method.interpolate(c1_hsb[0], c2_hsb[0], coef) % 1.0;
        } else {
            h = method.interpolate(c1_hsb[0], c2_hsb[0], coef);
        }

        int[] res = ColorSpaceConverter.HSBtoRGB(h, s, b);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int HSLInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        //Todo check those
        //https://github.com/d3/d3-color/blob/main/src/cubehelix.js
        //https://github.com/d3/d3-interpolate/blob/main/src/cubehelix.js
        //https://davidjohnstone.net/lch-lab-colour-gradient-picker
        double[] c1_hsl = ColorSpaceConverter.RGBtoHSL(c1[1], c1[2], c1[3]);
        double[] c2_hsl = ColorSpaceConverter.RGBtoHSL(c2[1], c2[2], c2[3]);

        double h;
        double s = method.interpolate(c1_hsl[1], c2_hsl[1], coef);
        double l = method.interpolate(c1_hsl[2], c2_hsl[2], coef);

        double d = c2_hsl[0] - c1_hsl[0];

        double temp;
        if (c1_hsl[0] > c2_hsl[0]) {
            temp = c1_hsl[0];
            c1_hsl[0] = c2_hsl[0];
            c2_hsl[0] = temp;
            d = -d;
            coef = 1 - coef;
        }

        boolean condition = shortPath ? d > 0.5 : d <= 0.5;
        if (condition) {
            c1_hsl[0] += 1;
            h = method.interpolate(c1_hsl[0], c2_hsl[0], coef) % 1.0;
        } else {
            h = method.interpolate(c1_hsl[0], c2_hsl[0], coef);
        }

        int[] res = ColorSpaceConverter.HSLtoRGB(h, s, l);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return 0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int CubehelixInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        double[] c1_hsl = ColorSpaceConverter.RGBtoCubehelix(c1[1], c1[2], c1[3]);
        double[] c2_hsl = ColorSpaceConverter.RGBtoCubehelix(c2[1], c2[2], c2[3]);

        double h = Double.NaN;
        double s = Double.NaN;
        double l = method.interpolate(c1_hsl[2], c2_hsl[2], coef);

        if(!Double.isNaN(c2_hsl[1]) && !Double.isNaN(c1_hsl[1])) {
            s = method.interpolate(c1_hsl[1], c2_hsl[1], coef);
        }
        else if(!Double.isNaN(c2_hsl[1])) {
            s = c2_hsl[1];
        }
        else if(!Double.isNaN(c1_hsl[1])) {
            s = c1_hsl[1];
        }

        if(!Double.isNaN(c2_hsl[0]) && !Double.isNaN(c1_hsl[0])) {
            double d = c2_hsl[0] - c1_hsl[0];

            double temp;
            if (c1_hsl[0] > c2_hsl[0]) {
                temp = c1_hsl[0];
                c1_hsl[0] = c2_hsl[0];
                c2_hsl[0] = temp;
                d = -d;
                coef = 1 - coef;
            }

            boolean condition = shortPath ? d > 180 : d <= 180;

            if (condition) {
                c1_hsl[0] += 360;
                h = method.interpolate(c1_hsl[0], c2_hsl[0], coef) % 360.0;
            } else {
                h = method.interpolate(c1_hsl[0], c2_hsl[0], coef);
            }
        }
        else if(!Double.isNaN(c2_hsl[0])) {
            h = c2_hsl[0];
        }
        else if(!Double.isNaN(c1_hsl[0])) {
            h = c1_hsl[0];
        }

        int[] res = ColorSpaceConverter.CubehelixtoRGB(h, s, l);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int RGBInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        return method.interpolateColorsInternal(c1[1], c1[2], c1[3], c2[1], c2[2], c2[3], coef);
    }

    public static int ExpInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double c2_1 = c2[1] == 0 ? c2[1] + 1 : c2[1];
        double c2_2 = c2[2] == 0 ? c2[2] + 1 : c2[2];
        double c2_3 = c2[3] == 0 ? c2[3] + 1 : c2[3];

        double c1_1 = c1[1] == 0 ? c1[1] + 1 : c1[1];
        double c1_2 = c1[2] == 0 ? c1[2] + 1 : c1[2];
        double c1_3 = c1[3] == 0 ? c1[3] + 1 : c1[3];

        double to_r = Math.log(c2_1);
        double from_r = Math.log(c1_1);

        double to_g = Math.log(c2_2);
        double from_g = Math.log(c1_2);

        double to_b = Math.log(c2_3);
        double from_b = Math.log(c1_3);

        int red = (int) (Math.exp(method.interpolate(from_r, to_r, coef)));
        int green = (int) (Math.exp(method.interpolate(from_g, to_g, coef)));
        int blue = (int) (Math.exp(method.interpolate(from_b, to_b, coef)));

        return  0xff000000 | (red << 16) | (green << 8) | blue;
    }

    public static int SquareInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double to_r = Math.sqrt(c2[1]);
        double from_r = Math.sqrt(c1[1]);

        double to_g = Math.sqrt(c2[2]);
        double from_g = Math.sqrt(c1[2]);

        double to_b = Math.sqrt(c2[3]);
        double from_b = Math.sqrt(c1[3]);

        int red = (int) (Math.pow(method.interpolate(from_r, to_r, coef), 2));
        int green = (int) (Math.pow(method.interpolate(from_g, to_g, coef), 2));
        int blue = (int) (Math.pow(method.interpolate(from_b, to_b, coef), 2));

        return 0xff000000 | (red << 16) | (green << 8) | blue;
    }

    public static int SqrtInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double to_r = c2[1] * c2[1];
        double from_r = c1[1] * c1[1];

        double to_g = c2[2] * c2[2];
        double from_g = c1[2] * c1[2];

        double to_b = c2[3] * c2[3];
        double from_b = c1[3] * c1[3];

        int red = (int) (Math.sqrt(method.interpolate(from_r, to_r, coef)));
        int green = (int) (Math.sqrt(method.interpolate(from_g, to_g, coef)));
        int blue = (int) (Math.sqrt(method.interpolate(from_b, to_b, coef)));

        return 0xff000000 | (red << 16) | (green << 8) | blue;
    }

    public static int YCbCrInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        int[] ycbcr_from = ColorSpaceConverter.RGBtoYCbCr(c1[1], c1[2], c1[3]);
        int[] ycbcr_to = ColorSpaceConverter.RGBtoYCbCr(c2[1], c2[2], c2[3]);

        int y = method.interpolate(ycbcr_from[0], ycbcr_to[0], coef);
        int cb = method.interpolate(ycbcr_from[1], ycbcr_to[1], coef);
        int cr = method.interpolate(ycbcr_from[2], ycbcr_to[2], coef);

        int[] rgb = ColorSpaceConverter.YCbCrtoRGB(y, cb, cr);

        rgb[0] = ColorSpaceConverter.clamp(rgb[0]);
        rgb[1] = ColorSpaceConverter.clamp(rgb[1]);
        rgb[2] = ColorSpaceConverter.clamp(rgb[2]);

        return 0xff000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    public static int RYBInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double[] ryb_from = ColorSpaceConverter.RGBtoRYB(c1[1], c1[2], c1[3]);
        double[] ryb_to = ColorSpaceConverter.RGBtoRYB(c2[1], c2[2], c2[3]);

        double r = method.interpolate(ryb_from[0], ryb_to[0], coef);
        double y = method.interpolate(ryb_from[1], ryb_to[1], coef);
        double b = method.interpolate(ryb_from[2], ryb_to[2], coef);

        int[] rgb = ColorSpaceConverter.RYBtoRGB(r, y, b);

        rgb[0] = ColorSpaceConverter.clamp(rgb[0]);
        rgb[1] = ColorSpaceConverter.clamp(rgb[1]);
        rgb[2] = ColorSpaceConverter.clamp(rgb[2]);

        return 0xff000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    public static int LABInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double[] from = ColorSpaceConverter.RGBtoLAB(c1[1], c1[2], c1[3]);

        double[] to = ColorSpaceConverter.RGBtoLAB(c2[1], c2[2], c2[3]);

        double L = method.interpolate(from[0], to[0], coef);
        double a = method.interpolate(from[1], to[1], coef);
        double b = method.interpolate(from[2], to[2], coef);

        int[] res = ColorSpaceConverter.LABtoRGB(L, a, b);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return 0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int XYZInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double[] from = ColorSpaceConverter.RGBtoXYZ(c1[1], c1[2], c1[3]);

        double[] to = ColorSpaceConverter.RGBtoXYZ(c2[1], c2[2], c2[3]);

        double X = method.interpolate(from[0], to[0], coef);
        double Y = method.interpolate(from[1], to[1], coef);
        double Z = method.interpolate(from[2], to[2], coef);

        int[] res = ColorSpaceConverter.XYZtoRGB(X, Y, Z);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return 0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int LCHabInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        double[] from = ColorSpaceConverter.RGBtoLCH_ab(c1[1], c1[2], c1[3]);

        double[] to = ColorSpaceConverter.RGBtoLCH_ab(c2[1], c2[2], c2[3]);

        double L = method.interpolate(from[0], to[0], coef);
        double C = method.interpolate(from[1], to[1], coef);
        double H;

        double d = to[2] - from[2];

        double temp;
        if (from[2] > to[2]) {
            temp = from[2];
            from[2] = to[2];
            to[2] = temp;
            d = -d;
            coef = 1 - coef;
        }

        boolean condition = shortPath ? d > 180 : d <= 180;

        if (condition) {
            from[2] += 360;
            H = method.interpolate(from[2], to[2], coef) % 360.0;
        } else {
            H = method.interpolate(from[2], to[2], coef);
        }

        int[] res = ColorSpaceConverter.LCH_abtoRGB(L, C, H);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static void calculateBezierControlPoints(int[][] colors) {

        Point2D.Double[] knots_red = new Point2D.Double[colors.length + 1];
        Point2D.Double[] knots_green = new Point2D.Double[colors.length + 1];
        Point2D.Double[] knots_blue = new Point2D.Double[colors.length + 1]; // +1 to cycle the palette

        for (int i = 0; i < knots_red.length; i++) {
            knots_red[i] = new Point2D.Double(i, colors[i % colors.length][1]);
            knots_green[i] = new Point2D.Double(i, colors[i % colors.length][2]);
            knots_blue[i] = new Point2D.Double(i, colors[i % colors.length][3]);
        }

        bezierControlPoints_red = MathUtils.BezierSpline.GetCurveControlPoints(knots_red);
        bezierControlPoints_green = MathUtils.BezierSpline.GetCurveControlPoints(knots_green);
        bezierControlPoints_blue = MathUtils.BezierSpline.GetCurveControlPoints(knots_blue);

    }

    private static double evaluateBezier(double t, double p0, double p1, double p2, double p3) {

        return (1 - t) * (1 - t) * (1 - t) * p0 + 3 * (1 - t) * (1 - t) * t * p1 + 3 * (1 - t) * t * t * p2 + t * t * t * p3;

    }
    public static int BezierRGBInterpolation(InterpolationMethod method, int i, double coef, int[] c1, int[] c2) {
        double a = method.getCoef(coef);
        int red = ColorSpaceConverter.clamp((int) evaluateBezier(a, c1[1], bezierControlPoints_red[0][i].y, bezierControlPoints_red[1][i].y, c2[1]));
        int green = ColorSpaceConverter.clamp((int) evaluateBezier(a, c1[2], bezierControlPoints_green[0][i].y, bezierControlPoints_green[1][i].y, c2[2]));
        int blue = ColorSpaceConverter.clamp((int) evaluateBezier(a, c1[3], bezierControlPoints_blue[0][i].y, bezierControlPoints_blue[1][i].y, c2[3]));

        return  0xff000000 | (red << 16) | (green << 8) | blue;
    }

    public static int LUVInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double[] from = ColorSpaceConverter.RGBtoLUV(c1[1], c1[2], c1[3]);

        double[] to = ColorSpaceConverter.RGBtoLUV(c2[1], c2[2], c2[3]);

        double L = method.interpolate(from[0], to[0], coef);
        double a = method.interpolate(from[1], to[1], coef);
        double b = method.interpolate(from[2], to[2], coef);

        int[] res = ColorSpaceConverter.LUVtoRGB(L, a, b);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int LCHuvInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        double[] from = ColorSpaceConverter.RGBtoLCH_uv(c1[1], c1[2], c1[3]);

        double[] to = ColorSpaceConverter.RGBtoLCH_uv(c2[1], c2[2], c2[3]);

        double L = method.interpolate(from[0], to[0], coef);
        double C = method.interpolate(from[1], to[1], coef);
        double H;

        double d = to[2] - from[2];

        double temp;
        if (from[2] > to[2]) {
            temp = from[2];
            from[2] = to[2];
            to[2] = temp;
            d = -d;
            coef = 1 - coef;
        }

        boolean condition = shortPath ? d > 180 : d <= 180;

        if (condition) {
            from[2] += 360;
            H = method.interpolate(from[2], to[2], coef) % 360.0;
        } else {
            H = method.interpolate(from[2], to[2], coef);
        }

        int[] res = ColorSpaceConverter.LCH_uvtoRGB(L, C, H);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int OKLABInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double[] from = ColorSpaceConverter.RGBtoOKLAB(c1[1], c1[2], c1[3]);

        double[] to = ColorSpaceConverter.RGBtoOKLAB(c2[1], c2[2], c2[3]);

        double L = method.interpolate(from[0], to[0], coef);
        double a = method.interpolate(from[1], to[1], coef);
        double b = method.interpolate(from[2], to[2], coef);

        int[] res = ColorSpaceConverter.OKLABtoRGB(L, a, b);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int LinearRGBInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double[] ryb_from = ColorSpaceConverter.RGBtoLinearRGB(c1[1], c1[2], c1[3]);
        double[] ryb_to = ColorSpaceConverter.RGBtoLinearRGB(c2[1], c2[2], c2[3]);

        double r = method.interpolate(ryb_from[0], ryb_to[0], coef);
        double y = method.interpolate(ryb_from[1], ryb_to[1], coef);
        double b = method.interpolate(ryb_from[2], ryb_to[2], coef);

        int[] rgb = ColorSpaceConverter.LinearRGBtoRGB(r, y, b);

        rgb[0] = ColorSpaceConverter.clamp(rgb[0]);
        rgb[1] = ColorSpaceConverter.clamp(rgb[1]);
        rgb[2] = ColorSpaceConverter.clamp(rgb[2]);

        return  0xff000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    public static int HWBInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        double[] c1_hsb = ColorSpaceConverter.RGBtoHWB(c1[1], c1[2], c1[3]);
        double[] c2_hsb = ColorSpaceConverter.RGBtoHWB(c2[1], c2[2], c2[3]);

        double h;
        double s = method.interpolate(c1_hsb[1], c2_hsb[1], coef);
        double b = method.interpolate(c1_hsb[2], c2_hsb[2], coef);

        double d = c2_hsb[0] - c1_hsb[0];

        double temp;
        if (c1_hsb[0] > c2_hsb[0]) {
            temp = c1_hsb[0];
            c1_hsb[0] = c2_hsb[0];
            c2_hsb[0] = temp;
            d = -d;
            coef = 1 - coef;
        }

        boolean condition = shortPath ? d > 0.5 : d <= 0.5;

        if (condition) {
            c1_hsb[0] += 1;
            h = method.interpolate(c1_hsb[0], c2_hsb[0], coef) % 1.0;
        } else {
            h = method.interpolate(c1_hsb[0], c2_hsb[0], coef);
        }

        int[] res = ColorSpaceConverter.HWBtoRGB(h, s, b);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int HPLuvInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        double[] c1_hsl = ColorSpaceConverter.RGBtoHPL_uv(c1[1], c1[2], c1[3]);
        double[] c2_hsl = ColorSpaceConverter.RGBtoHPL_uv(c2[1], c2[2], c2[3]);

        double h;
        double s = method.interpolate(c1_hsl[1], c2_hsl[1], coef);
        double l = method.interpolate(c1_hsl[2], c2_hsl[2], coef);

        double d = c2_hsl[0] - c1_hsl[0];

        double temp;
        if (c1_hsl[0] > c2_hsl[0]) {
            temp = c1_hsl[0];
            c1_hsl[0] = c2_hsl[0];
            c2_hsl[0] = temp;
            d = -d;
            coef = 1 - coef;
        }

        boolean condition = shortPath ? d > 180 : d <= 180;

        if (condition) {
            c1_hsl[0] += 360;
            h = method.interpolate(c1_hsl[0], c2_hsl[0], coef) % 360.0;
        } else {
            h = method.interpolate(c1_hsl[0], c2_hsl[0], coef);
        }

        int[] res = ColorSpaceConverter.HPL_uvtoRGB(h, s, l);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int HSLuvInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        double[] c1_hsl = ColorSpaceConverter.RGBtoHSL_uv(c1[1], c1[2], c1[3]);
        double[] c2_hsl = ColorSpaceConverter.RGBtoHSL_uv(c2[1], c2[2], c2[3]);

        double h;
        double s = method.interpolate(c1_hsl[1], c2_hsl[1], coef);
        double l = method.interpolate(c1_hsl[2], c2_hsl[2], coef);

        double d = c2_hsl[0] - c1_hsl[0];

        double temp;
        if (c1_hsl[0] > c2_hsl[0]) {
            temp = c1_hsl[0];
            c1_hsl[0] = c2_hsl[0];
            c2_hsl[0] = temp;
            d = -d;
            coef = 1 - coef;
        }

        boolean condition = shortPath ? d > 180 : d <= 180;

        if (condition) {
            c1_hsl[0] += 360;
            h = method.interpolate(c1_hsl[0], c2_hsl[0], coef) % 360.0;
        } else {
            h = method.interpolate(c1_hsl[0], c2_hsl[0], coef);
        }

        int[] res = ColorSpaceConverter.HSL_uvtoRGB(h, s, l);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int LCHJzAzBzInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        double[] from = ColorSpaceConverter.RGBtoLCH_JzAzBz(c1[1], c1[2], c1[3]);

        double[] to = ColorSpaceConverter.RGBtoLCH_JzAzBz(c2[1], c2[2], c2[3]);

        double L = method.interpolate(from[0], to[0], coef);
        double C = method.interpolate(from[1], to[1], coef);
        double H;

        double d = to[2] - from[2];

        double temp;
        if (from[2] > to[2]) {
            temp = from[2];
            from[2] = to[2];
            to[2] = temp;
            d = -d;
            coef = 1 - coef;
        }

        boolean condition = shortPath ? d > 180 : d <= 180;

        if (condition) {
            from[2] += 360;
            H = method.interpolate(from[2], to[2], coef) % 360.0;
        } else {
            H = method.interpolate(from[2], to[2], coef);
        }

        int[] res = ColorSpaceConverter.LCH_JzAzBztoRGB(L, C, H);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int JzAzBzInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2) {
        double[] from = ColorSpaceConverter.RGBtoJzAzBz(c1[1], c1[2], c1[3]);

        double[] to = ColorSpaceConverter.RGBtoJzAzBz(c2[1], c2[2], c2[3]);

        double L = method.interpolate(from[0], to[0], coef);
        double a = method.interpolate(from[1], to[1], coef);
        double b = method.interpolate(from[2], to[2], coef);

        int[] res = ColorSpaceConverter.JzAzBztoRGB(L, a, b);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return 0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    public static int LCHOklabInterpolation(InterpolationMethod method, double coef, int[] c1, int[] c2, boolean shortPath) {
        double[] from = ColorSpaceConverter.RGBtoLCH_oklab(c1[1], c1[2], c1[3]);

        double[] to = ColorSpaceConverter.RGBtoLCH_oklab(c2[1], c2[2], c2[3]);

        double L = method.interpolate(from[0], to[0], coef);
        double C = method.interpolate(from[1], to[1], coef);
        double H;

        double d = to[2] - from[2];

        double temp;
        if (from[2] > to[2]) {
            temp = from[2];
            from[2] = to[2];
            to[2] = temp;
            d = -d;
            coef = 1 - coef;
        }

        boolean condition = shortPath ? d > 180 : d <= 180;

        if (condition) {
            from[2] += 360;
            H = method.interpolate(from[2], to[2], coef) % 360.0;
        } else {
            H = method.interpolate(from[2], to[2], coef);
        }

        int[] res = ColorSpaceConverter.LCH_oklabtoRGB(L, C, H);

        res[0] = ColorSpaceConverter.clamp(res[0]);
        res[1] = ColorSpaceConverter.clamp(res[1]);
        res[2] = ColorSpaceConverter.clamp(res[2]);

        return  0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }
}
