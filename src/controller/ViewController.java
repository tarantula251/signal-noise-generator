package controller;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import model.signal.Sample;
import model.signal.Signal;
import model.signal.generator.SignalGenerator;
import model.signal.generator.SignalGeneratorFactory;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

public class ViewController implements Initializable {
    // menu elements
    @FXML private ComboBox signalTypeComboBox;
    @FXML private TextField durationInput;
    @FXML private TextField startingTimeInput;
    @FXML private TextField amplitudeInput;
    @FXML private TextField frequencyInput;
    @FXML private TextField fillFactorInput;
    @FXML private LineChart<Number, Number> lineChart;
    @FXML private BarChart<Number, Number> barChart;
    @FXML private Button generateButton;
    @FXML private Slider intervalSlider;
    @FXML private Label averageLabel;
    @FXML private Label absoluteAverageLabel;
    @FXML private Label averagePowerLabel;
    @FXML private Label effectiveValueLabel;
    @FXML private Label varianceLabel;
    private ArrayList<Signal> loadedSignals = new ArrayList<>();

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        lineChart.setTitle("Krzywa sygnału");
        lineChart.getXAxis().setLabel("Czas");
        lineChart.getYAxis().setLabel("Wartość");

        barChart.setTitle("Rozkład wartości próbek");
        barChart.getXAxis().setLabel("Wartość");
        barChart.getYAxis().setLabel("Ilość próbek");

        // populate data in combobox
        signalTypeComboBox.getItems().addAll(
                "S1: Szum o rozkładzie jednostajnym",
                "S2: Szum gaussowski",
                "S3: Sygnał sinusoidalny",
                "S4: Sygnał sinusoidalny wyprostowany jednopołówkowo",
                "S5: Sygnał sinusoidalny wyprostowany dwupołówkowo",
                "S6: Sygnał prostokątny",
                "S7: Sygnał prostokątny symetryczny",
                "S8: Sygnał trójkątny",
                "S9: Skok jednostkowy",
                "S10: Impuls jednostkowy",
                "S11: Szum impulsowy"
        );
        signalTypeComboBox.getSelectionModel().selectFirst();
        signalTypeComboBox.setVisibleRowCount(11);
        setTextFieldsValidation();
        enableDisableGenerateBtn();
        enableDisableFillFactorInput();

        EventHandler<ActionEvent> generateButtonActionEventEventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mouseEvent) {
                SignalGenerator signalGenerator = SignalGeneratorFactory.getSignalGenerator((String) signalTypeComboBox.getValue());
                if(signalGenerator == null) return;
                Signal signal;
                if (fillFactorInput.isDisabled()) {
                    signal = signalGenerator.generate(Double.parseDouble(durationInput.getText()),
                            Double.parseDouble(startingTimeInput.getText()),
                            Double.parseDouble(amplitudeInput.getText()),
                            Double.parseDouble(frequencyInput.getText()));
                } else {
                    signal = signalGenerator.generateWithFillFactor(Double.parseDouble(durationInput.getText()),
                            Double.parseDouble(startingTimeInput.getText()),
                            Double.parseDouble(amplitudeInput.getText()),
                            Double.parseDouble(frequencyInput.getText()),
                            Double.parseDouble(fillFactorInput.getText()));
                }
                signal.setName("Signal " + LocalDateTime.now().toString());

                loadedSignals.clear();
                loadedSignals.add(signal);

                lineChart.getData().clear();
                drawSignalCurve(signal);

                barChart.getData().clear();
                drawHistogram(signal);

                displaySignalParameters(signal);
            }
        };
        generateButton.setOnAction(generateButtonActionEventEventHandler);
    }

    private void drawSignalCurve(Signal signal)
    {
        if(signal == null) return;
        lineChart.getData().add(new XYChart.Series<Number, Number>());
        lineChart.setCreateSymbols(false);
        XYChart.Series series = lineChart.getData().get(0);
        series.setName(signal.getName());
        for(Sample sample : signal.getSamples())
        {
            series.getData().add(new XYChart.Data<Number, Number>(sample.time, sample.value));
        }
    }

    private void drawHistogram(Signal signal)
    {
        if(signal == null) return;
        int interval = (int) intervalSlider.getValue();
        int lowestPossibleValue = (int)Math.floor(-signal.getAmplitude());
        TreeMap<Integer, Integer> histogramIntervals = new TreeMap<>();
        for(Sample sample : signal.getSamples())
        {
            int sampleInterval = lowestPossibleValue;
            while(sampleInterval < sample.value)
            {
                sampleInterval += interval;
            }
            Integer sampleCount = histogramIntervals.get(sampleInterval);
            if(sampleCount == null) histogramIntervals.put(sampleInterval, 1);
            else histogramIntervals.put(sampleInterval, sampleCount + 1);
        }
        if(histogramIntervals.isEmpty()) return;

        barChart.getData().add(new BarChart.Series<>());
        BarChart.Series series = barChart.getData().get(0);
        series.setName(signal.getName());
        for(Map.Entry<Integer, Integer> histogramInterval : histogramIntervals.entrySet())
        {
            series.getData().add(new BarChart.Data<String, Number>("< " + Integer.toString(histogramInterval.getKey() - interval) + " , " + histogramInterval.getKey().toString() + " >", histogramInterval.getValue()));
        }
    }

    private void displaySignalParameters(Signal signal)
    {
        averageLabel.setText(decimalFormat.format(signal.getAverage()));
        absoluteAverageLabel.setText(decimalFormat.format(signal.getAbsoluteAverage()));
        averagePowerLabel.setText(decimalFormat.format(signal.getAveragePower()));
        effectiveValueLabel.setText(decimalFormat.format(signal.getEffectiveValue()));
        varianceLabel.setText(decimalFormat.format(signal.getVariance()));
    }
    private void setTextFieldsValidation() {
        ArrayList<TextField> textFieldList = new ArrayList<>();
        textFieldList.add(durationInput);
        textFieldList.add(startingTimeInput);
        textFieldList.add(amplitudeInput);
        textFieldList.add(frequencyInput);
        textFieldList.add(fillFactorInput);
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
                        frequencyInput.textProperty(),
                        fillFactorInput.textProperty());
            }
            @Override
            protected boolean computeValue() {
                return (durationInput.getText().isEmpty()
                        || startingTimeInput.getText().isEmpty()
                        || amplitudeInput.getText().isEmpty()
                        || frequencyInput.getText().isEmpty()
                        || (!fillFactorInput.isDisabled() && fillFactorInput.getText().isEmpty()));
            }
        };
        generateButton.disableProperty().bind(binding);
    }

    private void enableDisableFillFactorInput() {
        signalTypeComboBox.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            ArrayList<String> allowedSignals = new ArrayList<>(Arrays.asList(
                    "S6: Sygnał prostokątny",
                    "S7: Sygnał prostokątny symetryczny",
                    "S8: Sygnał trójkątny"
            ));
            if (allowedSignals.contains(newValue)) {
                fillFactorInput.setDisable(false);
            } else {
                fillFactorInput.setDisable(true);
            }
        }));
    }
}
