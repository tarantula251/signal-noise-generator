package model.signal.converter;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Iterator;

public class ADConverter implements Converter
{
    public ADConverter(Quantizer quantizer)
    {
        this.quantizer = quantizer;
    }

    private Quantizer quantizer;

    private ArrayList<Sample> calculateSamples(Signal signal, double frequency)
    {
        if(signal.getSamples().size() < 2)
        {
            System.err.println("Input signal has not enough samples.");
            return null;
        }

        ArrayList<Sample> result = new ArrayList<>();
        Iterator<Sample> signalSamplesIterator = signal.getSamples().iterator();

        Sample leftSample = signalSamplesIterator.next();
        Sample rightSample = signalSamplesIterator.next();
        double originalSamplesDistance = Math.abs(leftSample.time - rightSample.time);
        double originalSamplesValueDiff = rightSample.value - leftSample.value;

        double duration = signal.getDuration();
        double beginTime = leftSample.time;
        int samplesCount = (int)(signal.getDuration() * frequency);
        double samplesDistance = duration / samplesCount;

        for(int i = 0; i < samplesCount; ++i)
        {
            double sampleTime = beginTime + i * samplesDistance;

            if(Signal.doubleComparator.compare(sampleTime, rightSample.time) > 0)
            {
                do
                {
                    leftSample = rightSample;
                    if(signalSamplesIterator.hasNext())
                    {
                        rightSample = signalSamplesIterator.next();
                    }
                    else
                    {
                        System.err.println("Not enough samples in input signal to calculate converted samples.");
                    }

                }
                while(Signal.doubleComparator.compare(sampleTime, rightSample.time) > 0);

                originalSamplesValueDiff = rightSample.value - leftSample.value;

            }

            double sampleValue = leftSample.value + ((sampleTime - leftSample.time) / originalSamplesDistance) * originalSamplesValueDiff;

            result.add(new Sample(sampleTime, sampleValue));
        }


        return result;
    }


    @Override
    public Signal convert(Signal signal, double frequency)
    {
        ArrayList<Sample> samples = calculateSamples(signal, frequency);
        if(samples == null)
        {
            System.err.println("Calculating samples failed.");
            return null;
        }

        samples = quantizer.quantize(samples);
        if(samples == null)
        {
            System.err.println("Samples quantization failed.");
            return null;
        }

        return new Signal(samples, signal.getDuration(), signal.getAmplitude(), frequency);
    }
}
