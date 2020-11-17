/*
 * Copyright and license
 * Copyright (c) 2009 University of Helsinki
 *
 *	Permission is hereby granted, free of charge, to any person
 *	obtaining a copy of this software and associated documentation
 *	files (the "Software"), to deal in the Software without
 *	restriction, including without limitation the rights to use,
 *	copy, modify, merge, publish, distribute, sublicense, and/or
 *	sell copies of the Software, and to permit persons to whom the
 *	Software is furnished to do so, subject to the following
 *	conditions:
 *
 *	The above copyright notice and this permission notice shall be
 *	included in all copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * 	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *	OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *	NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *	HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *	WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *	OTHER DEALINGS IN THE SOFTWARE.
 *
 *  more information:
 *	http://www.opensource.org/licenses/mit-license.php
 */

package Client;

import java.util.zip.Checksum;

/**
 * A class for computing CRC-16 checksums. The generator polynomial is x^16 + x^15 + x^2 + 1.
 *
 * @author Pakkaamo
 */
public class CRC16 implements Checksum {

  /* The CRC-16 polynomial reflected */
  private static final int CRC16_POLY = 0xa001;

  private int reg;

  /** Creates a new CRC-16 instance. */
  public CRC16() {
    reset();
  }

  /**
   * Returns CRC-16 value.
   *
   * @return The current CRC-16 checksum value
   */
  public long getValue() {
    return (reg & 0xffff);
  }

  /** Resets CRC-16 to the initial value. */
  public void reset() {
    reg = 0;
  }

  /**
   * Updates CRC-16 with specified array of bytes.
   *
   * @param b The array of bytes to update the checksum with
   * @param off The start offset of the data
   * @param len The number of bytes to use
   */
  public void update(byte[] b, int off, int len) {

    for (int i = off; i < off + len; i++) {
      this.update(b[i]);
    }
  }

  /**
   * Updates CRC-16 with specified array of bytes.
   *
   * @param b The array of bytes to update the checksum with
   */
  public void update(byte[] b) {
    this.update(b, 0, b.length);
  }

  /**
   * Updates CRC-16 with specified byte.
   *
   * @param b The byte to update the checksum with
   */
  public void update(int b) {
    this.updateBits(b, 8);
  }

  /**
   * Updates CRC-16 with specified bit.
   *
   * @param b The bit to update the checksum with
   */
  public void updateBit(int b) {

    reg ^= (b != 0) ? 0x0001 : 0x0000;
    reg = (reg & 1) != 0 ? (reg >>> 1) ^ 0xa001 : (reg >>> 1);
  }

  /**
   * Updates CRC-16 with specified bits.
   *
   * @param word The bits to update the checksum with
   * @param len The number of the least significant bits to use
   */
  public void updateBits(int word, int len) {

    for (int i = 0; i < len; i++) {
      this.updateBit(word & 1);
      word >>>= 1;
    }
  }
}
