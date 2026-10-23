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

        double bpm = 60.0 / (bestLag * windowSeconds);
        double[] beatTimes = trackBeats(onset, bestLag, bestPhase, onsetMean, windowSeconds);
        double offsetSeconds = beatTimes.length == 0 ? bestPhase * windowSeconds : beatTimes[0];
        return new Result(bpm, offsetSeconds, beatTimes);
    }

    private static double[] trackBeats(double[] onset, int initialLag, int initialPhase, double onsetMean, double windowSeconds) {
        ArrayList<Double> beats = new ArrayList<>();
        double interval = initialLag * windowSeconds;
        double targetSeconds = initialPhase * windowSeconds;
        double previousOnsetSeconds = Double.NaN;

        while (targetSeconds < onset.length * windowSeconds) {
            int center = (int) Math.round(targetSeconds / windowSeconds);
            int searchRadius = Math.max(1, (int) Math.round(interval * 0.18 / windowSeconds));
            int start = Math.max(0, center - searchRadius);
            int end = Math.min(onset.length - 1, center + searchRadius);
            int strongestFrame = center;
            double strongestOnset = 0;

            for (int frame = start; frame <= end; frame++) {
                if (onset[frame] > strongestOnset) {
                    strongestOnset = onset[frame];
                    strongestFrame = frame;
                }
            }

            if (strongestOnset >= onsetMean * 1.5 && strongestOnset > 0) {
                double detectedSeconds = strongestFrame * windowSeconds;
                double correction = (detectedSeconds - targetSeconds) * 0.4;
                targetSeconds += Math.max(-interval * 0.08, Math.min(interval * 0.08, correction));

                if (!Double.isNaN(previousOnsetSeconds)) {
                    double observedInterval = detectedSeconds - previousOnsetSeconds;
                    if (observedInterval >= interval * 0.8 && observedInterval <= interval * 1.2) {
                        interval += (observedInterval - interval) * 0.12;
                    }
                }
                previousOnsetSeconds = detectedSeconds;
            }

            beats.add(targetSeconds);
            targetSeconds += interval;
        }

        double[] beatTimes = new double[beats.size()];
        for (int i = 0; i < beats.size(); i++) {
            beatTimes[i] = beats.get(i);
        }
        return beatTimes;
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
