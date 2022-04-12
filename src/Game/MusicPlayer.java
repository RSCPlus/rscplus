package Game;

import Client.Logger;
import Client.Settings;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sound.midi.*;

public class MusicPlayer implements Runnable {
  private static String currentTrack = "";
  private static String switchTrack = "";

  private static Synthesizer synthesizer;
  private static Sequencer sequencer;
  private static Soundbank soundbank;
  private static Thread thread;

  private static boolean running = true;

  private static double volume = 0.0;

  public void run() {
    while (running) {
      String zipPath =
          Settings.Dir.JAR + "/" + Settings.CUSTOM_MUSIC_PATH.get(Settings.currentProfile);

      boolean customMusic = Settings.CUSTOM_MUSIC.get(Settings.currentProfile);

      if (!Replay.isSeeking
          && new File(zipPath).exists()
          && customMusic
          && !currentTrack.equals(switchTrack)) {
        // Play track
        if (getVolume() == 0.0) {
          Stop();
          if (switchTrack.length() > 0) {
            Logger.Info("Playing music '" + switchTrack + "'");

            InputStream input = null;

            try {
              FileInputStream fis = new FileInputStream(zipPath);
              BufferedInputStream bis = new BufferedInputStream(fis);
              ZipInputStream zis = new ZipInputStream(bis);

              ZipEntry ze;
              while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().equals(switchTrack)) {
                  input = zis;
                  break;
                }
              }
            } catch (Exception e) {
            }

            Play(input);
          }

          currentTrack = switchTrack;
        } else {
          setVolume(getVolume() - 0.03);
        }
      }

      if (!customMusic) {
        currentTrack = "";
        switchTrack = "";
        Stop();
      }

      // TODO: Make this customizable, it's music repeat
      if (sequencer != null) {
        if (Client.state == Client.STATE_GAME) sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        else sequencer.setLoopCount(0);
      }

      if (synthesizer != null) {
        try {
          int volumeInt = (int) (volume * 127.0);

          if (synthesizer != null) {
            MidiChannel[] channels = synthesizer.getChannels();
            for (MidiChannel channel : channels) {
              channel.controlChange(7, volumeInt);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      try {
        Thread.sleep(100);
      } catch (Exception e) {
      }
    }
  }

  public static void Init() {
    MusicPlayer player = new MusicPlayer();
    thread = new Thread(player);
    thread.start();
  }

  public static void loadSoundFont(String name) {
    Logger.Info("Loading soundfont '" + name + "'");

    String zipPath =
        Settings.Dir.JAR + "/" + Settings.CUSTOM_MUSIC_PATH.get(Settings.currentProfile);
    InputStream input = null;

    try {
      FileInputStream fis = new FileInputStream(zipPath);
      BufferedInputStream bis = new BufferedInputStream(fis);
      ZipInputStream zis = new ZipInputStream(bis);

      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        if (ze.getName().equals(name + ".sf2")) {
          input = zis;
          break;
        }
      }
    } catch (Exception e) {
    }

    try {
      soundbank = null;
      if (input != null) soundbank = MidiSystem.getSoundbank(input);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public double getVolume() {
    return volume;
  }

  public static void setVolume(double value) {
    volume = Math.max(0.0, value);
  }

  public static void playTrack(String name) {
    // Track didn't change
    if (switchTrack.equals(name)) return;

    // Switch track
    switchTrack = name;
  }

  public static void Play(InputStream inputStream) {
    try {
      synthesizer = MidiSystem.getSynthesizer();
      synthesizer.open();

      sequencer = MidiSystem.getSequencer(true);

      if (soundbank != null) {
        synthesizer.unloadAllInstruments(synthesizer.getDefaultSoundbank());
        synthesizer.loadAllInstruments(soundbank);
      }

      sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
      sequencer.open();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      sequencer.setTickPosition(0);
      sequencer.setSequence(inputStream);

      sequencer.start();
    } catch (Exception e) {
      e.printStackTrace();
    }

    setVolume(1.0);
  }

  public static void Stop() {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.stop();
      sequencer.close();
      synthesizer.close();
    }
  }

  public static void Close() {
    running = false;
    try {
      thread.join();
    } catch (Exception e) {
    }
    Stop();
  }
}
