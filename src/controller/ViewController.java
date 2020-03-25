package controller;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import model.signal.Sample;
import model.signal.Signal;
import model.signal.generator.SignalGenerator;
import model.signal.generator.SignalGeneratorFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ViewController implements Initializable {
    // menu elements
    @FXML private ComboBox signalTypeComboBox;
    @FXML private TextField durationInput;
    @FXML private TextField startingTimeInput;
    @FXML private TextField amplitudeInput;
    @FXML private TextField frequencyInput;
    @FXML private LineChart<Number, Number> lineChart;
    @FXML private Button generateButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Set axis labels
        lineChart.getXAxis().setLabel("Time");
        lineChart.getYAxis().setLabel("Value");

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

        EventHandler<MouseEvent> mouseEventEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                SignalGenerator signalGenerator = SignalGeneratorFactory.getSignalGenerator(SignalGeneratorFactory.getGeneratorNameFromId(signalTypeComboBox.getSelectionModel().getSelectedIndex()));
                if(signalGenerator == null) return;
                Signal signal = signalGenerator.generate(Double.parseDouble(durationInput.getText()),
                        Double.parseDouble(startingTimeInput.getText()),
                        Double.parseDouble(amplitudeInput.getText()),
                        Double.parseDouble(frequencyInput.getText()));
                if(signal == null) return;

                lineChart.getData().clear();

                lineChart.getData().add(new XYChart.Series<Number, Number>());
                XYChart.Series series = lineChart.getData().get(0);
                series.setName("Signal " + LocalDateTime.now().toString());
                for(Sample sample : signal.getSamples())
                {
                    series.getData().add(new XYChart.Data<Number, Number>(sample.time, sample.value));
                }
            }
        };

        generateButton.setOnMouseClicked(mouseEventEventHandler);

    }

    private void setTextFieldsValidation() {
        ArrayList<TextField> textFieldList = new ArrayList<>();
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
        generateButton.disableProperty().bind(binding);
    }
}
