package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Random;

public class GaussianSignalGenerator implements SignalGenerator {
    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = (int)(duration * frequency);
        double samplesDistance = duration / samplesCount;

        final int div = 10; //Not sure what value should it be. I used 10 for testing and it seems to work well.

        double min = -amplitude;
        double max = amplitude;

        Random random = new Random();

        for(int i = 0; i < samplesCount; ++i)
        {
            double value = 0;
            for(int j = 0; j < div; ++j)
            {
                value += random.nextDouble() * ((max/div - min/div) + 1) + min/div;
            }
            samples.add(new Sample(beginTime + (i * samplesDistance), value));
        }

        samples.get(random.nextInt(samples.size())).value = random.nextDouble() >= 0.5 ? max : min;

        return new Signal(samples, duration, amplitude, frequency);
    }

    @Override
    public Signal generateWithFillFactor(Double duration, Double beginTime, Double amplitude, Double frequency, Double fillFactor) {
        return null;
    }

    @Override
    public Signal generateWithJumpTime(Double duration, Double beginTime, Double amplitude, Double jumpTime) {
        return null;
    }
}
