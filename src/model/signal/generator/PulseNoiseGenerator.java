package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Random;

public class PulseNoiseGenerator implements SignalGenerator {

    @Override
    public Signal generateWithFillFactor(Double duration, Double beginTime, Double amplitude, Double frequency, Double probability) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = (int)(duration * frequency);
        double samplesDistance = duration / samplesCount;
        Random random = new Random();

        for(int i = 0; i <= samplesCount; ++i)
        {
            double time = beginTime + (i * samplesDistance);
            if (probability > random.nextDouble()) {
                samples.add(new Sample(time, amplitude));
            } else samples.add(new Sample(time, 0));
        }
        return new Signal(samples, duration, amplitude,true);
    }

    @Override
    public Signal generateWithJumpTime(Double duration, Double beginTime, Double amplitude, Double jumpTime) {
        return null;
    }

    @Override
    public Signal generateWithSampleNrForJump(Double duration, Double beginTime, Double amplitude, Double frequency, Integer sampleNumber) {
        return null;
    }

    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency) {
        return null;
    }
}
