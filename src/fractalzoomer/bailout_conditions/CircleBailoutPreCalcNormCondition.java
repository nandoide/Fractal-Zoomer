package fractalzoomer.bailout_conditions;

import fractalzoomer.core.BigComplex;
import fractalzoomer.core.BigNum;
import fractalzoomer.core.BigNumComplex;
import fractalzoomer.core.Complex;
import org.apfloat.Apfloat;

public class CircleBailoutPreCalcNormCondition extends BailoutCondition {

    public CircleBailoutPreCalcNormCondition(double bound) {

        super(bound);

    }

    @Override //euclidean precalculated norm
    public boolean escaped(Complex z, Complex zold, Complex zold2, int iterations, Complex c, Complex start, Complex c0, double norm_squared, Complex pixel) {

        return norm_squared >= bound;

    }

    @Override
    public boolean escaped(BigComplex z, BigComplex zold, BigComplex zold2, int iterations, BigComplex c, BigComplex start, BigComplex c0, Apfloat norm_squared, BigComplex pixel) {

        return norm_squared.compareTo(ddbound) >= 0;

    }

    @Override
    public boolean escaped(BigNumComplex z, BigNumComplex zold, BigNumComplex zold2, int iterations, BigNumComplex c, BigNumComplex start, BigNumComplex c0, BigNum norm_squared, BigNumComplex pixel) {
        return norm_squared.compare(bnbound) >= 0;
    }
}