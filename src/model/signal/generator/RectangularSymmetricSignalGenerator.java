package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class RectangularSymmetricSignalGenerator implements SignalGenerator {
    private static final int SAMPLES_PER_PERIOD = 48;

    @Override
    public Signal generateWithFillFactor(Double duration, Double beginTime, Double amplitude, Double frequency, Double fillFactor) {
        ArrayList<Sample> samples = new ArrayList<>();
        double period = 1 / frequency;
        int samplesCount = (int)(duration / (period / SAMPLES_PER_PERIOD));
        double samplesDistance = duration / samplesCount;
        for(int i = 0; i < samplesCount; ++i)
        {
            int K = i  / SAMPLES_PER_PERIOD;
            double time = beginTime + (i * samplesDistance);
            double leftBound = K * period + beginTime;
            double rightBound = fillFactor * period + leftBound;
            if (time >= leftBound && time < rightBound) {
                samples.add(new Sample(time, amplitude));
            } else samples.add(new Sample(time, -amplitude));
        }
        return new Signal(samples, duration, amplitude, frequency, fillFactor);
    }

    @Override
    public Signal generateWithJumpTime(Double duration, Double beginTime, Double amplitude, Double jumpTime) {
        return null;
    }

    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency) {
        return null;
    }
}
