package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class RectangularSymmetricSignalGenerator implements SignalGenerator {

    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency, Double period, Double fillFactor, Double jumpTime, Integer sampleNumber, Double probability) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = (int)(duration * frequency);
        int samplesPerPeriod = (int)(samplesCount / (duration / period));
        double samplesDistance = duration / samplesCount;
        for(int i = 0; i <= samplesCount; ++i)
        {
            int K = i  / samplesPerPeriod;
            double time = beginTime + (i * samplesDistance);
            double leftBound = K * period + beginTime;
            double rightBound = fillFactor * period + leftBound;
            if (time >= leftBound && time < rightBound) {
                samples.add(new Sample(time, amplitude));
            } else samples.add(new Sample(time, -amplitude));
        }
        return new Signal(samples, duration, amplitude, frequency, fillFactor);
    }
}
