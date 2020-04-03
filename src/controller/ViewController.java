package controller;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import model.signal.Sample;
import model.signal.Signal;
import model.signal.generator.SignalGenerator;
import model.signal.generator.SignalGeneratorFactory;

import java.io.*;
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
    @FXML private TextField jumpTimeInput;
    @FXML private TextField sampleNumberInput;
    @FXML private TextField probabilityInput;
    @FXML private LineChart<Number, Number> lineChart;
    @FXML private BarChart<Number, Number> barChart;
    @FXML private ScatterChart<Number, Number> scatterChart;
    @FXML private NumberAxis scatterXAxis;
    @FXML private Button generateButton;
    @FXML private Slider intervalSlider;
    @FXML private Label averageLabel;
    @FXML private Label absoluteAverageLabel;
    @FXML private Label averagePowerLabel;
    @FXML private Label effectiveValueLabel;
    @FXML private Label varianceLabel;
    @FXML private MenuBar menuBar;
    @FXML private MenuItem exportMenuItem;
    @FXML private MenuItem importMenuItem;
    private ArrayList<Signal> loadedSignals = new ArrayList<>();

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        lineChart.setTitle("Krzywa sygnału");
        lineChart.getXAxis().setLabel("Czas");
        lineChart.getYAxis().setLabel("Wartość");
        lineChart.setCreateSymbols(false);

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
        enableDisableTextInputs();

        EventHandler<ActionEvent> generateButtonActionEventEventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mouseEvent) {
                SignalGenerator signalGenerator = SignalGeneratorFactory.getSignalGenerator((String) signalTypeComboBox.getValue());
                if(signalGenerator == null) return;
                String generatorClassName = String.valueOf(signalGenerator.getClass());
                Signal signal;
                HashSet<String> fillFactorClassNames = new HashSet<>(Arrays.asList("class model.signal.generator.RectangularSignalGenerator",
                        "class model.signal.generator.RectangularSymmetricSignalGenerator",
                        "class model.signal.generator.TriangularSignalGenerator"));
                HashSet<String> jumpTimeClassNames = new HashSet<>(Arrays.asList("class model.signal.generator.HeavisideStepGenerator"));
                HashSet<String> sampleNumberForJumpClassNames = new HashSet<>(Arrays.asList("class model.signal.generator.UnitPulseGenerator"));
                HashSet<String> probabilityClassNames = new HashSet<>(Arrays.asList("class model.signal.generator.PulseNoiseGenerator"));
                boolean hideLineChart = false;
                if (fillFactorClassNames.contains(generatorClassName)) {
                    signal = signalGenerator.generateWithFillFactor(Double.parseDouble(durationInput.getText()),
                            Double.parseDouble(startingTimeInput.getText()),
                            Double.parseDouble(amplitudeInput.getText()),
                            Double.parseDouble(frequencyInput.getText()),
                            Double.parseDouble(fillFactorInput.getText()));
                } else if (jumpTimeClassNames.contains(generatorClassName)) {
                    signal = signalGenerator.generateWithJumpTime(Double.parseDouble(durationInput.getText()),
                            Double.parseDouble(startingTimeInput.getText()),
                            Double.parseDouble(amplitudeInput.getText()),
                            Double.parseDouble(jumpTimeInput.getText()));
                } else if (sampleNumberForJumpClassNames.contains(generatorClassName)) {
                    hideLineChart = true;
                    signal = signalGenerator.generateWithSampleNrForJump(Double.parseDouble(durationInput.getText()),
                            Double.parseDouble(startingTimeInput.getText()),
                            Double.parseDouble(amplitudeInput.getText()),
                            Double.parseDouble(frequencyInput.getText()),
                            Integer.parseInt(sampleNumberInput.getText()));
                } else if (probabilityClassNames.contains(generatorClassName)) {
                    hideLineChart = true;
                    signal = signalGenerator.generateWithFillFactor(Double.parseDouble(durationInput.getText()),
                            Double.parseDouble(startingTimeInput.getText()),
                            Double.parseDouble(amplitudeInput.getText()),
                            Double.parseDouble(frequencyInput.getText()),
                            Double.parseDouble(probabilityInput.getText()));
                } else {
                    signal = signalGenerator.generate(Double.parseDouble(durationInput.getText()),
                            Double.parseDouble(startingTimeInput.getText()),
                            Double.parseDouble(amplitudeInput.getText()),
                            Double.parseDouble(frequencyInput.getText()));
                }
                signal.setName("Signal " + LocalDateTime.now().toString().replace('.', '_').replace(':', '_'));

                loadedSignals.clear();
                loadedSignals.add(signal);

                if (!hideLineChart) {
                    if (scatterChart.isVisible()) scatterChart.setVisible(false);
                    if (!lineChart.isVisible()) lineChart.setVisible(true);
                    lineChart.getData().clear();
                    drawSignalCurve(signal);
                } else {
                    if (lineChart.isVisible()) lineChart.setVisible(false);
                    if (!scatterChart.isVisible()) scatterChart.setVisible(true);
                    scatterChart.setTitle("Próbki sygnału");
                    scatterChart.getXAxis().setLabel("Czas");
                    scatterChart.getYAxis().setLabel("Wartość");
                    scatterChart.getData().clear();
                    drawSignalPoints(signal);
                }

                barChart.getData().clear();
                drawHistogram(signal);

                displaySignalParameters(signal);
            }
        };

        EventHandler<ActionEvent> exportMenuItemActionEventEventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(loadedSignals.size() == 0) return;
                try {
                    exportSignal(loadedSignals.get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        };

        EventHandler<ActionEvent> importMenuItemActionEventEventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    loadedSignals = importSignals();
                    for(Signal signal : loadedSignals)
                    {
                        drawSignalCurve(signal);
                        drawHistogram(signal);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        };

        generateButton.setOnAction(generateButtonActionEventEventHandler);
        exportMenuItem.setOnAction(exportMenuItemActionEventEventHandler);
        importMenuItem.setOnAction(importMenuItemActionEventEventHandler);
    }

    private void drawSignalCurve(Signal signal)
    {
        if(signal == null) return;
        if (lineChart.isVisible()) {
            lineChart.getData().add(new XYChart.Series<Number, Number>());
            XYChart.Series series = lineChart.getData().get(lineChart.getData().size() - 1);
            series.setName(signal.getName());
            for(Sample sample : signal.getSamples())
            {
                series.getData().add(new XYChart.Data<Number, Number>(sample.time, sample.value));
            }
        }
    }

    private void drawSignalPoints(Signal signal)
    {
        if(signal == null) return;
        if (scatterChart.isVisible()) {
            scatterChart.getData().add(new XYChart.Series<Number, Number>());
            XYChart.Series series = scatterChart.getData().get(scatterChart.getData().size() - 1);
            series.setName(signal.getName());
            double minTime = signal.getSamples().get(0).time;
            double maxTime = signal.getSamples().get(signal.getSamples().size() - 1).time;
            for (Sample sample : signal.getSamples()) {
                series.getData().add(new XYChart.Data<Number, Number>(sample.time, sample.value));
            }
            // adjust bounds of x axis
            scatterChart.getXAxis().setAutoRanging(false);
            scatterXAxis.setUpperBound(maxTime + 1.0);
            scatterXAxis.setLowerBound(minTime - 1.0);
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
        BarChart.Series series = barChart.getData().get(barChart.getData().size() - 1);
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
        textFieldList.add(jumpTimeInput);
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
        fillFactorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if (!newValue.matches("[0]?(\\.\\d*)?|[1]")) {
                    fillFactorInput.setText(oldValue);
                }
            }
        });
        sampleNumberInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if (!newValue.matches("\\d+")) {
                    sampleNumberInput.setText(oldValue);
                }
            }
        });
        probabilityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if (!newValue.matches("[0]?(\\.\\d*)?|[1]")) {
                    probabilityInput.setText(oldValue);
                }
            }
        });
    }

    private void enableDisableGenerateBtn() {
        BooleanBinding binding = new BooleanBinding() {
            {
                super.bind(durationInput.textProperty(),
                        startingTimeInput.textProperty(),
                        amplitudeInput.textProperty(),
                        frequencyInput.textProperty(),
                        fillFactorInput.textProperty(),
                        jumpTimeInput.textProperty(),
                        sampleNumberInput.textProperty(),
                        probabilityInput.textProperty());
            }
            @Override
            protected boolean computeValue() {
                return (durationInput.getText().isEmpty()
                        || startingTimeInput.getText().isEmpty()
                        || amplitudeInput.getText().isEmpty()
                        || (!frequencyInput.isDisabled() && frequencyInput.getText().isEmpty())
                        || (!fillFactorInput.isDisabled() && fillFactorInput.getText().isEmpty())
                        || (!jumpTimeInput.isDisabled() && jumpTimeInput.getText().isEmpty())
                        || (!sampleNumberInput.isDisabled() && sampleNumberInput.getText().isEmpty())
                        || (!probabilityInput.isDisabled() && probabilityInput.getText().isEmpty()));
            }
        };
        generateButton.disableProperty().bind(binding);
    }

    private void enableDisableTextInputs() {
        signalTypeComboBox.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            ArrayList<String> factorAllowedSignals = new ArrayList<>(Arrays.asList(
                    "S6: Sygnał prostokątny",
                    "S7: Sygnał prostokątny symetryczny",
                    "S8: Sygnał trójkątny"
            ));
            String jumpTimeAllowedSignal = "S9: Skok jednostkowy";
            String sampleNumberAllowedSignal = "S10: Impuls jednostkowy";
            String probabilityAllowedSignal = "S11: Szum impulsowy";
            if (factorAllowedSignals.contains(newValue)) {
                fillFactorInput.setDisable(false);
            } else {
                fillFactorInput.setDisable(true);
            }
            if (jumpTimeAllowedSignal.equalsIgnoreCase(String.valueOf(newValue))) {
                jumpTimeInput.setDisable(false);
                frequencyInput.setDisable(true);
            } else {
                frequencyInput.setDisable(false);
                jumpTimeInput.setDisable(true);
            }
            if (sampleNumberAllowedSignal.equalsIgnoreCase(String.valueOf(newValue))) {
                sampleNumberInput.setDisable(false);
            } else {
                sampleNumberInput.setDisable(true);
            }
            if (probabilityAllowedSignal.equalsIgnoreCase(String.valueOf(newValue))) {
                probabilityInput.setDisable(false);
            } else {
                probabilityInput.setDisable(true);
            }
        }));
    }

    private void exportSignal(Signal signal) throws IOException {
        FileChooser exportSignalFileChooser = new FileChooser();
        FileChooser.ExtensionFilter sigExtensionFilter = new FileChooser.ExtensionFilter("Signal file", "*.sig");
        FileChooser.ExtensionFilter txtExtensionFilter = new FileChooser.ExtensionFilter("Text file", "*.txt");
        exportSignalFileChooser.getExtensionFilters().add(sigExtensionFilter);
        exportSignalFileChooser.getExtensionFilters().add(txtExtensionFilter);
        exportSignalFileChooser.setInitialFileName(signal.getName());

        File file = exportSignalFileChooser.showSaveDialog(menuBar.getScene().getWindow());

        if(file == null) return;

        if(!file.createNewFile()) System.out.println("File already exist. Overriding.");

        String fileType = exportSignalFileChooser.getSelectedExtensionFilter().getDescription();

        if(fileType.equals("Signal file"))
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(signal);
            objectOutputStream.close();
        }
        else
        {
            PrintWriter printWriter = new PrintWriter(new FileOutputStream(file));
            printWriter.println(signal.toString());
            printWriter.close();
        }
    }

    private ArrayList<Signal> importSignals() throws IOException {
        ArrayList<Signal> importedSignals = new ArrayList<>();
        FileChooser exportSignalFileChooser = new FileChooser();
        FileChooser.ExtensionFilter sigExtensionFilter = new FileChooser.ExtensionFilter("Signal file", "*.sig");
        exportSignalFileChooser.getExtensionFilters().add(sigExtensionFilter);

        List<File> files = exportSignalFileChooser.showOpenMultipleDialog(menuBar.getScene().getWindow());

        for(File file : files)
        {
            if(file == null) continue;;

            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));

            try {
                Signal signal = (Signal)objectInputStream.readObject();
                importedSignals.add(signal);
            } catch (ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }

            objectInputStream.close();
        }

        return importedSignals;
    }
}
