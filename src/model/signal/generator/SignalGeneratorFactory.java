package model.signal.generator;

import java.util.ArrayList;

public class SignalGeneratorFactory {

    static private ArrayList<String> generatorsIds = null;

    static public SignalGenerator getSignalGenerator(String type)
    {
        if(type == null) return null;
        if(type.equals("UniformDistributionSignalGenerator")) return new UniformDistributionSignalGenerator();
        return null;
    }

    static public String getGeneratorNameFromId(Integer id)
    {
        if(generatorsIds == null)
        {
            generatorsIds = new ArrayList<>();
            //Add new SignalGenerator classes names below
            generatorsIds.add("UniformDistributionSignalGenerator");
        }
        try
        {
            return generatorsIds.get(id);
        }
        catch (IndexOutOfBoundsException ex)
        {
            return null;
        }
    }


}
