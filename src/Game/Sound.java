package Game;

import java.io.*;
import javax.sound.sampled.*;

public class Sound {
  byte[] samples;
  AudioFormat format;

  public Sound(byte[] samples, AudioFormat format) {
    this.samples = samples;
    this.format = format;
  }

  public byte[] getSamples() {
    return this.samples;
  }

  public AudioFormat getFormat() {
    return this.format;
  }

  public static Sound loadSound(BufferedInputStream file)
      throws UnsupportedAudioFileException, IOException {
    AudioInputStream baseStream = AudioSystem.getAudioInputStream(file);
    AudioFormat baseFormat = baseStream.getFormat();
    AudioFormat decodedFormat =
        new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baseFormat.getSampleRate(),
            16,
            baseFormat.getChannels(),
            baseFormat.getChannels() * 2,
            baseFormat.getSampleRate(),
            false);
    AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, baseStream);
    // Buffer audio data from audio stream
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int nBytesRead = 0;
    try {
      while (nBytesRead != -1) {
        nBytesRead = decodedStream.read(buffer, 0, buffer.length);
        // Write read bytes to out stream
        if (nBytesRead > 0) {
          byteOut.write(buffer, 0, nBytesRead);
        }
      }
    } catch (IndexOutOfBoundsException ex) {
      ex.printStackTrace();
    }

    // close streams and cleanup
    byte[] samples = byteOut.toByteArray();

    return new Sound(samples, decodedFormat);
  }

  public static void play(Sound sound) {
    InputStream source = new ByteArrayInputStream(sound.getSamples());
    AudioFormat format = sound.getFormat();
    // use a short, 100ms (1/10th sec) buffer for real-time changes to the sound stream
    int bufferSize = format.getFrameSize() * Math.round(format.getSampleRate() / 10);
    byte[] buffer = new byte[bufferSize];

    // create a line to play to
    SourceDataLine line;
    try {
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(format, bufferSize);
    } catch (LineUnavailableException ex) {
      ex.printStackTrace();
      return;
    }

    // start the line
    line.start();

    // copy data to the line
    try {
      int numBytesRead = 0;
      while (numBytesRead != -1) {
        numBytesRead = source.read(buffer, 0, buffer.length);
        if (numBytesRead != -1) {
          line.write(buffer, 0, numBytesRead);
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ArrayIndexOutOfBoundsException ignored) {
    }

    // wait until all data is played, then close the line
    line.drain();
    line.close();
  }
}
