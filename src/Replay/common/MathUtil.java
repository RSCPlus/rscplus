/**
 * rscminus
 *
 * <p>This file is part of rscminus.
 *
 * <p>rscminus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscminus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscminus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/RSCPlus/rscminus>
 */
package Replay.common;

public class MathUtil {
  public static int pow(int number, int power) {
    int ret = 1;
    while (power-- > 0) ret *= number;
    return ret;
  }

  public static int clamp(int number, int min, int max) {
    if (number > max) number = max;
    else if (number < min) number = min;
    return number;
  }

  public static int getBitmask(int count) {
    int ret = pow(2, count);
    return ret - 1;
  }

  public static int sign(int number) {
    if (number == 0) return 0;
    return (number > 0) ? 1 : -1;
  }
}
