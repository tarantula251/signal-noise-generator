package model.signal.generator;

public class SignalGeneratorFactory {
    // signal types
    private static final String SIGNAL_TYPE_S1_VALUE = "S1: Szum o rozkładzie jednostajnym";
    private static final String SIGNAL_TYPE_S2_VALUE = "S2: Szum gaussowski";
    private static final String SIGNAL_TYPE_S3_VALUE = "S3: Sygnał sinusoidalny";
    private static final String SIGNAL_TYPE_S4_VALUE = "S4: Sygnał sinusoidalny wyprostowany jednopołówkowo";
    private static final String SIGNAL_TYPE_S5_VALUE = "S5: Sygnał sinusoidalny wyprostowany dwupołówkowo";
    private static final String SIGNAL_TYPE_S6_VALUE = "S6: Sygnał prostokątny";
    private static final String SIGNAL_TYPE_S7_VALUE = "S7: Sygnał prostokątny symetryczny";
    private static final String SIGNAL_TYPE_S8_VALUE = "S8: Sygnał trójkątny";
    private static final String SIGNAL_TYPE_S9_VALUE = "S9: Skok jednostkowy";
    private static final String SIGNAL_TYPE_S10_VALUE = "S10: Impuls jednostkowy";
    private static final String SIGNAL_TYPE_S11_VALUE = "S11: Szum impulsowy";

    static public SignalGenerator getSignalGenerator(String type)
    {
        if(type == null) return null;
        switch (type) {
            case SIGNAL_TYPE_S1_VALUE: {
                return new UniformDistributionSignalGenerator();
            }
            case SIGNAL_TYPE_S2_VALUE: {
                return new GaussianSignalGenerator();
            }
            case SIGNAL_TYPE_S3_VALUE: {
                return new SinusoidalSignalGenerator();
            }
            case SIGNAL_TYPE_S4_VALUE: {
                return new SinusoidalHalfStraightGenerator();
            }
            case SIGNAL_TYPE_S5_VALUE: {
                return new SinusoidalFullStraightGenerator();
            }
            case SIGNAL_TYPE_S6_VALUE: {
                return new RectangularSignalGenerator();
            }
            case SIGNAL_TYPE_S7_VALUE: {
                return new RectangularSymmetricSignalGenerator();
            }
            case SIGNAL_TYPE_S8_VALUE: {
                return new TriangularSignalGenerator();
            }
            case SIGNAL_TYPE_S9_VALUE: {
                return new HeavisideStepGenerator();
            }
            case SIGNAL_TYPE_S10_VALUE: {
                return new UnitPulseGenerator();
            }
            case SIGNAL_TYPE_S11_VALUE: {
                return new PulseNoiseGenerator();
            }
        }
        return null;
    }

}
