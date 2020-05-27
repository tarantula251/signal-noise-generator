package model.signal.converter;

import model.signal.Sample;
import model.signal.Signal;

import java.util.ArrayList;

public class TruncationQuantizer extends Quantizer
{
    public TruncationQuantizer(int bits)
    {
        super(bits);
    }

    @Override
    public ArrayList<Sample> quantize(ArrayList<Sample> samples, double amplitude)
    {
        double valueStep = 2 * amplitude / Math.pow(2, getBits());

        ArrayList<Sample> result = new ArrayList<>();
        for(Sample sample : samples)
        {
            double sampleValue = (int)(sample.value / valueStep) * valueStep;

            result.add(new Sample(sample.time, sampleValue));
        }
        return result;
    }
}
