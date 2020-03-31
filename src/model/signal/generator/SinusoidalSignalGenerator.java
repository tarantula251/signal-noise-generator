package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class SinusoidalSignalGenerator implements SignalGenerator {
    private static final int SAMPLES_COUNTER = 100;
    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency) {
        ArrayList<Sample> samples = new ArrayList<>();
        double period = 1 / frequency;
        int samplesCount = SAMPLES_COUNTER;
        double samplesDistance = duration / samplesCount;

        for(double i = 0; i < samplesCount; i+=0.1)
        {
            samples.add(new Sample(beginTime + (i * samplesDistance), amplitude * Math.sin(2 * Math.PI * (i * samplesDistance - beginTime) / period)));
        }
        return new Signal(samples, duration, amplitude, frequency);
    }

    @Override
    public Signal generateWithFillFactor(Double duration, Double beginTime, Double amplitude, Double frequency, Double fillFactor) {
        return null;
    }
}
