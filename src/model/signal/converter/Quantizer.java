package model.signal.converter;

import model.signal.Sample;

import java.util.ArrayList;

public abstract class Quantizer
{
    public Quantizer(int bits)
    {
        this.bits = bits;
    }

    private int bits;

    public int getBits()
    {
        return bits;
    }

    public abstract ArrayList<Sample> quantize(ArrayList<Sample> samples, double amplitude);
}
