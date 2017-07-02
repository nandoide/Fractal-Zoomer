/* 
 * Fractal Zoomer, Copyright (C) 2017 hrkalona2
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

package fractalzoomer.bailout_tests;

import fractalzoomer.core.Complex;

/**
 *
 * @author hrkalona2
 */
public class SquareBailoutTest extends BailoutTest {
 
    public SquareBailoutTest(double bound) {
        
        super(bound);
        
    }
    
     @Override //infinity norm   
     public boolean escaped(Complex z, Complex zold, Complex zold2, int iterations, Complex c, Complex start, Complex[] vars) {
         
        return z.getAbsRe() >= bound || z.getAbsIm() >= bound;
         
     }
    
}
