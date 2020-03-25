package controller;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.function.Function;

public class ViewController implements Initializable {
    // menu elements
    @FXML private ComboBox signalTypeComboBox;
    @FXML private TextField durationInput;
    @FXML private TextField startingTimeInput;
    @FXML private TextField amplitudeInput;
    @FXML private TextField frequencyInput;
    @FXML private Button generateBtn;
    @FXML private LineChart<Double, Double> lineChart;
    @FXML private BarChart<Double, Double> barChart;
    private CustomGraph lineCustomGraph;
    private CustomGraph barCustomGraph;
    //parameters map keys
    private static final String SIGNAL_TYPE_KEY = "signal_type";
    private static final String DURATION_KEY = "duration";
    private static final String START_TIME_KEY = "start_time";
    private static final String AMPLITUDE_KEY = "amplitude";
    private static final String FREQUENCY_KEY = "frequency";
    //possible signal types
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // populate data in menu
        signalTypeComboBox.getItems().addAll(
                SIGNAL_TYPE_S1_VALUE,
                SIGNAL_TYPE_S2_VALUE,
                SIGNAL_TYPE_S3_VALUE,
                SIGNAL_TYPE_S4_VALUE,
                SIGNAL_TYPE_S5_VALUE,
                SIGNAL_TYPE_S6_VALUE,
                SIGNAL_TYPE_S7_VALUE,
                SIGNAL_TYPE_S8_VALUE,
                SIGNAL_TYPE_S9_VALUE,
                SIGNAL_TYPE_S10_VALUE,
                SIGNAL_TYPE_S11_VALUE
        );
        signalTypeComboBox.getSelectionModel().selectFirst();
        signalTypeComboBox.setVisibleRowCount(3);
        setTextFieldsValidation();
        enableDisableGenerateBtn();
        lineCustomGraph = new CustomGraph(lineChart, 10);
        barCustomGraph = new CustomGraph(barChart, 10);
    }

    private void setTextFieldsValidation() {
        ArrayList<TextField> textFieldList = new ArrayList<TextField>();
        textFieldList.add(durationInput);
        textFieldList.add(startingTimeInput);
        textFieldList.add(amplitudeInput);
        textFieldList.add(frequencyInput);
        textFieldList.add(durationInput);
        for (TextField field : textFieldList) {
            field.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                    if (!newValue.matches("[-]?\\d*(\\.\\d*)?")) {
                        field.setText(oldValue);
                    }
                }
            });
        }
    }

    private void enableDisableGenerateBtn() {
        BooleanBinding binding = new BooleanBinding() {
            {
                super.bind(durationInput.textProperty(),
                        startingTimeInput.textProperty(),
                        amplitudeInput.textProperty(),
                        frequencyInput.textProperty());
            }
            @Override
            protected boolean computeValue() {
                return (durationInput.getText().isEmpty()
                        || startingTimeInput.getText().isEmpty()
                        || amplitudeInput.getText().isEmpty()
                        || frequencyInput.getText().isEmpty());
            }
        };
        generateBtn.disableProperty().bind(binding);
    }

    public void generateChart(ActionEvent actionEvent) {
        HashMap<String, Object> parametersMap = new HashMap<>();
        parametersMap.put(SIGNAL_TYPE_KEY, signalTypeComboBox.getValue());
        parametersMap.put(DURATION_KEY, durationInput.getText());
        parametersMap.put(START_TIME_KEY, startingTimeInput.getText());
        parametersMap.put(AMPLITUDE_KEY, amplitudeInput.getText());
        parametersMap.put(FREQUENCY_KEY, frequencyInput.getText());
        plotLine(parametersMap);
    }

    private void plotLine(HashMap<String, Object> parametersMap) {
        String signalType = (String) parametersMap.get(SIGNAL_TYPE_KEY);
        Function<Double, Double> functionForSignal = null;
        //TODO change these sample functions
        switch (signalType) {
            case SIGNAL_TYPE_S1_VALUE: {
                functionForSignal = x -> x;
                break;
            }
            case SIGNAL_TYPE_S2_VALUE: {
                functionForSignal = x -> x - 3;
                break;
            }
            case SIGNAL_TYPE_S3_VALUE: {
                functionForSignal = x -> Math.pow(x, 2);
                break;
            }
            case SIGNAL_TYPE_S4_VALUE: {
                functionForSignal = x -> Math.pow(x, 2) + 2;
                break;
            }
            case SIGNAL_TYPE_S5_VALUE: {
                functionForSignal = x -> Math.pow(x - 3, 3) - 1;
                break;
            }
            case SIGNAL_TYPE_S6_VALUE: {
                functionForSignal = x -> Math.pow(x, 3);
                break;
            }
            case SIGNAL_TYPE_S7_VALUE: {
                functionForSignal = x -> Math.pow(x - 3, 3) - 2;
                break;
            }
            case SIGNAL_TYPE_S8_VALUE: {
                functionForSignal = x -> x + 5;
                break;
            }
            case SIGNAL_TYPE_S9_VALUE: {
                functionForSignal = x -> Math.pow(x, 3) + 2;
                break;
            }
            case SIGNAL_TYPE_S10_VALUE: {
                functionForSignal = x -> Math.pow(x - 3, 2) - 1;
                break;
            }
            case SIGNAL_TYPE_S11_VALUE: {
                functionForSignal = x -> Math.pow(x, 2) - 4;
                break;
            }
        }
        lineCustomGraph.plotLine(functionForSignal);
        //TODO fix rendering barCustomGraph
//        barCustomGraph.plotLine(functionForSignal);
    }
}
