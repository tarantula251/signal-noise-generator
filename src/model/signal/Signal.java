package model.signal;

import java.io.Serializable;
import java.util.ArrayList;

public class Signal implements Serializable {

    private String name;
    private ArrayList<Sample> samples;
    private double duration;
    private double amplitude;
    private double frequency;
    private double fillFactor;
    private double average;
    private double absoluteAverage;
    private double averagePower;
    private double effectiveValue;
    private double variance;

    @Override
    public String toString() {
        return  "name='" + name + '\'' +
                "\nduration=" + duration +
                "\namplitude=" + amplitude +
                "\nfrequency=" + frequency +
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
        for(Sample sample : samples)
        {
            average += sample.value;
        }
        average /= samples.size();
    }

    private void calculateAbsoluteAverage()
    {
        absoluteAverage = 0;
        for(Sample sample : samples)
        {
            absoluteAverage += Math.abs(sample.value);
        }
        absoluteAverage /= samples.size();
    }

    private void calculateAveragePower()
    {
        averagePower = 0;
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

    public Signal(ArrayList<Sample> samples, double duration, double amplitude, double frequency, double fillFactor) {
        this.samples = samples;
        this.duration = duration;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.fillFactor = fillFactor;
        calculateSignalParameters();
    }

    public Signal(ArrayList<Sample> samples, double duration, double amplitude) {
        this.samples = samples;
        this.duration = duration;
        this.amplitude = amplitude;
        calculateSignalParameters();
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

    private void calculateSignalParameters() {
        calculateAverage();
        calculateAbsoluteAverage();
        calculateAveragePower();
        calculateEffectiveValue();
        calculateVariance();
    }
}
