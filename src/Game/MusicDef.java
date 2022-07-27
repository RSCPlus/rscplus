package Game;

public class MusicDef {
  public String trackname;
  public String filename;
  public String filetype;
  public static final MusicDef NONE = new MusicDef("", "", "");

  public MusicDef(String trackname, String filename, String filetype) {
    this.trackname = trackname;
    this.filename = filename;
    this.filetype = filetype;
  }
}
