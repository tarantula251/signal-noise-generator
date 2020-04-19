package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class UnitPulseGenerator implements SignalGenerator {

    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency, Double period, Double fillFactor, Double jumpTime, Integer sampleNumber, Double probability) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = (int)(duration * frequency);
        double samplesDistance = duration / samplesCount;

        for(int i = 0; i < samplesCount; ++i)
        {
            double time = beginTime + (i * samplesDistance);
            if (time == sampleNumber) {
                samples.add(new Sample(time, amplitude));
            } else samples.add(new Sample(time, 0));
        }
        Signal result = new Signal(samples, duration, amplitude,frequency);
        result.setContinuous(false);
        return result;
    }
}
