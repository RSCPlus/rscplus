package Replay.scraper.client;

public final class Class11 {
  private int[] anIntArray208;
  private int[] anIntArray209;
  private byte[] aByteArray210;

  public Class11(byte[] var1) {
    int var2 = var1.length;
    this.aByteArray210 = var1;
    this.anIntArray208 = new int[var2];
    this.anIntArray209 = new int[8];
    int[] var3 = new int[33];
    int var4 = 0;

    for (int var5 = 0; var5 < var2; ++var5) {
      byte var6 = var1[var5];
      if (var6 != 0) {
        int var7 = 1 << 32 + -var6;
        int var8 = var3[var6];
        this.anIntArray208[var5] = var8;
        int var9;
        int var10;
        int var11;
        int var12;
        if ((var8 & var7) == 0) {
          var9 = var8 | var7;

          for (var10 = -1 + var6; var10 >= 1; --var10) {
            var11 = var3[var10];
            if (var11 != var8) {
              break;
            }

            var12 = 1 << 32 + -var10;
            if ((var11 & var12) != 0) {
              var3[var10] = var3[var10 - 1];
              break;
            }

            var3[var10] = Utility.bitwiseOr(var11, var12);
          }
        } else {
          var9 = var3[-1 + var6];
        }

        var3[var6] = var9;

        for (var10 = 1 + var6; var10 <= 32; ++var10) {
          if (var8 == var3[var10]) {
            var3[var10] = var9;
          }
        }

        var11 = 0;

        for (var12 = 0; var12 < var6; ++var12) {
          int var13 = Integer.MIN_VALUE >>> var12;
          if ((var8 & var13) != 0) {
            if (this.anIntArray209[var11] == 0) {
              this.anIntArray209[var11] = var4;
            }

            var11 = this.anIntArray209[var11];
          } else {
            ++var11;
          }

          var13 >>>= 1;
          if (var11 >= this.anIntArray209.length) {
            int[] var14 = new int[2 * this.anIntArray209.length];

            for (int var15 = 0; var15 < this.anIntArray209.length; ++var15) {
              var14[var15] = this.anIntArray209[var15];
            }

            this.anIntArray209 = var14;
          }
        }

        if (var11 >= var4) {
          var4 = var11 - -1;
        }

        this.anIntArray209[var11] = ~var5;
      }
    }
  }

  public final int method240(byte[] var1, int var2, byte[] var3, boolean var4, int var5, int var6) {
    if (var6 == 0) {
      return 0;
    } else {
      var6 += var2;
      int var7 = 0;
      if (!var4) {
        this.method240((byte[]) null, -4, (byte[]) null, false, -81, -40);
      }

      int var8 = var5;

      while (true) {
        byte var9 = var1[var8];
        if (var9 >= 0) {
          ++var7;
        } else {
          var7 = this.anIntArray209[var7];
        }

        int var10;
        if ((var10 = this.anIntArray209[var7]) < 0) {
          var3[var2++] = (byte) (~var10);
          if (var6 <= var2) {
            break;
          }

          var7 = 0;
        }

        if ((64 & var9) == 0) {
          ++var7;
        } else {
          var7 = this.anIntArray209[var7];
        }

        if ((var10 = this.anIntArray209[var7]) < 0) {
          var3[var2++] = (byte) (~var10);
          if (var2 >= var6) {
            break;
          }

          var7 = 0;
        }

        if ((var9 & 32) != 0) {
          var7 = this.anIntArray209[var7];
        } else {
          ++var7;
        }

        if ((var10 = this.anIntArray209[var7]) < 0) {
          var3[var2++] = (byte) (~var10);
          if (var2 >= var6) {
            break;
          }

          var7 = 0;
        }

        if ((var9 & 16) == 0) {
          ++var7;
        } else {
          var7 = this.anIntArray209[var7];
        }

        if ((var10 = this.anIntArray209[var7]) < 0) {
          var3[var2++] = (byte) (~var10);
          if (var2 >= var6) {
            break;
          }

          var7 = 0;
        }

        if ((var9 & 8) == 0) {
          ++var7;
        } else {
          var7 = this.anIntArray209[var7];
        }

        if ((var10 = this.anIntArray209[var7]) < 0) {
          var3[var2++] = (byte) (~var10);
          if (var6 <= var2) {
            break;
          }

          var7 = 0;
        }

        if ((4 & var9) == 0) {
          ++var7;
        } else {
          var7 = this.anIntArray209[var7];
        }

        if ((var10 = this.anIntArray209[var7]) < 0) {
          var3[var2++] = (byte) (~var10);
          if (var2 >= var6) {
            break;
          }

          var7 = 0;
        }

        if ((2 & var9) != 0) {
          var7 = this.anIntArray209[var7];
        } else {
          ++var7;
        }

        if ((var10 = this.anIntArray209[var7]) < 0) {
          var3[var2++] = (byte) (~var10);
          if (var2 >= var6) {
            break;
          }

          var7 = 0;
        }

        if ((var9 & 1) == 0) {
          ++var7;
        } else {
          var7 = this.anIntArray209[var7];
        }

        if ((var10 = this.anIntArray209[var7]) < 0) {
          var3[var2++] = (byte) (~var10);
          if (var6 <= var2) {
            break;
          }

          var7 = 0;
        }

        ++var8;
      }

      return -var5 + var8 - -1;
    }
  }

  final int method241(int var1, int var2, byte[] var3, byte[] var4, int var5, int var6) {
    int var7 = 0;
    var2 += var1;

    int var8;
    for (var8 = var6 << 3; var1 < var2; ++var1) {
      int var9 = var4[var1] & 255;
      int var10 = this.anIntArray208[var9];
      byte var11 = this.aByteArray210[var9];
      if (var11 == 0) {
        throw new RuntimeException("No codeword for data value " + var9);
      }

      int var12 = var8 >> 3;
      int var13 = var8 & 7;
      var7 &= -var13 >> 31;
      int var14 = (var11 + var13 + -1 >> 3) + var12;
      var8 += var11;
      var13 += 24;
      var3[var12] = (byte) (var7 = Utility.bitwiseOr(var7, var10 >>> var13));
      if (var14 > var12) {
        ++var12;
        var13 -= 8;
        var3[var12] = (byte) (var7 = var10 >>> var13);
        if (var12 < var14) {
          ++var12;
          var13 -= 8;
          var3[var12] = (byte) (var7 = var10 >>> var13);
          if (var14 > var12) {
            var13 -= 8;
            ++var12;
            var3[var12] = (byte) (var7 = var10 >>> var13);
            if (var12 < var14) {
              ++var12;
              var13 -= 8;
              var3[var12] = (byte) (var7 = var10 << -var13);
            }
          }
        }
      }
    }

    if (var5 != 18695) {
      // GameData.dataInteger = null;
    }

    return (7 + var8 >> 3) - var6;
  }
}
