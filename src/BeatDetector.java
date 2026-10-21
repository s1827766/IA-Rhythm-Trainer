import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class BeatDetector {
    private static final double WINDOW_SECONDS = 0.01;
    private static final int MIN_BPM = 60;
    private static final int MAX_BPM = 200;

    public static class Result {
        public final double bpm;
        public final double offsetSeconds;

        public Result(double bpm, double offsetSeconds) {
            this.bpm = bpm;
            this.offsetSeconds = offsetSeconds;
        }
    }

    public static Result analyze(File file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        try (AudioInputStream source = AudioSystem.getAudioInputStream(file)) {
            float sampleRate = source.getFormat().getSampleRate();

            AudioFormat monoPcm16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);

            try (AudioInputStream pcm = AudioSystem.getAudioInputStream(monoPcm16, source)) {
                byte[] audio = readAll(pcm);
                return estimate(audio, sampleRate);
            }
        }
    }

    private static Result estimate(byte[] audio, float sampleRate) {
        int samplesPerWindow = Math.max(1, (int) (sampleRate * WINDOW_SECONDS));
        int sampleCount = audio.length / 2;
        int windowCount = sampleCount / samplesPerWindow;

        if (windowCount < 2) {
            throw new IllegalArgumentException("WAV is too short to analyze.");
        }

        double[] energy = new double[windowCount];

        for (int window = 0; window < windowCount; window++) {
            int start = window * samplesPerWindow;
            int end = Math.min(start + samplesPerWindow, sampleCount);
            double sumSquares = 0;

            for (int sampleIndex = start; sampleIndex < end; sampleIndex++) {
                int byteIndex = sampleIndex * 2;
                short sample = (short) ((audio[byteIndex + 1] << 8) | (audio[byteIndex] & 0xff));
                double value = sample / 32768.0;
                sumSquares += value * value;
            }

            energy[window] = Math.sqrt(sumSquares / (end - start));
        }

        // Keep only increases in energy; these are rough onset candidates.
        double[] onset = new double[windowCount];
        double onsetMean = 0;

        for (int i = 1; i < windowCount; i++) {
            onset[i] = Math.max(0, energy[i] - energy[i - 1]);
            onsetMean += onset[i];
        }
        onsetMean /= windowCount;

        int minLag = (int) (60.0 / (MAX_BPM * WINDOW_SECONDS));
        int maxLag = (int) (60.0 / (MIN_BPM * WINDOW_SECONDS));
        maxLag = Math.min(maxLag, windowCount / 2);

        int bestLag = -1;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int lag = minLag; lag <= maxLag; lag++) {
            double score = 0;

            for (int i = lag; i < windowCount; i++) {
                score += (onset[i] - onsetMean) * (onset[i - lag] - onsetMean);
            }

            score /= (windowCount - lag);

            if (score > bestScore) {
                bestScore = score;
                bestLag = lag;
            }
        }

        if (bestLag < 0) {
            throw new IllegalArgumentException("Could not estimate a beat interval.");
        }

        // Find the strongest repeating phase within the estimated beat interval.
        int bestPhase = 0;
        double strongestPhase = Double.NEGATIVE_INFINITY;

        for (int phase = 0; phase < bestLag; phase++) {
            double score = 0;

            for (int i = phase; i < windowCount; i += bestLag) {
                score += onset[i];
            }

            if (score > strongestPhase) {
                strongestPhase = score;
                bestPhase = phase;
            }
        }

        double bpm = 60.0 / (bestLag * WINDOW_SECONDS);
        double offsetSeconds = bestPhase * WINDOW_SECONDS;
        return new Result(bpm, offsetSeconds);
    }

    private static byte[] readAll(AudioInputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;

        while ((count = stream.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }

        return output.toByteArray();
    }
}
