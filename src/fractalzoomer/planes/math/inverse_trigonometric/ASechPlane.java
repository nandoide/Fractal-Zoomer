

package fractalzoomer.planes.math.inverse_trigonometric;

import fractalzoomer.core.*;
import fractalzoomer.planes.Plane;

/**
 *
 * @author hrkalona2
 */
public class ASechPlane extends Plane {
    
    public ASechPlane() {
        
        super();
        
    }

    @Override
    public Complex transform_internal(Complex pixel) {

        if(pixel.isZero()) {
            return pixel;
        }

        return pixel.asech();

    }

    @Override
    public MpfrBigNumComplex transform_internal(MpfrBigNumComplex pixel) {

        if(pixel.isZero()) {
            return pixel;
        }

        return pixel.asech();

    }

    @Override
    public DDComplex transform_internal(DDComplex pixel) {

        if(pixel.isZero()) {
            return pixel;
        }

        return pixel.asech();

    }

    @Override
    public MpirBigNumComplex transform_internal(MpirBigNumComplex pixel) {

        if(pixel.isZero()) {
            return pixel;
        }

        return new MpirBigNumComplex(transform_internal(pixel.toComplex()));

    }

    @Override
    public BigComplex transform_internal(BigComplex pixel) {

        if(pixel.isZero()) {
            return pixel;
        }

        return new BigComplex(transform_internal(pixel.toComplex()));

    }
    
}
