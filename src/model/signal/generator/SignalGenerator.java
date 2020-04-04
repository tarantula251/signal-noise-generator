package model.signal.generator;

import model.signal.Signal;

public interface SignalGenerator {

    Signal generate(Double duration, Double beginTime, Double amplitude, Double frequency, Double period, Double fillFactor, Double jumpTime, Integer sampleNumber, Double probability);

//    Signal generateWithFillFactor(Double duration, Double beginTime, Double amplitude, Double frequency, Double fillFactor);
//
//    Signal generateWithJumpTime(Double duration, Double beginTime, Double amplitude, Double jumpTime);
//
//    Signal generateWithSampleNrForJump(Double duration, Double beginTime, Double amplitude, Double frequency, Integer sampleNumber);

}
