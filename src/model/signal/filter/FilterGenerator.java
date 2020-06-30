package model.signal.filter;

import controller.FilterDialogController;
import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class FilterGenerator extends Filter {

    public FilterGenerator(){
        super();
    }

    public FilterGenerator(String windowType, String filterType) {
        super(windowType, filterType);
    }

    @Override
    public HashMap<String, Signal> filter(Signal signal, double cutOffFrequency, int coefficientsCount) {
        HashMap<String, Signal> outputMap = new HashMap<>();
        Signal impulseResponseSignal = createImpulseResponseSignal(signal, coefficientsCount, cutOffFrequency);
        Signal resultSignal = convoluteSignals(impulseResponseSignal, signal);
        outputMap.put(Filter.IMPULSE_RESPONSE_SIGNAL, impulseResponseSignal);
        outputMap.put(Filter.FILTERED_SIGNAL, resultSignal);
        return outputMap;
    }

    @Override
    public Signal convoluteSignals(Signal primarySignal, Signal secondarySignal) {
        ArrayList<Sample> primarySamples = primarySignal.getSamples();
        ArrayList<Sample> secondarySamples = secondarySignal.getSamples();
        int convolutionComponentsMaxCount = Math.min(primarySamples.size(), secondarySamples.size());
        int samplesCount = primarySignal.getSamples().size() + secondarySamples.size() - 1;
        ArrayList<Sample> resultSamples = new ArrayList<>(samplesCount);
        double duration = Math.max(primarySignal.getDuration(), secondarySignal.getDuration());
        double samplesDistance = duration / samplesCount;
        double beginTime = Math.min(primarySignal.getSamples().get(0).time, secondarySamples.get(0).time);

        for (int sampleIndex = 0; sampleIndex < samplesCount; ++sampleIndex) {
            double convolutionValue = 0;
            double convolutionTime = beginTime + (sampleIndex * samplesDistance);
            for (int k = 0; k < convolutionComponentsMaxCount; ++k) {
                int secondaryIndex = secondarySamples.size() - 1 - sampleIndex + k;
                if(secondaryIndex >= secondarySamples.size() || secondaryIndex < 0) continue;

                double primaryValue = primarySamples.get(k).getValue();
                double secondaryValue = secondarySamples.get(secondaryIndex).getValue();

                convolutionValue += primaryValue * secondaryValue;
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
        double samplingFrequency = inputSignal.getFrequency(); //TODO check if that's it
        //TODO adjust f0 for band pass filter ?
        int samplesCount = inputSignal.getSamples().size();
        double duration = inputSignal.getDuration();
        double samplesDistance = duration / samplesCount;
        double beginTime = inputSignal.getSamples().get(0).time;

        if (filterType.equals(FilterDialogController.FILTER_TYPE_F2_VALUE)) {
            double fd = samplingFrequency / 4.0 - f0;
            double fg = samplingFrequency / 4.0 + f0;
        }
        double K = samplingFrequency / f0;
        double resultIndex = 0;
        for (int sampleIndex = 0; sampleIndex < M; sampleIndex++) {
            // calculate core sample value
            double sampleValue;
            if (sampleIndex == (M - 1) / 2) {
                sampleValue = 2 / K;
            } else {
                double denominator = Math.PI * (sampleIndex - (double) ((M - 1) / 2));
                double numerator = Math.sin(2 * denominator / K);
                sampleValue = numerator / denominator;
            }

            // multiply sample value with window coefficient
            sampleValue *= calculateWindowValue(sampleIndex, M);

            //multiply sample value with filtration type coefficient
            sampleValue *= getFilterTypeValue(sampleIndex);

            //calculate time value
            double timeValue = beginTime + (resultIndex * samplesDistance);

            Sample resultSample = new Sample(timeValue, sampleValue);
            if(Double.isFinite(resultSample.value)) resultSamples.add(resultSample);

            ++resultIndex;
        }
        Sample maxSample = resultSamples
                .stream()
                .max(Comparator.comparingDouble(Sample::getValue))
                .orElse(null);
        double amplitude = (maxSample != null) ? maxSample.getValue() : 0;
        return new Signal(resultSamples, duration, amplitude);
    }

    private double calculateWindowValue(int sampleIndex, double coefficientsCount) {
        if (this.windowType.equals(FilterDialogController.WINDOW_TYPE_O1_VALUE)) {
            return 1.0;
        } else {
            return 0.42 - 0.5 * Math.cos(2.0 * Math.PI * sampleIndex / coefficientsCount) + 0.08 * Math.cos(4.0 * Math.PI * sampleIndex / coefficientsCount);
        }
    }

    private double getFilterTypeValue(int sampleIndex) {
        if (this.filterType.equals(FilterDialogController.FILTER_TYPE_F1_VALUE)) {
            return 1.0;
        } else {
            return 2.0 * Math.sin(Math.PI * sampleIndex / 2.0);
        }
    }

    public Signal correlateSignals(Signal primarySignal, Signal secondarySignal, String correlationType) {
        ArrayList<Sample> primarySamples = primarySignal.getSamples();
        ArrayList<Sample> secondarySamples = secondarySignal.getSamples();
        if (Filter.CORRELATION_DIRECT_METHOD.equals(correlationType)) {
            int samplesCount = primarySamples.size();
            ArrayList<Sample> resultSamples = new ArrayList<>(samplesCount);
            double duration = Math.max(primarySignal.getDuration(), secondarySignal.getDuration());
            double samplesDistance = duration / (primarySignal.getSamples().size() + secondarySamples.size() - 1);
            double beginTime = Math.min(primarySamples.get(0).time, secondarySamples.get(0).time);
            double resultIndex = 0;
            for (int sampleIndex = 1 - secondarySamples.size(); sampleIndex < samplesCount; ++sampleIndex) {
                double convolutionValue = 0;
                double convolutionTime = beginTime + (resultIndex * samplesDistance);
                for (int k = 0; k < samplesCount; ++k) {
                    if (k - sampleIndex < 0 || k - sampleIndex >= secondarySamples.size()) {
                        continue;
                    }
                    convolutionValue += primarySamples.get(k).value * secondarySamples.get(k - sampleIndex).value;
                }
                Sample resultSample = new Sample(convolutionTime, convolutionValue);
                if (Double.isFinite(resultSample.value)) resultSamples.add(resultSample);
                ++resultIndex;
            }
            Sample maxSample = resultSamples
                    .stream()
                    .max(Comparator.comparingDouble(Sample::getValue))
                    .orElse(null);
            double amplitude = (maxSample != null) ? maxSample.getValue() : 0;
            double frequency = samplesCount / duration;
            return new Signal(resultSamples, duration, amplitude, frequency);
        } else {
            return convoluteSignals(primarySignal, secondarySignal);
        }
    }
}
