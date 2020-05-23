package model.signal.converter;

import model.signal.Sample;

import java.util.ArrayList;

public class RoundingQuantizer extends Quantizer
{
    public RoundingQuantizer(int bits)
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
            double sampleValue = Math.round(sample.value / valueStep) * valueStep;

            result.add(new Sample(sample.time, sampleValue));
        }
        return result;
    }
}
