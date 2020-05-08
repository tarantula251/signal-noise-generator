package model.signal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.DoubleBinaryOperator;

public class Signal implements Serializable {

    private String name;
    private ArrayList<Sample> samples;
    
    //Parameters
    private double duration;
    private double amplitude;
    private double frequency;
    private double period;
    private double fillFactor;
    private double average;
    private double absoluteAverage;
    private double averagePower;
    private double effectiveValue;
    private double variance;
    private boolean continuous = true;
    
    //Measurements
    private double meanSquareError;
    private double signalNoiseRatio;
    private double peakSignalNoiseRation;
    private double maximumDifference;

    public static Comparator<Double> doubleComparator = new Comparator<Double>() {
        @Override
        public int compare(Double aDouble, Double t1) {
            return Math.abs(aDouble - t1) < 0.0000000001 ? 0 : Double.compare(aDouble, t1);
        }
    };

    @Override
    public String toString() {
        return  "name='" + name + '\'' +
                "\nduration=" + duration +
                "\namplitude=" + amplitude +
                "\nfrequency=" + frequency +
                "\nperiod=" + period +
                "\nfillFactor=" + fillFactor +
                "\naverage=" + average +
                "\nabsoluteAverage=" + absoluteAverage +
                "\naveragePower=" + averagePower +
                "\neffectiveValue=" + effectiveValue +
                "\nvariance=" + variance +
                "\nsamples=" + samples;
    }

    private void calculateAverage()
    {
        average = 0;
        if(samples.isEmpty()) return;
        for(Sample sample : samples)
        {
            average += sample.value;
        }
        average /= samples.size();
    }

    private void calculateAbsoluteAverage()
    {
        absoluteAverage = 0;
        if(samples.isEmpty()) return;
        for(Sample sample : samples)
        {
            absoluteAverage += Math.abs(sample.value);
        }
        absoluteAverage /= samples.size();
    }

    private void calculateAveragePower()
    {
        averagePower = 0;
        if(samples.isEmpty()) return;
        for(Sample sample : samples)
        {
            averagePower += Math.pow(sample.value, 2);
        }
        averagePower /= samples.size();
    }

    private void calculateEffectiveValue()
    {
        effectiveValue = Math.sqrt(averagePower);
    }

    private void calculateVariance()
    {
        variance = 0;
        if(samples.isEmpty()) return;
        for(Sample sample : samples)
        {
            variance += Math.pow(sample.value - average, 2);
        }
        variance /= samples.size();
    }

    public Signal(ArrayList<Sample> samples, double duration, double amplitude, double frequency) {
        this.samples = samples;
        this.duration = duration;
        this.amplitude = amplitude;
        this.frequency = frequency;
        calculateSignalParameters();
    }

    public Signal(ArrayList<Sample> samples, double duration, double amplitude, double frequency, double period) {
        this.samples = samples;
        this.duration = duration;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.period = period;
        calculateSignalParameters();
    }

    public Signal(ArrayList<Sample> samples, double duration, double amplitude, double frequency, double period, double fillFactor) {
        this.samples = samples;
        this.duration = duration;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.period = period;
        this.fillFactor = fillFactor;
        calculateSignalParameters();
    }

    public Signal(ArrayList<Sample> samples, double duration, double amplitude) {
        this.samples = samples;
        this.duration = duration;
        this.amplitude = amplitude;
        calculateSignalParameters();
    }

    public void calculateMeasurements(Signal signal)
    {
        if(samples.isEmpty()) return;
        if(samples.size() != signal.samples.size()) return;
        double meanSquareValueDiff = 0;
        double valueSquaresSum = 0;
        double maxValue = samples.get(0).value;
        double maxMeanValueDiff = samples.get(0).value - signal.samples.get(0).value;
        for(int i = 0; i < samples.size(); ++i)
        {
            Sample sample = samples.get(i);
            Sample sampleToCompare = signal.samples.get(i);

            double sampleMeanValueDiff = sample.value - sampleToCompare.value;
            maxMeanValueDiff = Math.max(maxMeanValueDiff, sampleMeanValueDiff);
            maxValue = Math.max(maxValue, sample.value);
            valueSquaresSum += Math.pow(sample.value, 2);
            meanSquareValueDiff += Math.pow(sampleMeanValueDiff, 2);
        }

        meanSquareError = meanSquareValueDiff / samples.size();
        signalNoiseRatio = 10 * Math.log10(valueSquaresSum / meanSquareValueDiff);
        peakSignalNoiseRation = 10 * Math.log10(maxValue / meanSquareError);
        maximumDifference = maxMeanValueDiff;
        signal.meanSquareError = meanSquareError;
        signal.signalNoiseRatio = signalNoiseRatio;
        signal.peakSignalNoiseRation = peakSignalNoiseRation;
        signal.maximumDifference = maximumDifference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Sample> getSamples() {
        return samples;
    }

    public double getDuration() {
        return duration;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getPeriod() {
        return period;
    }

    public double getFillFactor() {
        return fillFactor;
    }

    public double getAverage() {
        return average;
    }

    public double getAbsoluteAverage() {
        return absoluteAverage;
    }

    public double getAveragePower() {
        return averagePower;
    }

    public double getEffectiveValue() {
        return effectiveValue;
    }

    public double getVariance() {
        return variance;
    }

    public double getMeanSquareError()
    {
        return meanSquareError;
    }

    public double getSignalNoiseRatio()
    {
        return signalNoiseRatio;
    }

    public double getPeakSignalNoiseRation()
    {
        return peakSignalNoiseRation;
    }

    public double getMaximumDifference()
    {
        return maximumDifference;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    private void calculateSignalParameters() {
        calculateAverage();
        calculateAbsoluteAverage();
        calculateAveragePower();
        calculateEffectiveValue();
        calculateVariance();
    }

    private boolean isCompatible(Signal signal)
    {
        return signal.getDuration() == duration && signal.getFrequency() == frequency && signal.getSamples().size() == samples.size();
    }

    private Signal performAction(Signal signal, DoubleBinaryOperator operator) throws SignalException {
        if(!isCompatible(signal)) throw new SignalException("Signals are not compatible");

        ArrayList<Sample> resultSamples = new ArrayList<>();
        for(int sampleIndex = 0; sampleIndex < samples.size(); ++sampleIndex)
        {
            Sample leftSample = samples.get(sampleIndex);
            Sample rightSample = signal.getSamples().get(sampleIndex);
            Sample resultSample = new Sample(leftSample.time, operator.applyAsDouble(leftSample.value, rightSample.value));
            if(Double.isFinite(resultSample.value)) resultSamples.add(resultSample);
        }

        return new Signal(resultSamples, duration,  amplitude, frequency, fillFactor);
}

    public Signal add(Signal signal) throws SignalException {
        Signal result = performAction(signal, Double::sum);
        result.amplitude += signal.amplitude;
        return result;
    }

    public Signal subtract(Signal signal) throws SignalException {
        Signal result = performAction(signal, (a, b) -> a-b);
        result.amplitude += signal.amplitude;
        return result;
    }

    public Signal multiply(Signal signal) throws SignalException {
        Signal result = performAction(signal, (a, b) -> a*b);
        result.amplitude *= signal.amplitude;
        return result;
    }

    public Signal divide(Signal signal) throws SignalException {
        return performAction(signal, (a, b) -> a/b);
    }
}
