package model.signal.generator;

import model.signal.Signal;

public interface SignalGenerator {

    Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency);

    Signal generateWithFillFactor(Double duration, Double beginTime, Double amplitude, Double frequency, Double fillFactor);

    Signal generateWithJumpTime(Double duration, Double beginTime, Double amplitude, Double jumpTime);

    Signal generateWithSampleNrForJump(Double duration, Double beginTime, Double amplitude, Double frequency, Integer sampleNumber);

}
