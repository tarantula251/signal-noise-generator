package controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import model.signal.Sample;
import model.signal.Signal;
import model.signal.filter.RadarGenerator;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class RadarDialogController implements Initializable {
    @FXML private Button radarBtn;
    @FXML private TextField realVelocityInput;
    @FXML private TextField propagationVelocityInput;
    @FXML private TextField signalPeriodInput;
    @FXML private TextField probeFrequencyInput;
    @FXML private TextField bufferSizeInput;
    @FXML private TextField reportPeriodInput;
    @FXML private Label originalDistanceLabel;
    @FXML private Label calculatedDistanceLabel;
    @FXML private LineChart<Number, Number> sentSignalLineChart;
    @FXML private LineChart<Number, Number> receivedLineChart;
    @FXML private LineChart<Number, Number> correlatedSignalLineChart;
    private Signal sentSignal = null;
    private Signal receivedSignal = null;
    private Signal correlatedSignal = null;
    private Stage stage;

    private void drawSignalCurve(Signal signal, LineChart<Number, Number> lineChart) {
        if (signal == null) return;
        if (lineChart.isVisible()) {
            if (lineChart.getData().size() > 1) lineChart.getData().remove(lineChart.getData().size() - 1);
            lineChart.getData().add(new XYChart.Series<Number, Number>());
            XYChart.Series<Number, Number> series = lineChart.getData().get(lineChart.getData().size() - 1);

            if(!signal.isContinuous()) series.nodeProperty().get().setStyle("-fx-stroke: transparent;");
            series.setName(signal.getName());

            for(Sample sample : signal.getSamples())
            {
                series.getData().add(new XYChart.Data<Number, Number>(sample.time, sample.value));
                if(signal.isContinuous()) series.getData().get(series.getData().size() - 1).getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: transparent");
            }
        }
    }

    private void populateListView(ArrayList<Double> distancesList, Label label) {
        if (distancesList == null) return;
        label.setText(String.valueOf(distancesList.get(0)));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final ContextMenu chartContextMenu = new ContextMenu();
        MenuItem clearChartsMenuItem = new MenuItem();
        clearChartsMenuItem.textProperty().bind(Bindings.format("Wyczyść wykres"));
        clearChartsMenuItem.setOnAction(actionEvent -> {
            sentSignalLineChart.getData().clear();
            receivedLineChart.getData().clear();
            correlatedSignalLineChart.getData().clear();
            calculatedDistanceLabel.setText("");
            originalDistanceLabel.setText("");
        });
        chartContextMenu.getItems().add(clearChartsMenuItem);

        sentSignalLineChart.setTitle("Sygnał wysłany");
        sentSignalLineChart.setLegendVisible(false);
        sentSignalLineChart.getXAxis().setLabel("Czas");
        sentSignalLineChart.getYAxis().setLabel("Wartość");
        sentSignalLineChart.setOnMouseClicked(event -> {
            if (MouseButton.SECONDARY.equals(event.getButton())
                    && !sentSignalLineChart.getData().isEmpty()
                    && !receivedLineChart.getData().isEmpty()
                    && !correlatedSignalLineChart.getData().isEmpty()) {
                chartContextMenu.show(sentSignalLineChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

        receivedLineChart.setTitle("Sygnał otrzymany");
        receivedLineChart.setLegendVisible(false);
        receivedLineChart.getXAxis().setLabel("Czas");
        receivedLineChart.getYAxis().setLabel("Wartość");
        receivedLineChart.setOnMouseClicked(event -> {
            if (MouseButton.SECONDARY.equals(event.getButton())
                    && !sentSignalLineChart.getData().isEmpty()
                    && !receivedLineChart.getData().isEmpty()
                    && !correlatedSignalLineChart.getData().isEmpty()) {
                chartContextMenu.show(receivedLineChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

        correlatedSignalLineChart.setTitle("Korelacja sygnałów");
        correlatedSignalLineChart.setLegendVisible(false);
        correlatedSignalLineChart.getXAxis().setLabel("Czas");
        correlatedSignalLineChart.getYAxis().setLabel("Wartość");
        correlatedSignalLineChart.setOnMouseClicked(event -> {
            if (MouseButton.SECONDARY.equals(event.getButton())
                    && !sentSignalLineChart.getData().isEmpty()
                    && !receivedLineChart.getData().isEmpty()
                    && !correlatedSignalLineChart.getData().isEmpty()) {
                chartContextMenu.show(correlatedSignalLineChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

        setTextFieldsValidation();
        enableDisableFilterBtn();

        radarBtn.setOnAction(actionEvent -> {
            double realVelocity = Double.parseDouble(realVelocityInput.getText());
            double propagationVelocity = Double.parseDouble(propagationVelocityInput.getText());
            double signalPeriod = Double.parseDouble(signalPeriodInput.getText());
            double samplingFrequency = Double.parseDouble(probeFrequencyInput.getText());
            double reportPeriod = Double.parseDouble(reportPeriodInput.getText());
            int bufferSize = Integer.parseInt(bufferSizeInput.getText());

            RadarGenerator radarGenerator = new RadarGenerator(realVelocity, propagationVelocity, signalPeriod,
                    samplingFrequency, reportPeriod, bufferSize);

            try {
                radarGenerator.startRadarSimulation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            HashMap<String, Signal> signalsMap = radarGenerator.getSignals();

            HashMap<String, ArrayList<Double>> distancesMap = radarGenerator.getDistancesMap();
            ArrayList<Double> originalDistances = distancesMap.get(RadarGenerator.ORIGINAL_DISTANCE);
            ArrayList<Double> calculatedDistances = distancesMap.get(RadarGenerator.CALCULATED_DISTANCE);

            sentSignal = signalsMap.get(RadarGenerator.SENT_SIGNAL);
            receivedSignal = signalsMap.get(RadarGenerator.RECEIVED_SIGNAL);
            correlatedSignal = signalsMap.get(RadarGenerator.CORRELATED_SIGNAL);

            sentSignal.setName("Sent" + LocalDateTime.now().toString().replace('.', '_').replace(':', '_'));
            receivedSignal.setName("Received" + LocalDateTime.now().toString().replace('.', '_').replace(':', '_'));
            correlatedSignal.setName("Correlated" + LocalDateTime.now().toString().replace('.', '_').replace(':', '_'));

            drawSignalCurve(sentSignal, sentSignalLineChart);
            drawSignalCurve(receivedSignal, receivedLineChart);
            drawSignalCurve(correlatedSignal, correlatedSignalLineChart);

            populateListView(originalDistances, originalDistanceLabel);
            populateListView(calculatedDistances, calculatedDistanceLabel);
        });
    }

    private void setTextFieldsValidation() {
        ArrayList<TextField> integerTextFieldList = new ArrayList<>();
        integerTextFieldList.add(bufferSizeInput);
        for (TextField textField : integerTextFieldList) {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                    if (!newValue.matches("\\d+")) {
                        textField.setText(oldValue);
                    }
                }
            });
        }
        ArrayList<TextField> decimalTextFieldList = new ArrayList<>();
        decimalTextFieldList.add(realVelocityInput);
        decimalTextFieldList.add(propagationVelocityInput);
        decimalTextFieldList.add(signalPeriodInput);
        decimalTextFieldList.add(probeFrequencyInput);
        decimalTextFieldList.add(reportPeriodInput);
        for (TextField textField : decimalTextFieldList) {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                    if (!newValue.matches("\\d*(\\.\\d*)?")) {
                        textField.setText(oldValue);
                    }
                }
            });
        }
    }

    private void enableDisableFilterBtn() {
        BooleanBinding binding = new BooleanBinding() {
            {
                super.bind(realVelocityInput.textProperty(),
                        propagationVelocityInput.textProperty(),
                        signalPeriodInput.textProperty(),
                        probeFrequencyInput.textProperty(),
                        bufferSizeInput.textProperty(),
                        reportPeriodInput.textProperty());
            }
            @Override
            protected boolean computeValue() {
                return (realVelocityInput.getText().isEmpty()
                        || propagationVelocityInput.getText().isEmpty()
                        || signalPeriodInput.getText().isEmpty()
                        || probeFrequencyInput.getText().isEmpty()
                        || bufferSizeInput.getText().isEmpty()
                        || reportPeriodInput.getText().isEmpty());
            }
        };
        radarBtn.disableProperty().bind(binding);
    }
}
