package model.signal;

import java.util.ArrayList;

public class Signal {

    private String name;
    private ArrayList<Sample> samples;
    private double duration;
    private double amplitude;
    private double frequency;

    public Signal(ArrayList<Sample> samples, double duration, double amplitude, double frequency) {
        this.samples = samples;
        this.duration = duration;
        this.amplitude = amplitude;
        this.frequency = frequency;
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
}
