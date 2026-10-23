import javax.sound.sampled.*;
import java.io.*;

public class Sound {

	private Clip clip;
	private Clip loopClip;

	public void loadMusic(String musicfile) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		if (clip != null) {
			clip.close();
		}

		try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(musicfile))) {
			clip = AudioSystem.getClip();
			clip.open(inputStream);
		}
	}

	public void playPreparedMusic() {
		if (clip == null) {
			throw new IllegalStateException("Music has not been prepared.");
		}
		clip.setFramePosition(0);
		clip.start();
	}

	public void resetMusic() {
		if (clip != null) {
			clip.stop();
			clip.setFramePosition(0);
		}
	}

	private void setVolume(Clip c, float volume) {
		if (c != null && c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl gainControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
			float dB = (float) (Math.log(volume == 0.0f ? 0.0001f : volume) / Math.log(10.0) * 20.0);
			gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
		}
	}

	public void playmusic(String musicfile) {
		playmusic(musicfile, 1.0f);
	}

	public void playmusic(String musicfile, float volume) {
		File soundFile = new File(musicfile);
		try {
			if (musicfile.equals("stop")) {
				if (clip != null) {
					clip.stop();
					// System.out.print("stopped");
				}
			} else {
				AudioInputStream inputStream;
				inputStream = AudioSystem.getAudioInputStream(soundFile);
				clip = AudioSystem.getClip();
				clip.open(inputStream);
				setVolume(clip, volume);
				clip.start();
			}
		}

		catch (Exception e) {
			System.out.println(e);
		}

	}

	public void loopmusic(String musicfile) {
		loopmusic(musicfile, 1.0f);
	}

	public void loopmusic(String musicfile, float volume) {
		File soundFile = new File(musicfile);
		try {
			if (musicfile.equals("stop")) {
				if (loopClip != null) {
					loopClip.stop();
					// System.out.print("stopped");
				}
			} else {
				AudioInputStream inputStream = AudioSystem.getAudioInputStream(soundFile);
				loopClip = AudioSystem.getClip();
				loopClip.open(inputStream);
				setVolume(loopClip, volume);
				loopClip.loop(Clip.LOOP_CONTINUOUSLY);
			}
		}

		catch (Exception e) {
			System.out.println(e);
		}

	}

	public double getPlaybackSeconds() {
		return clip == null ? 0 : clip.getMicrosecondPosition() / 1_000_000.0;
	}

	public double getMusicLengthSeconds() {
		return clip == null ? 0 : clip.getMicrosecondLength() / 1_000_000.0;
	}
}
