package Game;

import Client.Logger;
import Client.Settings;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sound.midi.*;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MusicPlayer implements Runnable {
  private static MusicDef currentTrack = MusicDef.NONE;
  private static MusicDef switchTrack = MusicDef.NONE;

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
          stop();
          if (switchTrack.filename.length() > 0) {
            Logger.Info("Playing music '" + switchTrack.filename + "'");

            InputStream input = null;

            try {
              FileInputStream fis = new FileInputStream(zipPath);
              BufferedInputStream bis = new BufferedInputStream(fis);
              ZipInputStream zis = new ZipInputStream(bis);

              ZipEntry ze;
              while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().equals(switchTrack.filename)) {
                  input = zis;
                  break;
                }
              }
            } catch (Exception e) {
            }
            if (switchTrack.filetype.equals("mid")) {
              playMidi(input);
            } else {
              try {
                if (null != input) {
                  Logger.Info("Loading " + switchTrack.filename + "." + switchTrack.filetype);
                  Sound.play(Sound.loadSound(new BufferedInputStream(input)), false);
                }
              } catch (UnsupportedAudioFileException | IOException e) {
                e.printStackTrace();
              }
            }
          }

          currentTrack = switchTrack;
        } else {
          setVolume(getVolume() - 0.03);
        }
      }

      if (!customMusic) {
        currentTrack = MusicDef.NONE;
        switchTrack = MusicDef.NONE;
        stop();
      }

      // TODO: Make this customizable, it's music repeat
      if (sequencer != null) {
        if (Client.state == Client.STATE_GAME) sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        else sequencer.setLoopCount(0);
      }

      if (synthesizer != null) {
        try {
          int volumeInt =
              1; // (int) (volume * 127.0); // TODO: this is the minimum volume achievable with this
          // method but it should be even quieter

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

  public static void init() {
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

  public static void playTrack(MusicDef track) {
    // Track didn't change
    if (switchTrack.equals(track.filename)) return;

    // Switch track
    switchTrack = track;
  }

  public static void playMidi(InputStream inputStream) {
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

    setVolume(Settings.MIDI_VOLUME);
  }

  public static void stop() {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.stop();
      sequencer.close();
      synthesizer.close();
    }
  }

  public static void close() {
    running = false;
    try {
      thread.join();
    } catch (Exception e) {
    }
    stop();
  }
}
