package fractalzoomer.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

public class Multiwave {
    static WaveColorParams[] g_spdz2_params;
    static WaveColorParams[] default_params;
    static WaveColorParams[] g_spdz2_custom_params;

    enum Blend {
        HSL_BIAS,
        HSL_BIAS_UFCOMPAT
    }

    enum Mapping {
        MAPPING_NORMAL,
        MAPPING_SQUARE,
        MAPPING_SQRT,
        MAPPING_LOG,
    }

    private static double huefrac(double a, double b) {

        if(b == 0) {
            return 0;
        }

        if(b < a) {
            return b / a;
        }

        if(a == 0) {
            return 2.0;
        }

        if(a < b) {
            return 2.0 - a / b;
        }

        return  1.0;

    }

    private static double fastRem(double d, double denom) {
        return d - (denom * Math.floor(d / denom));
    }

    private static double[] rgbToHsl(int[] rgb) {

        double rec = 1.0 / 255.0;
        double r = rgb[0] * rec;
        double g = rgb[1] * rec;
        double b = rgb[2] * rec;

        double small = Math.min(Math.min(r, g), b);
        double large = Math.max(Math.max(r, g), b);

        double[] hsl = new double[3];

        double hue = 0, sat = 0, light = 0;

        if(large == 0.0) {
            light = Double.NEGATIVE_INFINITY;
        }
        else if(small == 1.0) {
            light = Double.POSITIVE_INFINITY;
        }
        else {
            double l = (small + large) * 0.5;
            double ll = 1.0 - (2.0 * Math.abs(l - 0.5));
            double s = (large - small) / ll;
            s = Math.max(0.0, Math.min(1.0, s));

            double rs = r - small;
            double gs = g - small;
            double bs = b - small;

            sat = Math.tan((s - 0.5) * Math.PI);
            light = Math.tan((l - 0.5) * Math.PI);

            if(small == r) {
                hue = 4.0 + huefrac(gs, bs);
            }
            else if(small == g) {
                hue = 6.0 + huefrac(bs, rs);
            }
            else {
                hue = 2.0 * huefrac(rs, gs);
            }

            hue = fastRem(hue, 8.0);
        }

        hsl[0] = hue;
        hsl[1] = sat;
        hsl[2] = light;
        return hsl;

    }

    private static int[] hslToRgb(double[] hsl) {
        double h = hsl[0];

        int[] rgb = new int[3];

        double r = 0, g = 0, b = 0;

        if(h >= 4.0) {
            h = h - 2.0;
        }
        else {
            h  = h * 0.5;
        }
        double rec = 1.0 / Math.PI;
        double s_color = Math.atan(hsl[1]) * rec + 0.5;
        double s_grey = 0.5 * (1.0 - s_color);
        double l_color = Math.atan(hsl[2]) * rec;
        double lca = Math.abs(l_color);
        double l_white = l_color + lca;
        l_color = 1.0 - 2.0 * lca;

        if(h < 1.0) {
            r = 1.0;
            g = h;
        }
        else if(h < 2.0) {
            r = 2.0 - h;
            g = 1.0;
        }
        else if(h < 3.0) {
            g = 1.0;
            b = h - 2.0;
        }
        else if (h < 4.0) {
            g = 4.0 - h;
            b = 1.0;
        }
        else if (h < 5.0) {
            r = h - 4.0;
            b = 1.0;
        }
        else {
            r = 1.0;
            b = 6.0 - h;
        }

        r = ((((r * s_color) + s_grey) * l_color) + l_white) * 255;
        g = ((((g * s_color) + s_grey) * l_color) + l_white) * 255;
        b = ((((b * s_color) + s_grey) * l_color) + l_white) * 255;

        rgb[0] = (int)(Math.floor(r));
        rgb[1] = (int)(Math.floor(g));
        rgb[2] = (int)(Math.floor(b));

        return rgb;
    }

    private static double[] hslBias(double[] hsl1, double[] hsl2) {
        double[] hsl_res = new double[3];

        hsl_res[0] = fastRem(hsl1[0] + hsl2[0], 8.0);
        hsl_res[1] = hsl1[1] + hsl2[1];
        hsl_res[2] = hsl1[2] + hsl2[2];

        return hsl_res;
    }

    private static double slx(double x, double y) {
        double rec = 1.0 / Math.PI;
        x = Math.atan(x) * rec + 0.5;
        y = Math.atan(y) * rec + 0.5;

        if(x == 0) {
            return  0.0;
        }
        else if(y == 0) {
            return 0.0;
        }
        else if (x == 1.0) {
            return 1.0;
        }
        else if (y == 1.0) {
            return 1.0;
        }
        else {
            double z = 1.0 - 1 / ((1 / (1.0 - x) - 1.0) * (1 / (1.0 - y) - 1.0) + 1.0);
            z = Math.max(0.0, Math.min(1.0, z));
            return Math.tan((z - 0.5) * Math.PI);
        }
    }

    private static double[] hslBiasUfcompat(double[] hsl1, double[] hsl2) {
        double[] hsl_res = new double[3];
        hsl_res[0] = fastRem(hsl1[0] + hsl2[0], 8.0);
        hsl_res[1] = slx(hsl1[1], hsl2[1]);
        hsl_res[2] = slx(hsl1[2], hsl2[2]);
        return hsl_res;
    }

    private static double clamp255(double x) {
        return Math.floor(Math.min(255.0, Math.max(0.0, x)));
    }

    private static double[] rgbToHsl2(int[] rgb) {

        double rec = 1.0 / 255.0;
        double r = rgb[0] * rec;
        double g = rgb[1] * rec;
        double b = rgb[2] * rec;

        double small = Math.min(Math.min(r, g), b);
        double large = Math.max(Math.max(r, g), b);

        double[] hsl = new double[3];

        double hue = 0, sat = 0, light = 0;

        if(large == 0.0) {
            light = Double.NEGATIVE_INFINITY;
        }
        else if(small == 1.0) {
            light = Double.POSITIVE_INFINITY;
        }
        else {
            double l = (small + large) * 0.5;
            double ll = 1.0 - 2.0 * Math.abs(l - 0.5);
            double s = (large - small) / ll;
            s = Math.max(0.0, Math.min(1.0, s));
            double rs = r - small;
            double gs = g - small;
            double bs = b - small;
            sat = Math.tan((s - 0.5) * Math.PI);
            light = Math.tan((l - 0.5) * Math.PI);

            if(r == small) {
                hue = 2.0 + huefrac(gs, bs);
            }
            else if (g == small) {
                hue = 4.0 + huefrac(bs, rs);
            }
            else {
                hue = huefrac(rs, gs);//0.0  +
            }

            hue = 4.0/3.0 * fastRem(hue, 6.0);
        }


        hsl[0] = hue;
        hsl[1] = sat;
        hsl[2] = light;
        return hsl;
    }

    private static int[] hslToRgb2(double[] hsl) {
        double h = hsl[0] / (4.0 / 3.0);
        double s_color = Math.atan(hsl[1]) / Math.PI + 0.5;
        double s_grey = 0.5 * (1.0 - s_color);
        double l_color = Math.atan(hsl[2]) / Math.PI;
        double lca = Math.abs(l_color);
        double l_white = l_color + lca;
        l_color = 1.0 - 2.0 * lca;

        double r = 0, g = 0, b = 0;

        if(h < 1.0) {
            r = 1.0;
            g = h;
        }
        else if(h < 2.0) {
            r = 2.0 - h;
            g = 1.0;
        }
        else if (h < 3.0) {
            g = 1.0;
            b = h - 2.0;
        }
        else if (h < 4.0) {
            g = 4.0 - h;
            b = 1.0;
        }
        else if (h < 5.0) {
            r = h - 4.0;
            b = 1.0;
        }
        else {
            r = 1.0;
            b = 6.0 - h;
        }

        r = ((((r * s_color) + s_grey) * l_color) + l_white) * 255;
        g = ((((g * s_color) + s_grey) * l_color) + l_white) * 255;
        b = ((((b * s_color) + s_grey) * l_color) + l_white) * 255;

        int[] rgb = new int[3];
        rgb[0] = (int)(Math.floor(r));
        rgb[1] = (int)(Math.floor(g));
        rgb[2] = (int)(Math.floor(b));

        return rgb;
    }

    private static HashMap<String, int[][]> precalculated = new HashMap<>();
    private static double[] tricubicGradientHsl(String key, int resolution, double fvalIn, double[][] hsls) {

        int[][] rgbs_resolution = precalculated.get(key);
        if(rgbs_resolution!= null) {
            return rgbToHsl2(rgbs_resolution[(int)Math.floor(fvalIn * resolution)]);
        }

        int[][] colorsrgb = new int[hsls.length][];
        for (int i = 0; i < colorsrgb.length; i++) {
            colorsrgb[i] = hslToRgb2(hsls[i]);
        }

        int numc = colorsrgb.length;
        double p2 = 1.0 / numc;

        rgbs_resolution = new int[resolution][];

        for (int i = 0; i < resolution; i++) {

            double fval = i / (double) resolution;

            int b = (int) Math.floor(fval / p2);
            int a = (int) fastRem(b - 1, numc);
            int c = (int) fastRem(b + 1, numc);
            int d = (int) fastRem(c + 1, numc);

            fval = fastRem(fval, p2) / p2;

            int[] as = colorsrgb[a];
            int[] bs = colorsrgb[b];
            int[] cs = colorsrgb[c];
            int[] ds = colorsrgb[d];

            double rp0 = bs[0];
            double gp0 = bs[1];
            double bp0 = bs[2];

            double rm0 = (cs[0] - as[0]) * 0.5;
            double gm0 = (cs[1] - as[1]) * 0.5;
            double bm0 = (cs[2] - as[2]) * 0.5;

            double rp1 = cs[0];
            double gp1 = cs[1];
            double bp1 = cs[2];

            double rm1 = (ds[0] - bs[0]) * 0.5;
            double gm1 = (ds[1] - bs[1]) * 0.5;
            double bm1 = (ds[2] - bs[2]) * 0.5;

            double ffval = fval * fval;
            double ffval3 = 3 * ffval;
            double fffval = ffval * fval;
            double fffval2 = fffval * 2;

            double fa = fffval2 - ffval3 + 1;
            double fb = (fffval - 2 * ffval) + fval;
            double fc = ffval3 - fffval2;
            double fd = fffval - ffval;


            int[] temp = new int[3];
            temp[0] = (int) (clamp255(fa * rp0) + (fb * rm0) + (fc * rp1) + (fd * rm1));
            temp[1] = (int) (clamp255(fa * gp0) + (fb * gm0) + (fc * gp1) + (fd * gm1));
            temp[2] = (int) (clamp255(fa * bp0) + (fb * bm0) + (fc * bp1) + (fd * bm1));

            rgbs_resolution[i] = temp;
        }

        precalculated.put(key, rgbs_resolution);
        return rgbToHsl2(rgbs_resolution[(int)Math.floor(fvalIn * resolution)]);
    }

    private static double[] tricubicGradientRgb(String key, int resolution, double fval, int[][] rgbs) {
        double[][] hsls = new double[rgbs.length][3];
        for(int i = 0; i < rgbs.length; i++) {
            hsls[i] = rgbToHsl(rgbs[i]);
        }
        return tricubicGradientHsl(key, resolution, fval, hsls);
    }

    private static double[] tricubicGradientHslNp(double fval, double[][] hsls) {

        int[][] colorsrgb = new int[hsls.length][];
        for(int i = 0; i < colorsrgb.length; i++) {
            colorsrgb[i] = hslToRgb2(hsls[i]);
        }

        int numc = colorsrgb.length;
        double p2 = 1.0 / numc;

        int b =  (int) Math.floor(fval / p2);//fastRem(fval / p2, numc);
        int a  = (int) fastRem(b - 1, numc);
        int c = (int) fastRem(b + 1, numc);
        int d = (int) fastRem(c + 1, numc);

        fval = fastRem(fval, p2) / p2;

        int[] as = colorsrgb[a];
        int[] bs = colorsrgb[b];
        int[] cs = colorsrgb[c];
        int[] ds = colorsrgb[d];

        double rp0 = bs[0];
        double gp0 = bs[1];
        double bp0 = bs[2];

        double rm0 = (cs[0] - as[0]) * 0.5;
        double gm0 = (cs[1] - as[1]) * 0.5;
        double bm0 = (cs[2] - as[2]) * 0.5;

        double rp1 = cs[0];
        double gp1 = cs[1];
        double bp1 = cs[2];

        double rm1 = (ds[0] - bs[0]) * 0.5;
        double gm1 = (ds[1] - bs[1]) * 0.5;
        double bm1 = (ds[2] - bs[2]) * 0.5;

        double ffval = fval * fval;
        double ffval3 = 3 * ffval;
        double fffval = ffval * fval;
        double fffval2 = fffval * 2;

        double fa = fffval2 - ffval3 + 1;
        double fb = (fffval - 2 * ffval) + fval;
        double fc = ffval3 - fffval2;
        double fd = fffval - ffval;

        int[] temp = new int[3];
        temp[0] = (int)(clamp255(fa * rp0) + (fb * rm0) + (fc * rp1) + (fd * rm1));
        temp[1] = (int)(clamp255(fa * gp0) + (fb * gm0) + (fc * gp1) + (fd * gm1));
        temp[2] = (int)(clamp255(fa * bp0) + (fb * bm0) + (fc * bp1) + (fd * bm1));

        return rgbToHsl2(temp);
    }

    private static double[] tricubicGradientRgbNp(double fval, int[][] rgbs) {
        double[][] hsls = new double[rgbs.length][3];
        for(int i = 0; i < rgbs.length; i++) {
            hsls[i] = rgbToHsl(rgbs[i]);
        }
        return tricubicGradientHslNp(fval, hsls);
    }

    private static class LinearRGB {
        @JsonProperty("x")
        double x;

        public double getX() {
            return x;
        }

        public int[] getRgb() {
            return rgb;
        }

        @JsonProperty("rgb")
        int[] rgb;

        public LinearRGB(double x, int[] rgb) {
            this.x = x;
            this.rgb = rgb;
        }
    }

    private static class LinearHSL {
        @JsonProperty("x")
        double x;

        public double getX() {
            return x;
        }

        public double[] getHsl() {
            return hsl;
        }

        @JsonProperty("hsl")
        double[] hsl;

        public LinearHSL(double x, double[] hsl) {
            this.x = x;
            this.hsl = hsl;
        }
    }

    private static double[] linearGradientHslNp(double fval, LinearHSL[] input) {

        LinearRGB[] converted = new LinearRGB[input.length];

        for(int i = 0; i < converted.length; i++) {
            converted[i] = new LinearRGB(input[i].x, hslToRgb2(input[i].hsl));
        }

        return linearGradientRgbNp(fval, converted);

    }

    private static double[] linearGradientRgbNp(double fval, LinearRGB[] input) {

        int[] temp  = new int[3];

//        if(input.length < 1 || input[0].x != 0) {
//            throw new Exception("The first color must begin at 0.");
//        }


        int i;
        for(i = 0; i < input.length - 1; i++) {
            if(fval >= input[i].x && fval <= input[i + 1].x) {
                break;
            }
        }

        LinearRGB a = null;
        LinearRGB b = null;

        if(i < input.length - 1) {
            a = input[i];
            b = input[i + 1];
        }
        else {
            if(i < input.length) {
                a = input[i];
                b = input[0];
            }
        }

        if(b.x >= a.x) {
            fval = (fval - a.x) / (b.x - a.x);
        }
        else {
            fval = (fval - a.x) / (1.0 - a.x);
        }

        double pfval = 1.0 - fval;

        temp[0] = (int)clamp255(a.rgb[0] * pfval + b.rgb[0] * fval);
        temp[1] = (int)clamp255(a.rgb[1] * pfval + b.rgb[1] * fval);
        temp[2] = (int)clamp255(a.rgb[2] * pfval + b.rgb[2] * fval);

        return rgbToHsl2(temp);
    }

    private static double[] metaTricubicGradientRgb(double fval, MetaTricubicRGB o) {
        double period1 = o.period1;
        double period2 = o.period2;
        int[][][] rgbs = o.colors;

        double fval1 = (fval % period1) / period1;
        double fval2 = (fval % period2) / period2;

        double[][] result = new double[rgbs.length][];

        for(int i = 0; i < rgbs.length; i++) {
            result[i] = tricubicGradientRgbNp(fval1, rgbs[i]);
        }

        return tricubicGradientHslNp(fval2, result);

    }

    private static double[] metaTricubicGradientHsl(double fval, MetaTricubicHSL o) {
        double period1 = o.period1;
        double period2 = o.period2;
        double[][][] hsls = o.colors;

        double fval1 = (fval % period1) / period1;
        double fval2 = (fval % period2) / period2;

        double[][] result = new double[hsls.length][];

        for(int i = 0; i < hsls.length; i++) {
            result[i] = tricubicGradientHslNp(fval1, hsls[i]);
        }

        return tricubicGradientHslNp(fval2, result);

    }

    private static double[] blend(Blend blend, double[] hsl1, double[] hsl2) {
        if(blend == Blend.HSL_BIAS) {
            return hslBias(hsl1, hsl2);
        }
        return hslBiasUfcompat(hsl1, hsl2);
    }
    private static double[] multiwaveColor(WaveColor[] waves) {

        if(waves.length == 0) {
            return new double[] {0.0, 0.0, 0.0};
        }

        double[] prev_hsl = waves[0].gradient;
        for(int i = 1; i < waves.length; i++) {
            prev_hsl = blend(waves[i].blend, prev_hsl, waves[i].gradient);
        }

        return prev_hsl;

    }

    public static class MetaTricubicRGB {
        public int[][][] getColors() {
            return colors;
        }

        public double getPeriod1() {
            return period1;
        }

        public double getPeriod2() {
            return period2;
        }

        int[][][] colors;
        double period1;
        double period2;

        public MetaTricubicRGB(int[][][] colors, double period1, double period2) {
            this.colors = colors;
            this.period1 = period1;
            this.period2 = period2;
        }
    }

    public static class MetaTricubicHSL {
        public double[][][] getColors() {
            return colors;
        }

        public double getPeriod1() {
            return period1;
        }

        public double getPeriod2() {
            return period2;
        }

        double[][][] colors;
        double period1;
        double period2;

        public MetaTricubicHSL(double[][][] colors, double period1, double period2) {
            this.colors = colors;
            this.period1 = period1;
            this.period2 = period2;
        }
    }

    @JsonPropertyOrder({"attribute", "*"})
    public static class WaveColorParams {
        @JsonProperty("period")
        public Double period;
        @JsonProperty("mapping")
        public Mapping mapping;
        @JsonProperty("start")
        public Double start;
        @JsonProperty("end")
        public Double end;
        @JsonProperty("blend")
        public Blend blend;

        @JsonProperty("attribute")
        public String getName() {
            return "color_wave";
        }

        public Double getPeriod() {
            return period;
        }

        public Mapping getMapping() {
            return mapping;
        }

        public Double getStart() {
            return start;
        }

        public Double getEnd() {
            return end;
        }

        public Blend getBlend() {
            return blend;
        }

        public int[][] getTricubic_rgb() {
            return tricubic_rgb;
        }

        public double[][] getTricubic_hsl() {
            return tricubic_hsl;
        }

        public LinearRGB[] getLinear_rgb() {
            return linear_rgb;
        }

        public LinearHSL[] getLinear_hsl() {
            return linear_hsl;
        }

        public MetaTricubicRGB getMeta_tricubic_rgb() {
            return meta_tricubic_rgb;
        }

        public MetaTricubicHSL getMeta_tricubic_hsl() {
            return meta_tricubic_hsl;
        }

        @JsonProperty("tricubic_rgb")
        public int[][] tricubic_rgb;

        @JsonProperty("tricubic_hsl")
        public double[][] tricubic_hsl;
        @JsonProperty("linear_rgb")
        public LinearRGB[] linear_rgb;

        @JsonProperty("linear_hsl")
        public LinearHSL[] linear_hsl;

        @JsonProperty("meta_tricubic_rgb")
        public MetaTricubicRGB meta_tricubic_rgb;

        @JsonProperty("meta_tricubic_hsl")
        public MetaTricubicHSL meta_tricubic_hsl;

        public WaveColorParams(Double period, Mapping mapping, Double start, Double end, Blend blend) {
            this.period = period;
            this.mapping = mapping;
            this.start = start;
            this.end = end;
            this.blend = blend;
        }

        public void setTricubicRGB(int[][] vals) {
            tricubic_rgb = vals;
        }

        public void setTricubicHSL(double[][] vals) {
            tricubic_hsl = vals;
        }

        public void setLinearRGB(LinearRGB[] vals) {
            linear_rgb = vals;
        }

        public void setLinearRGB(LinearHSL[] vals) {
            linear_hsl = vals;
        }

        public void setMetaTricubicRGB(MetaTricubicRGB o) {
            meta_tricubic_rgb = o;
        }

        public void setMetaTricubicHsl(MetaTricubicHSL o) {
            meta_tricubic_hsl = o;
        }

        public static WaveColor[] build(WaveColorParams[] params, double n) throws Exception {
            WaveColor[] waves = new WaveColor[params.length];
            for(int i = 0; i < waves.length; i++) {
                params[i].validate();
                waves[i] = new WaveColor(params[i], n);
            }
            return waves;
        }

        public void validate() throws Exception {
            if(period != null && period <= 0) {
                throw new Exception("Period must be greater than zero.");
            }
            if(start != null && start < 0) {
                throw new Exception("Start must be greater or equal to zero.");
            }
            if(end != null && end < 0) {
                throw new Exception("End must be greater or equal to zero.");
            }
            if(start != null && end != null && end < start) {
                throw new Exception("End must be greater than start.");
            }
            int definitions = 0;
            if(meta_tricubic_rgb != null) {
                definitions++;
            }
            if(meta_tricubic_hsl != null) {
                definitions++;
            }
            if(tricubic_rgb != null) {
                definitions++;
            }
            if(tricubic_hsl != null) {
                definitions++;
            }
            if(linear_rgb != null) {
                definitions++;
            }
            if(linear_hsl != null) {
                definitions++;
            }
            if(definitions != 1) {
                throw new Exception("Each color wave definition must contain one definition from the list (tricubic_rgb, tricubic_hsl, meta_tricubic_rgb, meta_tricubic_hsl, linear_rgb, linear_hsl)");
            }

            if(linear_rgb != null) {
                for(int i = 0; i < linear_rgb.length; i++) {
                    if(linear_rgb[i].x < 0 || linear_rgb[i].x > 1) {
                        throw new Exception("The linear coefficients must be between 0 and 1.");
                    }
                }
                for(int i = 0; i < linear_rgb.length - 1; i++) {
                    if(linear_rgb[i + 1].x < linear_rgb[i].x) {
                        throw new Exception("The linear coefficients must in ascending order.");
                    }
                }
            }

            if(linear_hsl != null) {
                for(int i = 0; i < linear_hsl.length; i++) {
                    if(linear_hsl[i].x < 0 || linear_hsl[i].x > 1) {
                        throw new Exception("The linear coefficients must be between 0 and 1.");
                    }
                }
                for(int i = 0; i < linear_hsl.length - 1; i++) {
                    if(linear_hsl[i + 1].x < linear_hsl[i].x) {
                        throw new Exception("The linear coefficients must in ascending order.");
                    }
                }
            }

            if(meta_tricubic_rgb != null) {
                if(meta_tricubic_rgb.period1 <= 0 || meta_tricubic_rgb.period2 <= 0) {
                    throw new Exception("Meta Tricubic Periods must be greater than zero.");
                }
            }

            if(meta_tricubic_hsl != null) {
                if(meta_tricubic_hsl.period1 <= 0 || meta_tricubic_hsl.period2 <= 0) {
                    throw new Exception("Meta Tricubic Periods must be greater than zero.");
                }
            }
        }

    }


    public static class WaveColor {
        protected double[] gradient;
        protected Blend blend;
        protected Mapping mapping;
        protected Double period;
        protected Double start;
        protected Double start2;
        protected Double end;

        public WaveColor(WaveColorParams params, double n) {
            Mapping mappingIn = params.mapping;
            Double startIn = params.start;
            Double endIn = params.end;
            Double periodIn = params.period;
            Blend blend = params.blend;

            mapping = mappingIn;
            if(mapping == null) {
                mapping = Mapping.MAPPING_NORMAL;
            }

            start = startIn;
            if(start == null) {
                start = Double.NEGATIVE_INFINITY;
            }
            else {
                start = mapping(start, mapping);
            }

            start2 = start > Double.NEGATIVE_INFINITY ? start : 0.0;

            end = endIn;
            if(end == null) {
                end = Double.POSITIVE_INFINITY;
            }
            else {
                end = mapping(end, mapping);
            }

            period = periodIn;
            if(period == null) {
                period = end - start; //Not start2?
            }
            else {
                period = mapping(period, mapping);
            }

            this.blend = blend;
            if(this.blend == null) {
                this.blend = Blend.HSL_BIAS;
            }

            if(params.tricubic_rgb != null) {
                gradient = tricubicGradientRgbNp(getFval(n), params.tricubic_rgb);
            }
            else if(params.linear_rgb != null) {
                gradient = linearGradientRgbNp(getFval(n), params.linear_rgb);
            }
            else if(params.linear_hsl != null) {
                gradient = linearGradientHslNp(getFval(n), params.linear_hsl);
            }
            else if(params.meta_tricubic_rgb != null) {
                gradient = metaTricubicGradientRgb(getFval(n), params.meta_tricubic_rgb);
            }
            else if(params.meta_tricubic_hsl != null) {
                gradient = metaTricubicGradientHsl(getFval(n), params.meta_tricubic_hsl);
            }
            else {
                gradient = new double[0];
            }
        }

        public double[] getGradient() {
            return gradient;
        }

        private double getFval(double fval) {
            double fval2 = mapping(fval, mapping);
            fval2 = Math.max(start, Math.min(end, fval2));
            return fastRem(fval2 - start2, period) / period;
        }

        private static double mapping(double x, Mapping mapping) {
            if(mapping == Mapping.MAPPING_NORMAL) {
                return x;
            }
            else if(mapping == Mapping.MAPPING_SQUARE) {
                return x * x;
            }
            else if (mapping == Mapping.MAPPING_SQRT) {
                return Math.sqrt(x);
            } else {
                return Math.log(x);
            }
        }
    }

    public static int multiwave_simple(double n) throws Exception {

        WaveColorParams p1 = new WaveColorParams(100.0, Mapping.MAPPING_NORMAL, null, null, Blend.HSL_BIAS);
        p1.setTricubicHSL(new double[][] {{0, 0, 0}, {7.5, -5, -5}, {6.5, 0, 0}, {7.5, 5, 5}});

        WaveColorParams[] params = {
                p1
        };
        int[] rgb = hslToRgb(multiwaveColor(WaveColorParams.build(params, n)));
        return 0xff000000 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
    }

    public static int multiwave_default(double n) throws Exception {
        int[] rgb = hslToRgb(multiwaveColor(WaveColorParams.build(default_params, n)));
        return 0xff000000 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
    }

    static {
        create_default_params();
        create_g_spdz2_params();
        create_g_spdz2_custom_params();
    }

    static void create_default_params() {
        WaveColorParams p1 = new WaveColorParams(1000.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS);
        p1.setTricubicHSL(new double[][] {{0, 0, 0}, {7.5, 0, -3}, {6.5, -3, 0}, {7.5, 0, 3}});

        WaveColorParams p2 = new WaveColorParams(30.0, Mapping.MAPPING_NORMAL, null, null, Blend.HSL_BIAS);
        p2.setTricubicHSL(new double[][] {{0, 0, 0}, {7.5, -2, -2}, {0.5, 2, 2}});

        WaveColorParams p3 = new WaveColorParams(120.0, Mapping.MAPPING_NORMAL, null, null, Blend.HSL_BIAS);
        p3.setTricubicHSL(new double[][] {{0, 0, 0}, {0, -1, -2}, {0, 0, 0}, {0, 1, 2}});

        WaveColorParams p4 = new WaveColorParams(1e6, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS);
        p4.setTricubicHSL(new double[][] {{0, 0, 0}, {1, 0, 0}, {2, 0, 0}, {3, 0, 0}, {4, 0, 0}, {5, 0, 0}, {6, 0, 0}, {7, 0, 0},});

        WaveColorParams p5 = new WaveColorParams(3500.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS);
        p5.setTricubicHSL(new double[][] {{0, 0, 0}, {2.5, 3, -5}, {3.5, 5, -2}, {2, -4, 4}, {0.5, 4, 2}});

        WaveColorParams[] params = {
                p1,
                p2,
                p3,
                p4,
                p5
        };
        default_params = params;
    }

    static void create_g_spdz2_custom_params() {
        WaveColorParams p0 = new WaveColorParams(1175000.0, Mapping.MAPPING_NORMAL, null, null, Blend.HSL_BIAS_UFCOMPAT);
        p0.setMetaTricubicRGB(new MetaTricubicRGB(new int[][][]{
                {{15, 91, 30}, {60, 62, 128}, {71, 37, 95}, {45, 45, 53}, {64, 62, 80}}
                ,{{56, 240, 80}, {187, 141, 199}, {142, 128, 146}, {24, 24, 164}, {135, 155, 171}}
                ,{{74, 186, 77}, {73, 0, 92}, {195, 130, 234}, {151, 149, 189}, {175, 199, 196}}
                ,{{29, 39, 227}, {225, 33, 255}, {9, 95, 233}, {120, 84, 100}, {21, 33, 123}}
        }, 0.02127659574468085, 8E-4));

        WaveColorParams p1 = new WaveColorParams(5000.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS_UFCOMPAT);
        p1.setTricubicRGB(new int[][]{{192, 64, 64}, {192, 64, 64}, {81, 71, 71}});

        WaveColorParams p2 = new WaveColorParams(10.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS_UFCOMPAT);
        p2.setTricubicRGB(new int[][]{{199, 83, 83}, {192, 64, 64}, {172, 58, 58}, {192, 64, 64}});

        WaveColorParams p3 = new WaveColorParams(17.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS_UFCOMPAT);
        p3.setTricubicRGB(new int[][]{{211, 121, 121}, {192, 64, 64}, {135, 45, 45}, {192, 64, 64}});

        WaveColorParams p4 = new WaveColorParams(2544.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS_UFCOMPAT);
        p4.setTricubicRGB(new int[][]{{243, 217, 217}, {192, 64, 64}, {39, 13, 13}, {192, 64, 64}});

        WaveColorParams p5 = new WaveColorParams(235.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS_UFCOMPAT);
        p5.setTricubicRGB(new int[][]{{192, 64, 64}, {76, 26, 26}, {192, 64, 64}, {231, 179, 179}});

        WaveColorParams p6 = new WaveColorParams(Math.pow(2, 16), Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS_UFCOMPAT);

        p6.setLinearRGB(new LinearRGB[] {
                new LinearRGB(0.0, new int[] {11, 25, 12}),
                new LinearRGB(0.375, new int[] {192, 64, 64}),
                new LinearRGB(0.5875, new int[] {192, 64, 64}),
                new LinearRGB(0.6125, new int[] {179, 177, 177}),
                new LinearRGB(0.69, new int[] {128, 237, 19}),
                new LinearRGB(0.7, new int[] {78, 99, 102}),
                new LinearRGB(0.7025, new int[] {63, 53, 131}),
                new LinearRGB(0.715, new int[] {0, 153, 180}),
                new LinearRGB(0.74, new int[] {4, 154, 184}),
                new LinearRGB(0.7475, new int[] {204, 34, 190}),
                new LinearRGB(0.7875, new int[] {216, 194, 195}),
                new LinearRGB(0.8325, new int[] {183, 154, 61}),
                new LinearRGB(1.0, new int[] {243, 227, 234}),
        });

        WaveColorParams[] params = {
                p0,
                p1,
                p2,
                p3,
                p4,
                p5,
                p6
        };

        g_spdz2_custom_params = params;
    }

    static void create_g_spdz2_params() {
        WaveColorParams p0 = new WaveColorParams(1175000.0, Mapping.MAPPING_NORMAL, null, null, Blend.HSL_BIAS_UFCOMPAT);
        p0.setMetaTricubicRGB(new MetaTricubicRGB(new int[][][]{
                {{15, 91, 30}, {60, 62, 128}, {71, 37, 95}, {45, 45, 53}, {64, 62, 80}}
                ,{{56, 240, 80}, {187, 141, 199}, {142, 128, 146}, {24, 24, 164}, {135, 155, 171}}
                ,{{74, 186, 77}, {73, 0, 92}, {195, 130, 234}, {151, 149, 189}, {175, 199, 196}}
                ,{{29, 39, 227}, {225, 33, 255}, {9, 95, 233}, {120, 84, 100}, {21, 33, 123}}
        }, 0.02127659574468085, 8E-4));

        WaveColorParams p1 = new WaveColorParams(5000.0, Mapping.MAPPING_NORMAL, null, null, Blend.HSL_BIAS_UFCOMPAT);
        p1.setTricubicRGB(new int[][]{{192, 64, 64}, {192, 64, 64}, {81, 71, 71}});

        WaveColorParams p2 = new WaveColorParams(10.0, Mapping.MAPPING_NORMAL, null, null, Blend.HSL_BIAS_UFCOMPAT);
        p2.setTricubicRGB(new int[][]{{199, 83, 83}, {192, 64, 64}, {172, 58, 58}, {192, 64, 64}});

        WaveColorParams p3 = new WaveColorParams(17.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS_UFCOMPAT);
        p3.setTricubicRGB(new int[][]{{211, 121, 121}, {192, 64, 64}, {135, 45, 45}, {192, 64, 64}});

        WaveColorParams p4 = new WaveColorParams(2544.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS_UFCOMPAT);
        p4.setTricubicRGB(new int[][]{{243, 217, 217}, {192, 64, 64}, {39, 13, 13}, {192, 64, 64}});

        WaveColorParams p5 = new WaveColorParams(235.0, Mapping.MAPPING_NORMAL, null, null,  Blend.HSL_BIAS_UFCOMPAT);
        p5.setTricubicRGB(new int[][]{{192, 64, 64}, {76, 26, 26}, {192, 64, 64}, {231, 179, 179}});

        WaveColorParams p6 = new WaveColorParams(null, Mapping.MAPPING_LOG, 1.0, 16777216.0,  Blend.HSL_BIAS_UFCOMPAT);
        p6.setLinearRGB(new LinearRGB[] {
                new LinearRGB(0.0, new int[] {11, 25, 12}),
                new LinearRGB(0.375, new int[] {192, 64, 64}),
                new LinearRGB(0.5875, new int[] {192, 64, 64}),
                new LinearRGB(0.6125, new int[] {179, 177, 177}),
                new LinearRGB(0.69, new int[] {128, 237, 19}),
                new LinearRGB(0.7, new int[] {78, 99, 102}),
                new LinearRGB(0.7025, new int[] {63, 53, 131}),
                new LinearRGB(0.715, new int[] {0, 153, 180}),
                new LinearRGB(0.74, new int[] {4, 154, 184}),
                new LinearRGB(0.7475, new int[] {204, 34, 190}),
                new LinearRGB(0.7875, new int[] {216, 194, 195}),
                new LinearRGB(0.8325, new int[] {183, 154, 61}),
                new LinearRGB(1.0, new int[] {243, 227, 234}),
        });

        WaveColorParams[] params = {
                p0,
                p1,
                p2,
                p3,
                p4,
                p5,
                p6
        };

        g_spdz2_params = params;
    }
    public static int g_spdz2(double n) throws Exception {
        int[] rgb = hslToRgb(multiwaveColor(WaveColorParams.build(g_spdz2_params, n)));
        return 0xff000000 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
    }

    public static int g_spdz2_custom(double n) throws Exception {
        int[] rgb = hslToRgb(multiwaveColor(WaveColorParams.build(g_spdz2_custom_params, n)));
        return 0xff000000 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
    }

    public static int linear_only(double n) throws Exception {


        WaveColorParams p1 = new WaveColorParams(Math.pow(2, 16), Mapping.MAPPING_NORMAL, null, null, Blend.HSL_BIAS_UFCOMPAT);
        p1.setLinearRGB(new LinearRGB[] {
                new LinearRGB(0.0, new int[] {11, 25, 12}),
                new LinearRGB(0.375, new int[] {192, 64, 64}),
                new LinearRGB(0.5875, new int[] {192, 64, 64}),
                new LinearRGB(0.6125, new int[] {179, 177, 177}),
                new LinearRGB(0.69, new int[] {128, 237, 19}),
                new LinearRGB(0.7, new int[] {78, 99, 102}),
                new LinearRGB(0.7025, new int[] {63, 53, 131}),
                new LinearRGB(0.715, new int[] {0, 153, 180}),
                new LinearRGB(0.74, new int[] {4, 154, 184}),
                new LinearRGB(0.7475, new int[] {204, 34, 190}),
                new LinearRGB(0.7875, new int[] {216, 194, 195}),
                new LinearRGB(0.8325, new int[] {183, 154, 61}),
                new LinearRGB(1.0, new int[] {243, 227, 234}),

        });

        WaveColorParams[] params = {
                p1
        };
        int[] rgb = hslToRgb(multiwaveColor(WaveColorParams.build(params, n)));
        return 0xff000000 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
    }

    public static int meta_tricubic_gradient_only(double n) throws Exception {

        WaveColorParams p1 = new WaveColorParams(1175000.0, Mapping.MAPPING_NORMAL, null, null, Blend.HSL_BIAS_UFCOMPAT);
        p1.setMetaTricubicRGB(new MetaTricubicRGB(new int[][][]{
                {{15, 91, 30}, {60, 62, 128}, {71, 37, 95}, {45, 45, 53}, {64, 62, 80}}
                ,{{56, 240, 80}, {187, 141, 199}, {142, 128, 146}, {24, 24, 164}, {135, 155, 171}}
                ,{{74, 186, 77}, {73, 0, 92}, {195, 130, 234}, {151, 149, 189}, {175, 199, 196}}
                ,{{29, 39, 227}, {225, 33, 255}, {9, 95, 233}, {120, 84, 100}, {21, 33, 123}}
        }, 0.02127659574468085, 8E-4));

        WaveColorParams[] params = {
                p1
        };
        int[] rgb = hslToRgb(multiwaveColor(WaveColorParams.build(params, n)));
        return 0xff000000 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];

    }

    static String paramsToJson(WaveColorParams[] params) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(params);
    }

    static WaveColorParams[] jsonToParams(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, WaveColorParams[].class);
    }

    public static void main(String[] args) throws Exception {


        int image_size = 1024;
        BufferedImage image = new BufferedImage(image_size, image_size, BufferedImage.TYPE_INT_ARGB);
        for(int y = 0; y < image_size; y++) {
            for (int x = 0; x < image_size; x++) {
                image.setRGB(x, y,  g_spdz2((((image_size-1-y) * image_size + x))));
            }
        }

        File outputfile = new File("multiwave.png");
        try {
            ImageIO.write(image, "png", outputfile);
        }
        catch (Exception ex) {}
    }
}
