package Game;

import Client.Launcher;
import Client.Logger;
import Client.Settings;
import java.io.BufferedInputStream;
import java.io.IOException;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundEffects {
  static final int PLAY_SOUND =
      204; // handling here to selectively enable/disable all sounds except prayeron/off
  static final int SET_SETTINGS = 240; // handling here to better control setting sounds_disabled
  public static boolean sounds_disabled;

  public static SourceDataLine mudClientSourceDataLine;

  public static Sound combat1;

  public static void loadCustomSoundEffects() {
    try {
      combat1 =
          Sound.loadSound(new BufferedInputStream(Launcher.getResourceAsStream("/assets/web.wav")));
      Logger.Info("Loaded combat1 sound effect");
    } catch (UnsupportedAudioFileException | IOException e) {
      Logger.Error("Could not load combat1 sound effect!");
      e.printStackTrace();
    }

    // TODO: sound effect packs
  }

  public static boolean processPacket(int opcode, int psize) {
    boolean processed = false;
    if (opcode == PLAY_SOUND) {
      return readPlaySoundPacket(psize);
    }

    if (opcode == SET_SETTINGS) {
      return readServerSettingsPacket(psize);
    }

    return processed;
  }

  private static boolean readServerSettingsPacket(int psize) {
    byte[] bufferByteArray = StreamUtil.getBufferByteArray(Client.packetsIncoming);
    int offset = StreamUtil.getBufferOffset(Client.packetsIncoming);
    if (null != bufferByteArray && 3 == psize - offset) {
      Camera.auto = (bufferByteArray[offset++] & 0xFF) == 1;
      Client.singleButtonMode = (bufferByteArray[offset++] & 0xFF) == 1;
      if (Settings.OVERRIDE_AUDIO_SETTING.get(Settings.currentProfile)) {
        sounds_disabled = !Settings.OVERRIDE_AUDIO_SETTING_SETTING_ON.get(Settings.currentProfile);
        if (Replay.timestamp_server_last
            > 100) { // client has been logged in sufficient enough time to display this warning
          Client.displayMessage(
              "@whi@Server-side @yel@Sound effects@whi@ setting set to: "
                  + (((bufferByteArray[offset] & 0xFF) == 1) ? "@red@off" : "@gre@on"),
              Client.CHAT_QUEST);
          Client.displayMessage(
              "@whi@You can configure the "
                  + Launcher.binaryPrefix
                  + "RSC+ Sound effect override in the @yel@Audio tab of Settings.",
              Client.CHAT_QUEST);
        }
      } else {
        sounds_disabled = (bufferByteArray[offset] & 0xFF) == 1;
      }
      return true;
    }
    return false;
  }

  private static boolean readPlaySoundPacket(int psize) {
    StringBuilder yummy = new StringBuilder();
    byte[] bufferByteArray = StreamUtil.getBufferByteArray(Client.packetsIncoming);
    int offset = StreamUtil.getBufferOffset(Client.packetsIncoming);
    for (int i = offset; i < offset + psize - 1; i++) {
      if (null != bufferByteArray && bufferByteArray[i] != 0)
        yummy.append((char) (bufferByteArray[i] & 0xFF));
    }

    if (isEnabled(yummy.toString())) {
      playSound(yummy.toString());
    }
    return true;
  }

  public static void playSound(String filename) {
    if (filename.equals("combat1")) {
      if (!sounds_disabled) Sound.play(combat1, true);
      return;
    }

    // play the sound effect using the original method
    playSoundReflectionCall(filename);
  }

  private static void playSoundReflectionCall(String filename) {
    try {
      final int garbageMustBeLessThanNeg43 = -128;
      Reflection.playSound.invoke(Client.instance, garbageMustBeLessThanNeg43, filename);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static boolean isEnabled(String soundEffectName) {
    switch (soundEffectName) {
      case "combat1":
        return Settings.SOUND_EFFECT_COMBAT1.get(Settings.currentProfile);
      case "advance":
        return Settings.SOUND_EFFECT_ADVANCE.get(Settings.currentProfile);
      case "anvil":
        return Settings.SOUND_EFFECT_ANVIL.get(Settings.currentProfile);
      case "chisel":
        return Settings.SOUND_EFFECT_CHISEL.get(Settings.currentProfile);
      case "click":
        return Settings.SOUND_EFFECT_CLICK.get(Settings.currentProfile);
      case "closedoor":
        return Settings.SOUND_EFFECT_CLOSEDOOR.get(Settings.currentProfile);
      case "coins":
        return Settings.SOUND_EFFECT_COINS.get(Settings.currentProfile);
      case "combat1a":
        return Settings.SOUND_EFFECT_COMBAT1A.get(Settings.currentProfile);
      case "combat1b":
        return Settings.SOUND_EFFECT_COMBAT1B.get(Settings.currentProfile);
      case "combat2a":
        return Settings.SOUND_EFFECT_COMBAT2A.get(Settings.currentProfile);
      case "combat2b":
        return Settings.SOUND_EFFECT_COMBAT2B.get(Settings.currentProfile);
      case "combat3a":
        return Settings.SOUND_EFFECT_COMBAT3A.get(Settings.currentProfile);
      case "combat3b":
        return Settings.SOUND_EFFECT_COMBAT3B.get(Settings.currentProfile);
      case "cooking":
        return Settings.SOUND_EFFECT_COOKING.get(Settings.currentProfile);
      case "death":
        return Settings.SOUND_EFFECT_DEATH.get(Settings.currentProfile);
      case "dropobject":
        return Settings.SOUND_EFFECT_DROPOBJECT.get(Settings.currentProfile);
      case "eat":
        return Settings.SOUND_EFFECT_EAT.get(Settings.currentProfile);
      case "filljug":
        return Settings.SOUND_EFFECT_FILLJUG.get(Settings.currentProfile);
      case "fish":
        return Settings.SOUND_EFFECT_FISH.get(Settings.currentProfile);
      case "foundgem":
        return Settings.SOUND_EFFECT_FOUNDGEM.get(Settings.currentProfile);
      case "mechanical":
        return Settings.SOUND_EFFECT_MECHANICAL.get(Settings.currentProfile);
      case "mine":
        return Settings.SOUND_EFFECT_MINE.get(Settings.currentProfile);
      case "mix":
        return Settings.SOUND_EFFECT_MIX.get(Settings.currentProfile);
      case "opendoor":
        return Settings.SOUND_EFFECT_OPENDOOR.get(Settings.currentProfile);
      case "outofammo":
        return Settings.SOUND_EFFECT_OUTOFAMMO.get(Settings.currentProfile);
      case "potato":
        return Settings.SOUND_EFFECT_POTATO.get(Settings.currentProfile);
      case "prayeroff":
        return Settings.SOUND_EFFECT_PRAYEROFF.get(Settings.currentProfile);
      case "prayeron":
        return Settings.SOUND_EFFECT_PRAYERON.get(Settings.currentProfile);
      case "prospect":
        return Settings.SOUND_EFFECT_PROSPECT.get(Settings.currentProfile);
      case "recharge":
        return Settings.SOUND_EFFECT_RECHARGE.get(Settings.currentProfile);
      case "retreat":
        return Settings.SOUND_EFFECT_RETREAT.get(Settings.currentProfile);
      case "secretdoor":
        return Settings.SOUND_EFFECT_SECRETDOOR.get(Settings.currentProfile);
      case "shoot":
        return Settings.SOUND_EFFECT_SHOOT.get(Settings.currentProfile);
      case "spellfail":
        return Settings.SOUND_EFFECT_SPELLFAIL.get(Settings.currentProfile);
      case "spellok":
        return Settings.SOUND_EFFECT_SPELLOK.get(Settings.currentProfile);
      case "takeobject":
        return Settings.SOUND_EFFECT_TAKEOBJECT.get(Settings.currentProfile);
      case "underattack":
        return Settings.SOUND_EFFECT_UNDERATTACK.get(Settings.currentProfile);
      case "victory":
        return Settings.SOUND_EFFECT_VICTORY.get(Settings.currentProfile);
      default:
        return true;
    }
  }

  public static void adjustMudClientSfxVolume() {
    if (mudClientSourceDataLine == null) {
      return;
    }

    adjustSfxVolume(mudClientSourceDataLine);
  }

  public static void adjustSfxVolume(SourceDataLine sourceDataLine) {
    try {
      if (sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        float volumePercent = Settings.SFX_VOLUME.get(Settings.currentProfile);

        FloatControl gainControl =
            (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);

        if (Settings.LOUDER_SOUND_EFFECTS.get(Settings.currentProfile)) {
          volumePercent *= 2;
        }

        float volumeInDecibels = 20f * (float) Math.log10(volumePercent / 100f);

        gainControl.setValue(volumeInDecibels);
      }
    } catch (Exception e) {
      Logger.Error("Error adjusting the sfx volume");
      e.printStackTrace();
    }
  }
}
