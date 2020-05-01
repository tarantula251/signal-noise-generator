package model.signal.converter;

import model.signal.Signal;

public interface Converter {
    public abstract Signal convert(Signal signal, double frequency);
}
