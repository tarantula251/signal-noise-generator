package model.signal.filter;

import model.signal.Signal;

import java.util.HashMap;

public abstract class Filter {
    public static String IMPULSE_RESPONSE_SIGNAL = "impulseResponseSignal";
    public static String FILTERED_SIGNAL = "filteredSignal";

    public Filter(String windowType)
    {
        this.windowType = windowType;
    }

    private String windowType;

    public abstract HashMap<String, Signal> filter(Signal signal, double cutOffFrequency, int coefficientsCount, int samplesCount);

    public abstract Signal convoluteSignals(Signal primarySignal, Signal secondarySignal);
}
