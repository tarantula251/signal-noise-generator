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
    private Signal postSamplingSignal = null;

    private ArrayList<Sample> calculateSamples(Signal signal, double frequency) throws ADConverterException {
        if(signal.getSamples().size() < 2)
        {
            throw new ADConverterException("Sygnał wejściowy ma niewystarczającą liczbę próbek.");
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
                        throw new ADConverterException("Zbyt mało próbek w sygnale wejściowym, aby obliczyć przekonwertowane próbki.");
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
    public Signal convert(Signal signal, double frequency) throws ADConverterException {
        ArrayList<Sample> samples = calculateSamples(signal, frequency);
        if(samples == null)
        {
            throw new ADConverterException("Błąd przy obliczaniu próbek.");
        }
        postSamplingSignal = new Signal(new ArrayList<>(samples), signal.getDuration(), signal.getAmplitude(), frequency);

        if(quantizer != null)
        {
            samples = quantizer.quantize(samples);
            if(samples == null)
            {
                throw new ADConverterException("Błąd kwantyzacji próbek.");
            }
        }

        Signal result = new Signal(samples, signal.getDuration(), signal.getAmplitude(), frequency);
        postSamplingSignal.calculateMeasurements(result);
        return result;
    }

    public Signal getPostSamplingSignal()
    {
        return postSamplingSignal;
    }

    public class ADConverterException extends Exception {
        public ADConverterException(String message) {
            super(message);
        }
    }
}
