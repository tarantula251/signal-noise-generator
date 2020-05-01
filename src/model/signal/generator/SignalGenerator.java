package model.signal.generator;

import model.signal.Signal;

import java.util.Comparator;

public interface SignalGenerator {
    Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency, Double period, Double fillFactor, Double jumpTime, Integer sampleNumber, Double probability);
}
