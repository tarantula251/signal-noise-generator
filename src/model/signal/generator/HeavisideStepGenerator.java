package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class HeavisideStepGenerator implements SignalGenerator {
    private static final int SAMPLES_COUNT = 136;

    @Override
    public Signal generateWithJumpTime(Double duration, Double beginTime, Double amplitude, Double jumpTime) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = (int)(duration * SAMPLES_COUNT);
        double samplesDistance = duration / samplesCount;

        for(int i = 0; i <= samplesCount; ++i)
        {
            double time = beginTime + (i * samplesDistance);
            double value;
            if (time > jumpTime) {
                value = amplitude;
            } else if (time == jumpTime) {
                value = 0.5 * amplitude;
            } else {
                value = 0;
            }
            samples.add(new Sample(time, value));
        }
        return new Signal(samples, duration, amplitude);
    }

    @Override
    public Signal generateWithSampleNrForJump(Double duration, Double beginTime, Double amplitude, Double frequency, Integer sampleNumber) {
        return null;
    }

    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency) {
        return null;
    }

    @Override
    public Signal generateWithFillFactor(Double duration, Double beginTime, Double amplitude, Double frequency, Double fillFactor) {
        return null;
    }
}
