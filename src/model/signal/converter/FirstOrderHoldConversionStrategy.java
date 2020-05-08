package model.signal.converter;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Iterator;

public class FirstOrderHoldConversionStrategy implements  ConversionStrategy
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

        double originalSamplesDistance = Math.abs(rightSample.time - leftSample.time);
        double originalSamplesValueDiff = rightSample.value - leftSample.value;
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
                do
                {
                    if(signalSamplesIterator.hasNext())
                    {
                        rightSample = signalSamplesIterator.next();
                        originalSamplesValueDiff = rightSample.value - leftSample.value;
                        originalSamplesDistance = Math.abs(rightSample.time - leftSample.time);
                    }
                    else rightSample = null;
                }
                while(rightSample != null && Signal.doubleComparator.compare(leftSample.value, rightSample.value) == 0);
            }

            double sampleValue = leftSample.value + ((sampleTime - leftSample.time) / originalSamplesDistance) * originalSamplesValueDiff;

            samples.add(new Sample(sampleTime, sampleValue));
        }


        return new Signal(samples, duration, signal.getAmplitude(), frequency);
    }
}
