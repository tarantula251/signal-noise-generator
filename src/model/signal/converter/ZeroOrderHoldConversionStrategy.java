package model.signal.converter;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Iterator;

public class ZeroOrderHoldConversionStrategy implements ConversionStrategy
{
    @Override
    public Signal convert(Signal signal, double frequency)
    {
        if(signal.getSamples().size() < 2)
        {
            System.err.println("Input signal has not enough samples.");
            return null;
        }

        ArrayList<Sample> samples = new ArrayList<>();
        Iterator<Sample> signalSamplesIterator = signal.getSamples().iterator();

        Sample leftSample = signalSamplesIterator.next();
        Sample rightSample = signalSamplesIterator.next();

        double duration = signal.getDuration();
        double beginTime = leftSample.time;
        int samplesCount = (int)(signal.getDuration() * frequency);
        double samplesDistance = duration / samplesCount;

        for(int i = 0; i < samplesCount; ++i)
        {
            double sampleTime = beginTime + i * samplesDistance;

            if(rightSample != null && Signal.doubleComparator.compare(sampleTime, rightSample.time) > 0)
            {
                leftSample = rightSample;
                if(signalSamplesIterator.hasNext())
                {
                    rightSample = signalSamplesIterator.next();
                }
                else rightSample = null;
            }

            samples.add(new Sample(sampleTime, leftSample.value));
        }


        return new Signal(samples, duration, signal.getAmplitude(), frequency);
    }
}
