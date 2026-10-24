import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.*;

public class BeatDetector {
    private static final double WINDOW_SECONDS = 0.01;
    private static final int MIN_BPM = 60;
    private static final int MAX_BPM = 200;

    public static class Result {
        public final double bpm;
        public final double offsetSeconds;
        public final double[] beatTimes;

        public Result(double bpm, double offsetSeconds) {
            this(bpm, offsetSeconds, new double[0]);
        }

        public Result(double bpm, double offsetSeconds, double[] beatTimes) {
            this.bpm = bpm;
            this.offsetSeconds = offsetSeconds;
            this.beatTimes = beatTimes.clone();
        }

        public Result withBpm(double adjustedBpm) {
            double scale = bpm / adjustedBpm;
            double[] adjustedBeatTimes = new double[beatTimes.length];
            for (int i = 0; i < beatTimes.length; i++) {
                adjustedBeatTimes[i] = offsetSeconds + (beatTimes[i] - offsetSeconds) * scale;
            }
            return new Result(adjustedBpm, offsetSeconds, adjustedBeatTimes);
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
        double windowSeconds = samplesPerWindow / sampleRate;
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

        int minLag = (int) (60.0 / (MAX_BPM * windowSeconds));
        int maxLag = (int) (60.0 / (MIN_BPM * windowSeconds));
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

        // bestLag is only accurate to about +-1 BPM, so refine it using the whole song.
        double lowBpm = 60.0 / ((bestLag + 1) * windowSeconds);
        double highBpm = 60.0 / (Math.max(1, bestLag - 1) * windowSeconds);
        double[] best = fit(onset, windowSeconds, lowBpm, highBpm, 0.005);

        // Autocorrelation often lands on half the real tempo. Prefer double unless it
        // fits much worse.
        if (best[0] * 2 <= MAX_BPM) {
            double[] faster = fit(onset, windowSeconds, best[0] * 2 - 0.1, best[0] * 2 + 0.1, 0.005);
            if (faster[2] >= best[2] * 0.85) {
                best = faster;
            }
        }

        return new Result(best[0], best[1]);
    }

    // Tries many BPM / start-time combinations and returns {bpm, offsetSeconds,
    // score}
    // for the grid that lands on the strongest onsets across the WHOLE song.
    private static double[] fit(double[] onset, double windowSeconds, double minBpm, double maxBpm, double bpmStep) {
        double bestScore = -1;
        double bestBpm = minBpm;
        double bestOffset = 0;

        for (double bpm = minBpm; bpm <= maxBpm; bpm += bpmStep) {
            double interval = 60.0 / bpm / windowSeconds; // beat spacing in windows (not rounded)

            for (double phase = 0; phase < interval; phase += 0.5) {
                double score = 0;
                int beats = 0;

                for (double pos = phase; pos < onset.length - 1; pos += interval) {
                    score += onset[(int) Math.round(pos)];
                    beats++;
                }

                score /= beats;

                if (score > bestScore) {
                    bestScore = score;
                    bestBpm = bpm;
                    bestOffset = phase * windowSeconds;
                }
            }
        }

        return new double[] { bestBpm, bestOffset, bestScore };
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
