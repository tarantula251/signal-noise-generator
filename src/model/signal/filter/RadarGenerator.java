package model.signal.filter;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class RadarGenerator {
    public final static String SENT_SIGNAL = "sentSignal";
    public final static String RECEIVED_SIGNAL = "receivedSignal";
    public final static String CORRELATED_SIGNAL = "correlatedSignal";

    public final static String ORIGINAL_DISTANCE = "originalDistance";
    public final static String CALCULATED_DISTANCE = "calculatedDistance";

    private double timeUnit; //TODO remove
    private double realVelocity;
    private double propagationVelocity;
    private double signalPeriod;
    private double samplingFrequency;
    private double reportPeriod;
    private int bufferSize;
    private double duration;
    private TrackedObject trackedObject;
    private ArrayList<Double> calculatedDistancesList = new ArrayList<>();
    private ArrayList<Double> originalDistancesList = new ArrayList<>();
    private Thread simulationThread;
    private Signal sentSignal;
    private Signal receivedSignal;
    private Signal correlatedSignal;

    public RadarGenerator(double timeUnit, double realVelocity, double propagationVelocity, double signalPeriod,
                          double samplingFrequency, double reportPeriod, int bufferSize) {
        this.timeUnit = timeUnit;
        this.realVelocity = realVelocity;
        this.propagationVelocity = propagationVelocity;
        this.signalPeriod = signalPeriod;
        this.samplingFrequency = samplingFrequency;
        this.reportPeriod = reportPeriod;
        this.bufferSize = bufferSize;
        this.trackedObject = new TrackedObject();
    }

    public void startRadarSimulation() throws InterruptedException {
        this.trackedObject = new TrackedObject();
        this.duration = this.bufferSize / this.samplingFrequency;

        this.sentSignal = generateSignal(this.signalPeriod, this.duration, this.samplingFrequency, -1);

        simulationThread = new Thread(() -> {
            try {
                startSimulation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        simulationThread.start();
        simulationThread.join();
    }

    private void startSimulation() throws InterruptedException {
        FilterGenerator filterGenerator = new FilterGenerator();
        Thread.sleep((int) this.reportPeriod * 1000);
        trackedObject.position += trackedObject.velocity * this.reportPeriod;
        double delayTime = 2.0 * trackedObject.position / this.propagationVelocity;

        this.receivedSignal = generateSignal(this.signalPeriod, this.duration, this.samplingFrequency, delayTime);
        this.correlatedSignal = filterGenerator.correlateSignals(this.sentSignal, this.receivedSignal, Filter.CORRELATION_CONVOLUTION_METHOD);

        double calculatedDistance = calculateDistance(correlatedSignal);
        this.calculatedDistancesList.add(calculatedDistance);

        this.trackedObject.position = this.realVelocity * this.bufferSize / (2.0 * this.samplingFrequency); //TODO fix
        originalDistancesList.add(this.trackedObject.position);
    }

    private double calculateDistance(Signal correlatedSignal) {
        ArrayList<Sample> correlatedSamples = correlatedSignal.getSamples();
        ArrayList<Sample> rightHalfSamples = new ArrayList<Sample>(correlatedSamples.subList(correlatedSamples.size() / 2, correlatedSamples.size()));
        Sample maxSample = rightHalfSamples
                .stream()
                .max(Comparator.comparing(Sample::getValue))
                .orElse(null);
        double maxSampleValue = maxSample.getValue();
        double delayTime = maxSampleValue / this.samplingFrequency;
        return Math.round(delayTime * this.propagationVelocity / 2.0);
    }

    public HashMap<String, ArrayList<Double>> getDistancesMap() {
        HashMap<String, ArrayList<Double>> distancesMap = new HashMap<String, ArrayList<Double>>();
        distancesMap.put(ORIGINAL_DISTANCE, this.originalDistancesList);
        distancesMap.put(CALCULATED_DISTANCE, this.calculatedDistancesList);

        return distancesMap;
    }

    public HashMap<String, Signal> getSignals() {
        HashMap<String, Signal> signalsMap = new HashMap<String, Signal>();
        signalsMap.put(SENT_SIGNAL, this.sentSignal);
        signalsMap.put(RECEIVED_SIGNAL, this.receivedSignal);
        signalsMap.put(CORRELATED_SIGNAL, this.correlatedSignal);

        return signalsMap;
    }

    private Signal generateSignal(double period, double duration, double frequency, double delayTime) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = this.bufferSize;
        double samplesDistance = duration / samplesCount;
        double amplitude = Math.random() * 2;
        for (int i = 0; i < samplesCount; ++i) {
            double value = amplitude * Math.sin(2 * Math.PI * (i * samplesDistance) / period) +
                amplitude * Math.sin(2 * Math.PI * (i * samplesDistance) / (period + 1));
            if (delayTime != -1) {
                samples.add(new Sample(i * samplesDistance + delayTime, value));
            } else {
                samples.add(new Sample(i * samplesDistance, value));
            }
        }
        return new Signal(samples, duration, amplitude, frequency);
    }
}
