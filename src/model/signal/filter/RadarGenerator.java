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

    private double realVelocity;
    private double propagationVelocity;
    private double signalPeriod;
    private double samplingFrequency;
    private double reportPeriod;
    private int bufferSize;
    private double duration;
    private double amplitude;
    private TrackedObject trackedObject;
    private ArrayList<Double> calculatedDistancesList = new ArrayList<>();
    private ArrayList<Double> originalDistancesList = new ArrayList<>();
    private Thread simulationThread;
    private Signal sentSignal;
    private Signal receivedSignal;
    private Signal correlatedSignal;
    private FilterGenerator filterGenerator;

    public RadarGenerator(double realVelocity, double propagationVelocity, double signalPeriod,
                          double samplingFrequency, double reportPeriod, int bufferSize) {
        this.realVelocity = realVelocity;
        this.propagationVelocity = propagationVelocity;
        this.signalPeriod = signalPeriod;
        this.samplingFrequency = samplingFrequency;
        this.reportPeriod = reportPeriod;
        this.bufferSize = bufferSize;
    }

    public void startRadarSimulation() throws InterruptedException {
        this.trackedObject = new TrackedObject(this.realVelocity);
        this.duration = this.bufferSize / this.samplingFrequency;
        this.amplitude = Math.random() * (5 - 2);

        this.sentSignal = generateSignal(this.signalPeriod, this.duration, this.samplingFrequency, 0);
        this.receivedSignal = generateSignal(this.signalPeriod, this.duration, this.samplingFrequency, 0);

        filterGenerator = new FilterGenerator();
        this.correlatedSignal = this.filterGenerator.correlateSignals(this.sentSignal, this.receivedSignal, Filter.CORRELATION_CONVOLUTION_METHOD);

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
        Thread.sleep((int) this.reportPeriod * 1000);
        trackedObject.position += trackedObject.velocity * this.reportPeriod;
        double delayTime = 2.0 * trackedObject.position / this.propagationVelocity;

        Signal currentReceivedSignal = generateSignal(this.signalPeriod, this.duration, this.samplingFrequency, delayTime);
        Signal currentCorrelatedSignal = filterGenerator.correlateSignals(this.sentSignal, currentReceivedSignal, Filter.CORRELATION_CONVOLUTION_METHOD);

        double calculatedDistance = calculateDistance(currentCorrelatedSignal);
        this.calculatedDistancesList.add(calculatedDistance);

//        this.trackedObject.position = Math.round((calculatedDistance * this.propagationVelocity * 1000 / (this.samplingFrequency * 2.0)) * 100000.0) / 100000.0;
        this.trackedObject.position = Math.round((calculatedDistance * this.propagationVelocity * 1000 / (this.samplingFrequency)) * 100000.0) / 100000.0;
        this.originalDistancesList.add(this.trackedObject.position);

        this.receivedSignal = currentReceivedSignal;
        this.correlatedSignal = currentCorrelatedSignal;
    }

    private double calculateDistance(Signal correlatedSignal) {
        ArrayList<Sample> correlatedSamples = correlatedSignal.getSamples();
        ArrayList<Sample> rightHalfSamples = new ArrayList<Sample>(correlatedSamples.subList(correlatedSamples.size() / 2, correlatedSamples.size()));
        Sample maxSample = rightHalfSamples
                .stream()
                .max(Comparator.comparing(Sample::getValue))
                .orElse(null);
//        double maxSampleValue = maxSample.getValue();
//        double delayTime = maxSampleValue / this.samplingFrequency; //???
//        System.out.println("delayTime "+delayTime);

        double timeDiff = maxSample.getTime() - rightHalfSamples.get(0).getTime();

        return Math.round((timeDiff * this.propagationVelocity * 1000 / 2.0) * 100000.0) / 100000.0;
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

    private Signal generateSignal(double period, double duration, double frequency, double delay) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = this.bufferSize;
        double samplesDistance = duration / samplesCount;

        for (int i = 0; i < samplesCount; ++i) {
            double partSinArg = 2 * Math.PI * (i * samplesDistance - delay);
            double value = this.amplitude * Math.sin(partSinArg / period) +
                    this.amplitude * Math.sin(partSinArg / (period * 2));
            samples.add(new Sample(i * samplesDistance, value));
        }
        return new Signal(samples, duration, this.amplitude, frequency);
    }
}
