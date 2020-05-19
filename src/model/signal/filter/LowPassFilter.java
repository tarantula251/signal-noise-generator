package model.signal.filter;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class LowPassFilter extends Filter {

    public LowPassFilter(String windowType) {
        super(windowType);
    }

    @Override
    public HashMap<String, Signal> filter(Signal signal, double cutOffFrequency, int coefficientsCount, int samplesCount) {
        HashMap<String, Signal> outputMap = new HashMap<>();
        Signal impulseResponseSignal = createImpulseResponseSignal(signal, coefficientsCount, cutOffFrequency);
        Signal resultSignal = convoluteSignals(impulseResponseSignal, signal);
        outputMap.put(Filter.IMPULSE_RESPONSE_SIGNAL, impulseResponseSignal);
        outputMap.put(Filter.FILTERED_SIGNAL, resultSignal);
        return outputMap;
    }

    @Override
    public Signal convoluteSignals(Signal primarySignal, Signal secondarySignal) {
        ArrayList<Sample> secondarySamples = secondarySignal.getSamples();
        int samplesCount = primarySignal.getSamples().size() + secondarySamples.size() - 1;
        ArrayList<Sample> resultSamples = new ArrayList<>(samplesCount);
        double duration = Math.max(primarySignal.getDuration(), secondarySignal.getDuration());
        double samplesDistance = duration / samplesCount;
        double beginTime = Math.min(primarySignal.getSamples().get(0).time, secondarySamples.get(0).time);

        for (int sampleIndex = 0; sampleIndex <= samplesCount - 1; ++sampleIndex) {
            double convolutionValue = 0;
            double convolutionTime = beginTime + (sampleIndex * samplesDistance);
            double primarySampleValue, secondarySampleValue;
            for (int k = 0; k < samplesCount; ++k) {
                if (k < primarySignal.getSamples().size()) {
                    primarySampleValue = primarySignal.getSamples().get(k).value;
                } else {
                    primarySampleValue = 0;
                }
                if ((sampleIndex - k < 0) || (sampleIndex - k >= secondarySamples.size())) {
                    secondarySampleValue = 0;
                } else {
                    secondarySampleValue = secondarySamples.get(sampleIndex - k).value;
                }
                convolutionValue += primarySampleValue * secondarySampleValue;
            }
            Sample resultSample = new Sample(convolutionTime, convolutionValue);
            if (Double.isFinite(resultSample.value)) resultSamples.add(resultSample);
        }
        Sample maxSample = resultSamples
                .stream()
                .max(Comparator.comparingDouble(Sample::getValue))
                .orElse(null);
        double amplitude = (maxSample != null) ? maxSample.getValue() : 0;
        double frequency = samplesCount / duration;

        return new Signal(resultSamples, duration, amplitude, frequency);
    }

    private Signal createImpulseResponseSignal(Signal inputSignal, int M, double f0) {
        ArrayList<Sample> resultSamples = new ArrayList<>(M);
        for(int sampleIndex = 0; sampleIndex < M; sampleIndex++) {
            double sampleValue = 0;
            double K = inputSignal.getFrequency() / f0;
            if (sampleIndex == (M - 1) / 2) {
                sampleValue = 2 / K;
            } else {
                double denominator = Math.PI * (sampleIndex - (double) ((M - 1) / 2));
                double numerator = Math.sin(2 * denominator / K);
                sampleValue = numerator / denominator;
            }
            Sample resultSample = new Sample(sampleIndex, sampleValue);
            if(Double.isFinite(resultSample.value)) resultSamples.add(resultSample);
        }
        // check below
        double duration = M;
        Sample maxSample = resultSamples
                .stream()
                .max(Comparator.comparingDouble(Sample::getValue))
                .orElse(null);
        double amplitude = (maxSample != null) ? maxSample.getValue() : 0;
        double frequency = M / duration;
        return new Signal(resultSamples, duration, amplitude, frequency);
    }
}
