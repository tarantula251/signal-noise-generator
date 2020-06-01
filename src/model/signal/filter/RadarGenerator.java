package model.signal.filter;

import model.signal.Signal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RadarGenerator {
    public final static String SENT_SIGNAL = "sentSignal";
    public final static String RECEIVED_SIGNAL = "receivedSignal";
    public final static String CORRELATED_SIGNAL = "correlatedSignal";

    public final static String ORIGINAL_DISTANCE = "originalDistance";
    public final static String CALCULATED_DISTANCE = "calculatedDistance";

    private double timeUnit;
    private double realVelocity;
    private double propagationVelocity;
    private double signalPeriod;
    private double samplingFrequency;
    private double reportPeriod;
    private int measuresCount;
    private int bufferSize;

    public RadarGenerator(double timeUnit, double realVelocity, double propagationVelocity, double signalPeriod,
                          double samplingFrequency, double reportPeriod, int measuresCount, int bufferSize) {
        this.timeUnit = timeUnit;
        this.realVelocity = realVelocity;
        this.propagationVelocity = propagationVelocity;
        this.signalPeriod = signalPeriod;
        this.samplingFrequency = samplingFrequency;
        this.reportPeriod = reportPeriod;
        this.measuresCount = measuresCount;
        this.bufferSize = bufferSize;
    }

    public HashMap<String, Signal> generateSignals() {
        HashMap<String, Signal> signalsMap = new HashMap<String, Signal>();

        Signal sentSignal = generateSentSignal();
        Signal receivedSignal = generateReceivedSignal();
        FilterGenerator filterGenerator = new FilterGenerator();
        Signal correlatedSignal = filterGenerator.correlateSignals(sentSignal, receivedSignal, Filter.CORRELATION_DIRECT_METHOD);

        signalsMap.put(SENT_SIGNAL, sentSignal);
        signalsMap.put(RECEIVED_SIGNAL, receivedSignal);
        signalsMap.put(CORRELATED_SIGNAL, correlatedSignal);

        return signalsMap;
    }

    public HashMap<String, ArrayList<Double>> getDistancesMap() {
        HashMap<String, ArrayList<Double>> distancesMap = new HashMap<String, ArrayList<Double>>();

        ArrayList<Double> originalDistances = new ArrayList<Double>(Arrays.asList(2.5, 0.9, 1.13, 10.0, 84.1, 0.46));
        ArrayList<Double> calculatedDistances = new ArrayList<Double>(Arrays.asList(-13.2, 52.0, 46.9, -46.5));

        distancesMap.put(ORIGINAL_DISTANCE, originalDistances);
        distancesMap.put(CALCULATED_DISTANCE, calculatedDistances);

        return distancesMap;
    }
    //TBD
    private Signal generateSentSignal() {
        return null;
    }
    //TBD
    private Signal generateReceivedSignal() {
        return null;
    }
}
