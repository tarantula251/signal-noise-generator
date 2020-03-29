package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class SinusoidalSignalGenerator implements SignalGenerator {
    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency) {
        ArrayList<Sample> samples = new ArrayList<>();
        double period = 1 / frequency;
        int samplesCount = (int) (duration * frequency);
        double samplesDistance = 0.1;
        double endTime = duration - beginTime;

        for(int i = 0; i < samplesCount; ++i)
        {
            samples.add(new Sample(beginTime + (i * samplesDistance), amplitude * Math.sin(2 * Math.PI * (beginTime + (i * samplesDistance) - beginTime)) / period));
        }
        return new Signal(samples, duration, amplitude, frequency);
    }
}
