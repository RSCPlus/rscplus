package Game;

import Client.Logger;

public class SoundEffects {
  static final int PLAY_SOUND = 204;

  public static boolean processPacket(int opcode, int psize) {
    boolean processed = false;
    if (opcode == PLAY_SOUND) {
      return readPlaySoundPacket(psize);
    }

    return processed;
  }

  private static boolean readPlaySoundPacket(int psize) {
    StringBuilder yummy = new StringBuilder();

    byte[] bufferByteArray = StreamUtil.getBufferByteArray(Client.packetsIncoming);
    int offset = StreamUtil.getBufferOffset(Client.packetsIncoming);

    for (int i = offset; i < offset + psize - 1; i++) {
      yummy.append((char) (bufferByteArray[i] & 0xFF));
    }
    Logger.Info(yummy.toString());

    if (yummy.toString().contains("combat1")) { // TODO: settings
      return true;
    }
    return false;
  }
}
