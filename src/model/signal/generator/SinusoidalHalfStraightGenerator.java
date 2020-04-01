package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class SinusoidalHalfStraightGenerator implements SignalGenerator {
    private static final int SAMPLES_PER_PERIOD = 48;
    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency) {
        ArrayList<Sample> samples = new ArrayList<>();
        double period = 1 / frequency;
        int samplesCount = (int)(duration / (period / SAMPLES_PER_PERIOD));
        double samplesDistance = duration / samplesCount;

        for(int i = 0; i <= samplesCount; ++i)
        {
            double angleVal = 2 * Math.PI * (i * samplesDistance - beginTime) / period;
            samples.add(new Sample(beginTime + (i * samplesDistance),
                    0.5 * amplitude * (Math.sin(angleVal) + Math.abs(Math.sin(angleVal)))));
        }
        return new Signal(samples, duration, amplitude, frequency);
    }

    @Override
    public Signal generateWithFillFactor(Double duration, Double beginTime, Double amplitude, Double frequency, Double fillFactor) {
        return null;
    }
}
