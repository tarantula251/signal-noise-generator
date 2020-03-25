package model.signal.generator;

import model.signal.Signal;

public interface SignalGenerator {

    Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency);
}
