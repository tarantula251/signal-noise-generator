package model.signal.filter;

import model.signal.Signal;

import java.util.HashMap;

public abstract class Filter {
    public static String IMPULSE_RESPONSE_SIGNAL = "impulseResponseSignal";
    public static String FILTERED_SIGNAL = "filteredSignal";

    public static String CORRELATION_DIRECT_METHOD = "DM";
    public static String CORRELATION_CONVOLUTION_METHOD = "CM";

    public Filter(String windowType, String filterType)
    {
        this.windowType = windowType;
        this.filterType = filterType;
    }

    public String windowType;

    public String filterType;

    public abstract HashMap<String, Signal> filter(Signal signal, double cutOffFrequency, int coefficientsCount);

    public abstract Signal convoluteSignals(Signal primarySignal, Signal secondarySignal);
}
