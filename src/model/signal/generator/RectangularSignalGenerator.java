package model.signal.generator;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class RectangularSignalGenerator implements SignalGenerator {
    private static final int SAMPLES_COUNTER = 100;

    @Override
    public Signal generateWithFillFactor(Double duration, Double beginTime, Double amplitude, Double frequency, Double fillFactor) {
        ArrayList<Sample> samples = new ArrayList<>();
        double period = 1 / frequency;
        int samplesCount = SAMPLES_COUNTER;
        double samplesDistance = duration / samplesCount;

        //TODO fix
        for(double i = 0; i < samplesCount; i+=0.1)
        {
            int K = 1;
            double time = beginTime + (i * samplesDistance);
            double amplitudeLeftLimit = K * period + beginTime;
            double rightLimit = fillFactor * period + K + period + beginTime;
            double zeroLeftLimit = fillFactor * period - K * period + beginTime;

            if (time >= amplitudeLeftLimit && time < rightLimit) {
                samples.add(new Sample(time, amplitude));
            } else if (time >= zeroLeftLimit && time < rightLimit) {
                samples.add(new Sample(time, 0));
            }
        }
        return new Signal(samples, duration, amplitude, frequency, fillFactor);
    }

    @Override
    public Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency) {
        return null;
    }
}
