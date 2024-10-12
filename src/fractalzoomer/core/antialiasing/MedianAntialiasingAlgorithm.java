package fractalzoomer.core.antialiasing;

import fractalzoomer.utils.ColorCorrection;

import java.util.Arrays;

public class MedianAntialiasingAlgorithm extends AntialiasingAlgorithm {
    private double[] AValues;
    private double[] BValues;
    private double[] CValues;
    private int median;
    private boolean avgWithMean;
    private double ASum;
    private double BSum;
    private double CSum;

    public MedianAntialiasingAlgorithm(int totalSamples, boolean avgWithMean, int colorSpace) {
        super(totalSamples, colorSpace);
        AValues = new double[totalSamples];
        BValues = new double[totalSamples];
        CValues = new double[totalSamples];
        median = (totalSamples >>> 1);
        this.avgWithMean = avgWithMean;
        ASum = 0;
        BSum = 0;
        CSum = 0;
    }

    @Override
    public void initialize(int color) {
        double[] result = getColorChannels(color);

        double a = result[0];
        double b = result[1];
        double c = result[2];

        if(avgWithMean) {
            ASum = a;
            BSum = b;
            CSum = c;
        }

        addedSamples = 0;
        AValues[addedSamples] = a;
        BValues[addedSamples] = b;
        CValues[addedSamples] = c;
        addedSamples = 1;
    }

    @Override
    public boolean addSample(int color) {
        double[] result = getColorChannels(color);

        double a = result[0];
        double b = result[1];
        double c = result[2];

        if(avgWithMean) {
            ASum += a;
            BSum += b;
            CSum += c;
        }

        AValues[addedSamples] = a;
        BValues[addedSamples] = b;
        CValues[addedSamples] = c;
        addedSamples++;

        return true;
    }

    @Override
    public int getColor() {
        if(addedSamples != totalSamples) {
            return 0xff000000;
        }

        Arrays.sort(AValues);
        Arrays.sort(BValues);
        Arrays.sort(CValues);

        if(avgWithMean) {
            int[] result = getColorChannels(AValues[median], BValues[median], CValues[median]);
            int[] result2 = getAveragedColorChannels(ASum, BSum, CSum);
            double redRes = (result2[0] + result[0]) * 0.5 + 0.5;
            double greenRes = (result2[1] + result[1]) * 0.5 + 0.5;
            double blueRes = (result2[2] + result[2]) * 0.5 + 0.5;

            return ColorCorrection.linearToGamma(redRes, greenRes, blueRes);
        }
        else {
            int[] result = getColorChannels(AValues[median], BValues[median], CValues[median]);
            return ColorCorrection.linearToGamma(result[0], result[1], result[2]);
        }
    }

}
