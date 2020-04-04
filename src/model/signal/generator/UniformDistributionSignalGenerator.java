package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Random;

public class UniformDistributionSignalGenerator implements SignalGenerator {
    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency, Double period, Double fillFactor, Double jumpTime, Integer sampleNumber, Double probability) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = (int)(duration * frequency);
        double samplesDistance = duration / samplesCount;

        double min = -amplitude;
        double max = amplitude;

        Random random = new Random();

        for(int i = 0; i < samplesCount; ++i)
        {
            samples.add(new Sample(beginTime + (i * samplesDistance), random.nextDouble() * ((max - min) + 1) + min));
        }

        samples.get(random.nextInt(samples.size())).value = random.nextDouble() >= 0.5 ? max : min;

        return new Signal(samples, duration, amplitude, frequency);
    }
}
