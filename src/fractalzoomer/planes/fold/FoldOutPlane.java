/* 
 * Fractal Zoomer, Copyright (C) 2020 hrkalona2
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fractalzoomer.planes.fold;

import fractalzoomer.core.*;
import fractalzoomer.core.mpfr.LibMpfr;
import fractalzoomer.planes.Plane;

/**
 *
 * @author hrkalona2
 */
public class FoldOutPlane extends Plane {

    private Complex center;
    private BigComplex ddcenter;

    private MpfrBigNumComplex mpfrbncenter;

    private DDComplex ddccenter;

    public FoldOutPlane(double plane_transform_radius) {

        super();
        center = new Complex(plane_transform_radius, 0);
        if(ThreadDraw.PERTURBATION_THEORY || ThreadDraw.HIGH_PRECISION_CALCULATION) {
            ddcenter = new BigComplex(center);
            ddccenter = new DDComplex(center);
            if (ThreadDraw.USE_BIGNUM_FOR_REF_IF_POSSIBLE || ThreadDraw.HIGH_PRECISION_CALCULATION) {

                if(LibMpfr.LOAD_ERROR == null) {
                    mpfrbncenter = new MpfrBigNumComplex(center);
                }
            }
        }

    }

    @Override
    public Complex transform(Complex pixel) {
        
        return pixel.fold_out(center);

    }

    @Override
    public BigComplex transform(BigComplex pixel) {

        return pixel.fold_out(ddcenter);

    }

    @Override
    public MpfrBigNumComplex transform(MpfrBigNumComplex pixel) {

        return pixel.fold_out(mpfrbncenter);

    }

    @Override
    public DDComplex transform(DDComplex pixel) {

        return pixel.fold_out(ddccenter);

    }
}
