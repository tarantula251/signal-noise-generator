package model.signal;

import java.util.ArrayList;

public class Signal {

    private String name;
    private ArrayList<Sample> samples;

    public Signal(ArrayList<Sample> samples) {
        this.samples = samples;
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
}
