package model.signal;

import java.io.Serializable;

public class Sample implements Serializable {

    public Sample(double time, double value) {
        this.time = time;
        this.value = value;
    }

    @Override
    public String toString() {
        return "\nSample{" +
                "time=" + time +
                ", value=" + value +
                '}';
    }

    public double time;
    public double value;

    public double getValue() {
        return this.value;
    }

    public double getTime() {
        return this.time;
    }
}
