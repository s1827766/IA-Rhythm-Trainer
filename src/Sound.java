import javax.swing.*;
import javax.sound.sampled.*;
import java.io.*;

@SuppressWarnings("unused")
public class Sound {

	private Clip clip;
	private Clip loopClip;

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
				if (Math.random() < 0.001) {
					inputStream = AudioSystem.getAudioInputStream(new File("sounds\\snd_pombark.wav"));
				} else {
					inputStream = AudioSystem.getAudioInputStream(soundFile);
				}
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

}
