package model.signal.converter;

import model.signal.Signal;

public class DAConverter implements  Converter
{
    private ConversionStrategy conversionStrategy;

    public void setConversionStrategy(ConversionStrategy conversionStrategy)
    {
        this.conversionStrategy = conversionStrategy;
    }

    @Override
    public Signal convert(Signal signal, double frequency)
    {
        return conversionStrategy.convert(signal, frequency);
    }
}
