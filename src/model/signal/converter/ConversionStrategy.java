package model.signal.converter;

import model.signal.Signal;

public interface ConversionStrategy
{
    public abstract Signal convert(Signal signal, double frequency);
}
