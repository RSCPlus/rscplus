package Game;

public class AreaDefinition {
  public static final int SIZE_X = 17;
  public static final int SIZE_Y = 19;
  public static final int SIZE_Y_ALL = SIZE_Y * 2;
  public static final AreaDefinition DEFAULT = new AreaDefinition("");

  public String music;

  public AreaDefinition(String music) {
    this.music = music;
  }
}
