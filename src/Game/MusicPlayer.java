package Game;

import Client.Launcher;
import Client.Logger;
import Client.Settings;

import javax.sound.midi.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MusicPlayer implements Runnable {
    public static final String MUSIC_PATH = "/assets/music";

    private static String currentTrack = "";
    private static String switchTrack = "";

    private static Synthesizer synthesizer;
    private static Sequencer sequencer;
    private static Soundbank soundbank;
    private static Thread thread;

    private static boolean running = true;

    public void run()
    {
        while (running) {
            String zipPath = Settings.Dir.JAR + "/" + Settings.CUSTOM_MUSIC_PATH.get(Settings.currentProfile);

            boolean customMusic = Settings.CUSTOM_MUSIC.get(Settings.currentProfile);

            if (new File(zipPath).exists() && customMusic && !currentTrack.equals(switchTrack)) {
                // Play track
                Stop();
                if (switchTrack.length() > 0) {
                    Logger.Info("Playing music '" + switchTrack + "'");

                    InputStream input = null;

                    try
                    {
                        FileInputStream fis = new FileInputStream(zipPath);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        ZipInputStream zis = new ZipInputStream(bis);

                        ZipEntry ze;
                        while ((ze = zis.getNextEntry()) != null)
                        {
                            if (ze.getName().startsWith(switchTrack)) {
                                input = zis;
                                break;
                            }
                        }
                    } catch (Exception e) {}

                    Play(input);
                }

                currentTrack = switchTrack;
            }

            if (!customMusic)
            {
                currentTrack = "";
                switchTrack = "";
                Stop();
            }

            try { Thread.sleep(100); } catch (Exception e) {}
        }
    }

    public static void Init()
    {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();

            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
            soundbank = synthesizer.getDefaultSoundbank();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MusicPlayer player = new MusicPlayer();
        thread = new Thread(player);
        thread.start();
    }

    public static void resetSoundFont()
    {
        synthesizer.unloadAllInstruments(soundbank);
        soundbank = synthesizer.getDefaultSoundbank();
        synthesizer.loadAllInstruments(soundbank);
    }

    public static void loadSoundFont(String name)
    {
        Logger.Info("Loading soundfont '" + name + "'");

        String zipPath = Settings.Dir.JAR + "/" + Settings.CUSTOM_MUSIC_PATH.get(Settings.currentProfile);
        InputStream input = null;

        try
        {
            FileInputStream fis = new FileInputStream(zipPath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);

            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null)
            {
                if (ze.getName().equals(name + ".sf2")) {
                    input = zis;
                    break;
                }
            }
        } catch (Exception e) {}

        try {
            Soundbank newSoundbank = synthesizer.getDefaultSoundbank();
            if (input != null)
                newSoundbank = MidiSystem.getSoundbank(input);
            synthesizer.unloadAllInstruments(soundbank);
            soundbank = newSoundbank;
            synthesizer.loadAllInstruments(soundbank);
        } catch (Exception e) {
        }
    }

    public static void playTrack(String name)
    {
        // Track didn't change
        if (switchTrack.equals(name))
            return;

        // Switch track
        switchTrack = name;
    }

    public static void Play(InputStream inputStream)
    {
        Stop();

        try {
            sequencer.setSequence(inputStream);
            sequencer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Stop()
    {
        if (sequencer.isRunning())
            sequencer.stop();
    }

    public static void Close()
    {
        running = false;
        try { thread.join(); } catch (Exception e) {}
        sequencer.close();
        synthesizer.close();
    }
}
