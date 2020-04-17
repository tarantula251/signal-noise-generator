package controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.signal.Sample;
import model.signal.Signal;
import model.signal.SignalException;
import model.signal.generator.SignalGenerator;
import model.signal.generator.SignalGeneratorFactory;

import java.io.*;
import java.math.RoundingMode;
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
    @FXML private TextField periodInput;
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
    @FXML private MenuItem importMenuItem;
    @FXML private ListView<String> signalsListView;
    private ArrayList<Signal> loadedSignals = new ArrayList<>();

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        final ContextMenu chartContextMenu = new ContextMenu();
        MenuItem clearChartsMenuItem = new MenuItem();
        clearChartsMenuItem.textProperty().bind(Bindings.format("Wyczyść wykres"));
        clearChartsMenuItem.setOnAction(actionEvent -> {
            lineChart.getData().clear();
            barChart.getData().clear();
        });
        chartContextMenu.getItems().add(clearChartsMenuItem);

        lineChart.setTitle("Krzywa sygnału");
        lineChart.getXAxis().setLabel("Czas");
        lineChart.getYAxis().setLabel("Wartość");
        lineChart.setOnMouseClicked(event -> {
            if (MouseButton.SECONDARY.equals(event.getButton())
            && !lineChart.getData().isEmpty() && !barChart.getData().isEmpty()) {
                chartContextMenu.show(lineChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

        barChart.setTitle("Rozkład wartości próbek");
        barChart.getXAxis().setLabel("Wartość");
        barChart.getYAxis().setLabel("Ilość próbek");
        barChart.setOnMouseClicked(event -> {
            if (MouseButton.SECONDARY.equals(event.getButton())
                    && !lineChart.getData().isEmpty() && !barChart.getData().isEmpty()) {
                chartContextMenu.show(lineChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

        // populate data in combobox
        signalTypeComboBox.getItems().addAll(
                SignalGeneratorFactory.SIGNAL_TYPE_S1_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S2_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S3_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S4_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S5_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S6_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S7_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S8_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S9_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S10_VALUE,
                SignalGeneratorFactory.SIGNAL_TYPE_S11_VALUE
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

                double duration = durationInput.getText().isEmpty() ? 0 : Double.parseDouble(durationInput.getText());
                double startingTime = startingTimeInput.getText().isEmpty() ? 0 : Double.parseDouble(startingTimeInput.getText());
                double amplitude = amplitudeInput.getText().isEmpty() ? 0 : Double.parseDouble(amplitudeInput.getText());
                double frequency = frequencyInput.getText().isEmpty() ? 0 : Double.parseDouble(frequencyInput.getText());
                double period = periodInput.getText().isEmpty() ? 0 : Double.parseDouble(periodInput.getText());
                double fillFactor = fillFactorInput.getText().isEmpty() ? 0 : Double.parseDouble(fillFactorInput.getText());
                double jumpTime = jumpTimeInput.getText().isEmpty() ? 0 : Double.parseDouble(jumpTimeInput.getText());
                int sampleNumber = sampleNumberInput.getText().isEmpty() ? 0 : Integer.parseInt(sampleNumberInput.getText());
                double probability = probabilityInput.getText().isEmpty() ? 0 : Double.parseDouble(probabilityInput.getText());

                Signal signal = signalGenerator.generate(
                        duration,
                        startingTime,
                        amplitude,
                        frequency,
                        period,
                        fillFactor,
                        jumpTime,
                        sampleNumber,
                        probability
                );

                if(signal == null) return;

                signal.setName("Signal " + LocalDateTime.now().toString().replace('.', '_').replace(':', '_'));

                loadedSignals.add(signal);
                refreshSignalsListView();

                drawSignalCurve(signal);
                drawHistogram(signal);
                displaySignalParameters(signal);
            }
        };

        EventHandler<ActionEvent> importMenuItemActionEventEventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    loadedSignals.addAll(importSignals());
                    refreshSignalsListView();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        };

        generateButton.setOnAction(generateButtonActionEventEventHandler);
        importMenuItem.setOnAction(importMenuItemActionEventEventHandler);

        signalsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        signalsListView.setCellFactory(lv -> {

            ListCell<String> cell = new ListCell<>();

            cell.textProperty().bind(cell.itemProperty());

            cell.setOnMouseClicked(mouseEvent ->
            {

                if(mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2)
                {
                    addRemoveSignalChart(cell.getItem());
                }
                else if(mouseEvent.getButton().equals(MouseButton.SECONDARY))
                {
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem addToChartItem = new MenuItem();

                    addToChartItem.textProperty().bind(Bindings.format("Dodaj/Usuń z wykresu"));
                    addToChartItem.setOnAction(event -> {
                        addRemoveSignalChart(cell.getItem());
                    });



                    MenuItem editItem = new MenuItem();
                    editItem.textProperty().bind(Bindings.format("Zmień nazwę..."));
                    editItem.setOnAction(event -> {
                        String item = cell.getItem();
                        TextInputDialog dialog = new TextInputDialog(item);
                        dialog.setTitle("Nowa nazwa");
                        dialog.setHeaderText(null);
                        dialog.setGraphic(null);

                        Optional<String> result = dialog.showAndWait();

                        result.ifPresent(name -> {
                            for(Signal signal : loadedSignals)
                            {
                                if(signal.getName().equals(name))
                                {
                                    ArrayList<String> duplicatedNames = new ArrayList<>();
                                    duplicatedNames.add(name);
                                    showDuplicateNamesAlert(duplicatedNames);
                                    return;
                                };
                            }

                            for(Signal signal : loadedSignals)
                            {
                                if(signal.getName().equals(cell.getItem()))
                                {
                                    signal.setName(name);
                                    refreshSignalsListView();
                                    break;
                                }
                            }
                        });
                    });
                    MenuItem deleteItem = new MenuItem();
                    deleteItem.textProperty().bind(Bindings.format("Usuń \"%s\"", cell.itemProperty()));
                    deleteItem.setOnAction(event -> {
                        for(Signal signal : loadedSignals)
                        {
                            if(signal.getName().equals(cell.getItem()))
                            {
                                loadedSignals.remove(signal);
                                signalsListView.getItems().remove(cell.getItem());
                                break;
                            }
                        }
                    });

                    MenuItem exportItem = new MenuItem();
                    exportItem.textProperty().bind(Bindings.format("Eksportuj \"%s\"", cell.itemProperty()));
                    exportItem.setOnAction(event -> {
                        for(Signal signal : loadedSignals)
                        {
                            if(signal.getName().equals(cell.getItem()))
                            {
                                try {
                                    exportSignal(signal);
                                } catch (IOException e) {
                                    Alert ioExceptionAlert = new Alert(Alert.AlertType.ERROR);
                                    ioExceptionAlert.setTitle("Błąd podczas zapisu do pliku");
                                    ioExceptionAlert.setHeaderText(null);
                                    ioExceptionAlert.setContentText(e.getMessage());
                                }
                                break;
                            }
                        }
                    });


                    contextMenu.getItems().add(addToChartItem);

                    if(signalsListView.getSelectionModel().getSelectedItems().size() > 1)
                    {
                        Menu performActionOnSelectedMenu = new Menu("Działania na zaznaczonych...");
                        MenuItem sumSelectedMenuItem = new MenuItem("Dodaj");
                        sumSelectedMenuItem.setOnAction(actionEvent ->
                        {
                            ArrayList<Signal> signals = getSelectedSignals();
                            if(signals.isEmpty()) return;

                            Signal resultSignal = signals.get(0);
                            for(int signalIndex = 1; signalIndex < signals.size(); ++signalIndex)
                            {
                                try {
                                    resultSignal = resultSignal.add(signals.get(signalIndex));
                                } catch (SignalException e) {
                                    showIncompatibleSignalsAlert();
                                    return;
                                }
                            }

                            resultSignal.setName("Sum of selected " + LocalDateTime.now().toString().replace('.', '_').replace(':', '_'));

                            loadedSignals.add(resultSignal);
                            refreshSignalsListView();
                        });

                        MenuItem subtractSelectedMenuItem = new MenuItem("Odejmij");
                        subtractSelectedMenuItem.setOnAction(actionEvent ->
                        {
                            ArrayList<Signal> signals = getSelectedSignals();
                            if(signals.isEmpty()) return;

                            Signal resultSignal = signals.get(0);
                            for(int signalIndex = 1; signalIndex < signals.size(); ++signalIndex)
                            {
                                try {
                                    resultSignal = resultSignal.subtract(signals.get(signalIndex));
                                } catch (SignalException e) {
                                    showIncompatibleSignalsAlert();
                                    return;
                                }
                            }

                            resultSignal.setName("Difference of selected " + LocalDateTime.now().toString().replace('.', '_').replace(':', '_'));

                            loadedSignals.add(resultSignal);
                            refreshSignalsListView();
                        });

                        MenuItem multiplySelectedMenuItem = new MenuItem("Pomnóż");
                        multiplySelectedMenuItem.setOnAction(actionEvent ->
                        {
                            ArrayList<Signal> signals = getSelectedSignals();
                            if(signals.isEmpty()) return;

                            Signal resultSignal = signals.get(0);
                            for(int signalIndex = 1; signalIndex < signals.size(); ++signalIndex)
                            {
                                try {
                                    resultSignal = resultSignal.multiply(signals.get(signalIndex));
                                } catch (SignalException e) {
                                    showIncompatibleSignalsAlert();
                                    return;
                                }
                            }

                            resultSignal.setName("Product of selected " + LocalDateTime.now().toString().replace('.', '_').replace(':', '_'));

                            loadedSignals.add(resultSignal);
                            refreshSignalsListView();
                        });

                        MenuItem divideSelectedMenuItem = new MenuItem("Podziel");
                        divideSelectedMenuItem.setOnAction(actionEvent ->
                        {
                            ArrayList<Signal> signals = getSelectedSignals();
                            if(signals.isEmpty()) return;

                            Signal resultSignal = signals.get(0);
                            for(int signalIndex = 1; signalIndex < signals.size(); ++signalIndex)
                            {
                                try {
                                    resultSignal = resultSignal.divide(signals.get(signalIndex));
                                } catch (SignalException e) {
                                    showIncompatibleSignalsAlert();
                                    return;
                                }
                            }

                            resultSignal.setName("Quotient of selected " + LocalDateTime.now().toString().replace('.', '_').replace(':', '_'));

                            loadedSignals.add(resultSignal);
                            refreshSignalsListView();
                        });

                        performActionOnSelectedMenu.getItems().addAll(
                                sumSelectedMenuItem,
                                subtractSelectedMenuItem,
                                multiplySelectedMenuItem,
                                divideSelectedMenuItem
                        );

                        contextMenu.getItems().add(performActionOnSelectedMenu);
                    }

                    contextMenu.getItems().addAll(
                            new SeparatorMenuItem(),
                            exportItem,
                            editItem,
                            deleteItem);



                    contextMenu.show(signalsListView.getScene().getWindow(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            });

            return cell ;
        });

    }

    private void refreshSignalsListView()
    {
        signalsListView.getItems().clear();
        for(Signal signal : loadedSignals) signalsListView.getItems().add(signal.getName());
    }

    private void drawSignalCurve(Signal signal)
    {
        if(signal == null) return;
        if (lineChart.isVisible()) {
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

    private void drawSignalPoints(Signal signal)
    {
        if(signal == null) return;
        if (scatterChart.isVisible()) {
            scatterChart.getData().add(new XYChart.Series<Number, Number>());
            XYChart.Series<Number, Number> series = scatterChart.getData().get(scatterChart.getData().size() - 1);
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
        double interval = (Math.abs(signal.getAmplitude()) * 2) / intervalSlider.getValue();
        double lowestPossibleValue = -signal.getAmplitude();
        TreeMap<Double, Integer> histogramIntervals = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double aDouble, Double t1) {
                return Math.abs(aDouble - t1) < 0.0000000001 ? 0 : Double.compare(aDouble, t1);
            }
        });
        for(int i = 1; i <= intervalSlider.getValue(); ++i)
        {
            histogramIntervals.put(lowestPossibleValue + i * interval, 0);
        }
        for(Sample sample : signal.getSamples())
        {
            double sampleInterval = lowestPossibleValue + interval;
            while(sampleInterval < sample.value)
            {
                sampleInterval += interval;
            }
            Integer sampleCount = histogramIntervals.get(sampleInterval);
            histogramIntervals.put(sampleInterval, sampleCount + 1);
        }
        if(histogramIntervals.isEmpty()) return;

        barChart.getData().add(new BarChart.Series<>());
        BarChart.Series series = barChart.getData().get(barChart.getData().size() - 1);
        series.setName(signal.getName());
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setRoundingMode(RoundingMode.UP);
        for(Map.Entry<Double, Integer> histogramInterval : histogramIntervals.entrySet())
        {
            series.getData().add(new BarChart.Data<String, Number>("< " + decimalFormat.format(histogramInterval.getKey() - interval) + " , " + decimalFormat.format(histogramInterval.getKey()) + " >", histogramInterval.getValue()));
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
                        probabilityInput.textProperty(),
                        periodInput.textProperty());
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
                        || (!probabilityInput.isDisabled() && probabilityInput.getText().isEmpty())
                        || (!periodInput.isDisabled() && periodInput.getText().isEmpty()));
            }
        };
        generateButton.disableProperty().bind(binding);
    }

    private void enableDisableTextInputs() {
        signalTypeComboBox.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            ArrayList<String> periodAllowedSignals = new ArrayList<>(Arrays.asList(
                    SignalGeneratorFactory.SIGNAL_TYPE_S3_VALUE,
                    SignalGeneratorFactory.SIGNAL_TYPE_S4_VALUE,
                    SignalGeneratorFactory.SIGNAL_TYPE_S5_VALUE,
                    SignalGeneratorFactory.SIGNAL_TYPE_S6_VALUE,
                    SignalGeneratorFactory.SIGNAL_TYPE_S7_VALUE,
                    SignalGeneratorFactory.SIGNAL_TYPE_S8_VALUE
            ));
            ArrayList<String> factorAllowedSignals = new ArrayList<>(Arrays.asList(
                    SignalGeneratorFactory.SIGNAL_TYPE_S6_VALUE,
                    SignalGeneratorFactory.SIGNAL_TYPE_S7_VALUE,
                    SignalGeneratorFactory.SIGNAL_TYPE_S8_VALUE
            ));

            if(periodAllowedSignals.contains(newValue)) {
                periodInput.setDisable(false);
            } else {
                periodInput.setDisable(true);
            }
            if (factorAllowedSignals.contains(newValue)) {
                fillFactorInput.setDisable(false);
            } else {
                fillFactorInput.setDisable(true);
            }
            if (SignalGeneratorFactory.SIGNAL_TYPE_S9_VALUE.equalsIgnoreCase(String.valueOf(newValue))) {
                jumpTimeInput.setDisable(false);
                frequencyInput.setDisable(true);
            } else {
                frequencyInput.setDisable(false);
                jumpTimeInput.setDisable(true);
            }
            if (SignalGeneratorFactory.SIGNAL_TYPE_S10_VALUE.equalsIgnoreCase(String.valueOf(newValue))) {
                sampleNumberInput.setDisable(false);
            } else {
                sampleNumberInput.setDisable(true);
            }
            if (SignalGeneratorFactory.SIGNAL_TYPE_S11_VALUE.equalsIgnoreCase(String.valueOf(newValue))) {
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
        ArrayList<String> duplicatedSignalsNames = new ArrayList<>();

        if(files == null) return new ArrayList<>();

        for(File file : files)
        {
            if(file == null) continue;;

            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));

            try {
                Signal signal = null;

                try
                {
                    signal = (Signal)objectInputStream.readObject();
                }
                catch (InvalidClassException e)
                {
                    Alert invalidClassExceptionAlert = new Alert(Alert.AlertType.ERROR);
                    invalidClassExceptionAlert.setTitle("Błąd importowania");
                    invalidClassExceptionAlert.setHeaderText(null);
                    invalidClassExceptionAlert.setContentText("Format jednego z wybranych plików jest nieprawidłowy. Importowanie przerwane.\nSzczegóły:\n" + file.getName());
                    invalidClassExceptionAlert.showAndWait();
                    return new ArrayList<>();
                }

                boolean isDuplicated = false;
                for(Signal loadedSignal : loadedSignals)
                {
                    if(loadedSignal.getName().equals(signal.getName()))
                    {
                        duplicatedSignalsNames.add(signal.getName());
                        isDuplicated = true;
                        break;
                    }
                }
                if(!isDuplicated) importedSignals.add(signal);
            } catch (ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }

            objectInputStream.close();
        }

        if(!duplicatedSignalsNames.isEmpty()) showDuplicateNamesAlert(duplicatedSignalsNames);

        return importedSignals;
    }

    void addRemoveSignalChart(String signalName)
    {
        XYChart.Series lineChartSeries = null;
        for(XYChart.Series series : lineChart.getData())
        {
            if(series.getName().equals(signalName))
            {
                lineChartSeries = series;
                break;
            }
        }

        BarChart.Series barChartSeries = null;
        for(BarChart.Series series : barChart.getData())
        {
            if(series.getName().equals(signalName))
            {
                barChartSeries = series;
                break;
            }
        }

        if(lineChartSeries != null && barChartSeries != null) {
            lineChart.getData().remove(lineChartSeries);
            barChart.getData().remove(barChartSeries);
        }
        else
        {
            for (Signal signal : loadedSignals) {
                if (signal.getName().equals(signalName)) {
                    drawSignalCurve(signal);
                    drawHistogram(signal);
                    displaySignalParameters(signal);
                    break;
                }
            }
        }
    }

    ArrayList<Signal> getSelectedSignals()
    {
        ArrayList<Signal> selectedSignals = new ArrayList<>();
        for(String signalName : signalsListView.getSelectionModel().getSelectedItems())
        {
            for(Signal signal : loadedSignals)
            {
                if(signal.getName().equals(signalName))
                {
                    selectedSignals.add(signal);
                    break;
                }
            }
        }
        return selectedSignals;
    }

    void showIncompatibleSignalsAlert()
    {
        Alert incompatibleSignalsAlert = new Alert(Alert.AlertType.ERROR);
        incompatibleSignalsAlert.setTitle("Sygnały niekompatybilne");
        incompatibleSignalsAlert.setContentText("Zaznaczone sygnały są ze sobą niekompatybilne.");
        incompatibleSignalsAlert.setHeaderText(null);
        incompatibleSignalsAlert.showAndWait();
    }

    void showDuplicateNamesAlert(List<String> duplicatedNames)
    {
        String duplicatedNamesString = "";
        for(String duplicate : duplicatedNames)
        {
            duplicatedNamesString += "\n- ";
            duplicatedNamesString += duplicate;
        }
        Alert incompatibleSignalsAlert = new Alert(Alert.AlertType.WARNING);
        incompatibleSignalsAlert.setTitle("Duplikaty");
        incompatibleSignalsAlert.setContentText("Jeden lub więcej sygnałów o takiej samej nazwie już istnieją.\nZduplikowane sygnały:" + duplicatedNamesString);
        incompatibleSignalsAlert.setHeaderText(null);
        incompatibleSignalsAlert.showAndWait();
    }

    public void closeApp(ActionEvent actionEvent) {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.close();
        System.exit(0);
    }
}
