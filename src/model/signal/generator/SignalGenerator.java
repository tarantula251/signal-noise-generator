package model.signal.generator;

import model.signal.Signal;

import java.util.Comparator;

public interface SignalGenerator {

    Comparator<Double> doubleComparator = new Comparator<Double>() {
        @Override
        public int compare(Double aDouble, Double t1) {
            return Math.abs(aDouble - t1) < 0.0000000001 ? 0 : Double.compare(aDouble, t1);
        }
    };

    Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency, Double period, Double fillFactor, Double jumpTime, Integer sampleNumber, Double probability);
}
