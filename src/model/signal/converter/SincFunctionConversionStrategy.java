package model.signal.converter;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class SincFunctionConversionStrategy implements ConversionStrategy
{
    private int samplesCount;

    public SincFunctionConversionStrategy(int samplesCount)
    {
        this.samplesCount = samplesCount;
    }

    private double sinc(double x)
    {
        if(Signal.doubleComparator.compare(x, .00000) == 0) return 1;
        else return Math.sin(Math.PI * x) / (Math.PI * x);
    }

    private double getSampleValueForTime(Signal signal, double time, double samplesDistance)
    {
        double value = 0;

        for(int i = 0; i < signal.getSamples().size(); ++i)
        {
            Sample sample = signal.getSamples().get(i);
            if(Signal.doubleComparator.compare(sample.time, time) < 0 && i < signal.getSamples().size() - 1) continue;

            for(int leftIndex = i - 1; leftIndex >= i - samplesCount; --leftIndex)
            {
                if(leftIndex < 0) break;
                value += signal.getSamples().get(leftIndex).value * sinc(time / samplesDistance - leftIndex);
            }

            for(int rightIndex = i; rightIndex < i + samplesCount; ++rightIndex)
            {
                if(rightIndex > signal.getSamples().size() - 1) break;
                value += signal.getSamples().get(rightIndex).value * sinc(time / samplesDistance - rightIndex);
            }
            break;
        }
        return value;
    }


    @Override
    public Signal convert(Signal signal, double frequency)
    {
        if(signal.getSamples().size() < 2)
        {
            System.err.println("Not enough samples to reconstruct signal.");
            return null;
        }

        ArrayList<Sample> samples = new ArrayList<>();

        Sample firstSample = signal.getSamples().get(0);
        double beginTime = firstSample.time;
        double duration = signal.getDuration();
        int samplesCount = (int)(signal.getDuration() * frequency);
        double samplesDistance = duration / samplesCount;
        double originalSamplesDistance = duration / signal.getSamples().size();

        for(int i = 0; i < samplesCount; ++i)
        {
            double sampleTime = beginTime + i * samplesDistance;
            double sampleValue = getSampleValueForTime(signal, sampleTime, originalSamplesDistance);
            samples.add(new Sample(sampleTime, sampleValue));
        }

        return new Signal(samples, duration, signal.getAmplitude(), frequency);
    }
}
