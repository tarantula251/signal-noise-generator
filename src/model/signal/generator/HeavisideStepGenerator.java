package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class HeavisideStepGenerator implements SignalGenerator {
    private static final int SAMPLES_COUNT = 136;

    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency, Double period, Double fillFactor, Double jumpTime, Integer sampleNumber, Double probability) {
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
}
