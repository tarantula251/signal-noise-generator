package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Random;

public class GaussianSignalGenerator implements SignalGenerator {
    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency, Double period, Double fillFactor, Double jumpTime, Integer sampleNumber, Double probability) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = (int)(duration * frequency);
        double samplesDistance = duration / samplesCount;

        final int div = 2; //The lower value is, the flatter will be histogram

        double min = -amplitude;

        Random random = new Random();

        for(int i = 0; i < samplesCount; ++i)
        {
            double value = 0;
            for(int j = 0; j < div; ++j)
            {
                value += (random.nextDouble() * Math.abs(amplitude * 2) + min)/div;
            }
            samples.add(new Sample(beginTime + (i * samplesDistance), value));
        }

        return new Signal(samples, duration, amplitude, frequency);
    }
}
