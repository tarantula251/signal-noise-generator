package model.signal.converter;

import model.signal.Sample;

import java.util.ArrayList;

public class TruncationQuantizer extends Quantizer
{
    public TruncationQuantizer(int bits)
    {
        super(bits);
    }

    @Override
    public ArrayList<Sample> quantize(ArrayList<Sample> samples)
    {
        int msbValue = (int)Math.pow(2, getBits() - 1);
        int leftBound = -msbValue;
        int rightBound = msbValue -1;

        ArrayList<Sample> result = new ArrayList<>();
        for(Sample sample : samples)
        {
            long sampleValue = (long)sample.value;
            sampleValue = Math.max(sampleValue, leftBound);
            sampleValue = Math.min(sampleValue, rightBound);
            result.add(new Sample(sample.time, sampleValue));
        }
        return result;
    }
}
