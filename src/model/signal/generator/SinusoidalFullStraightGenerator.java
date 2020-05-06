package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class SinusoidalFullStraightGenerator implements SignalGenerator {
    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency, Double period, Double fillFactor, Double jumpTime, Integer sampleNumber, Double probability) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = (int)(duration * frequency);
        double samplesDistance = duration / samplesCount;

        for(int i = 0; i < samplesCount; ++i)
        {
            double angleVal = 2 * Math.PI * (i * samplesDistance - beginTime) / period;
            samples.add(new Sample(beginTime + (i * samplesDistance),
                    amplitude * Math.abs((Math.sin(angleVal)))));
        }
        return new Signal(samples, duration, amplitude, frequency);
    }
}
