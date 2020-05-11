package controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.signal.Sample;
import model.signal.Signal;
import model.signal.converter.*;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConversionDialogController implements Initializable
{
    @FXML private Button buttonConvert;
    @FXML private Button buttonSave;
    @FXML private TextField textFieldFrequency;
    @FXML private TextField textFieldQuantizerBits;
    @FXML private TextField textFieldSamplesCount;
    @FXML private ComboBox comboBoxQuantizer;
    @FXML private ComboBox comboBoxDAConversionType;
    @FXML private LineChart<Number, Number> lineChart;

    @FXML private Label labelQuantizationMSE;
    @FXML private Label labelQuantizationSNR;
    @FXML private Label labelQuantizationPSNR;
    @FXML private Label labelQuantizationMD;

    @FXML private Label labelSamplingMSE;
    @FXML private Label labelSamplingSNR;
    @FXML private Label labelSamplingPSNR;
    @FXML private Label labelSamplingMD;

    private Signal signal;
    private Signal outputSignal = null;
    private Signal tempOutputSignal = null;
    private Stage stage;

    public final String QUANTIZER_TYPE_Q0_VALUE = "(Q0) Bez kwantyzacji";
    public final String QUANTIZER_TYPE_Q1_VALUE = "(Q1) Kwantyzacja równomierna z obcięciem";
    public final String QUANTIZER_TYPE_Q2_VALUE = "(Q2) Kwantyzacja równomierna z zaokrąglaniem";

    public final String CONVERSION_TYPE_R0_VALUE = "(R0) Brak";
    public final String CONVERSION_TYPE_R1_VALUE = "(R1) Ekstrapolacja zerowego rzędu";
    public final String CONVERSION_TYPE_R2_VALUE = "(R2) Interpolacja pierwszego rzędu";
    public final String CONVERSION_TYPE_R3_VALUE = "(R3) Rekonstrukcja w oparciu o funkcję sinc";
    
    private DecimalFormat decimalFormat = new DecimalFormat("#.####");

    private void resetMeasurements()
    {
        labelQuantizationMSE.setText(decimalFormat.format(0));
        labelQuantizationSNR.setText(decimalFormat.format(0));
        labelQuantizationPSNR.setText(decimalFormat.format(0));
        labelQuantizationMD.setText(decimalFormat.format(0));
        labelSamplingMSE.setText(decimalFormat.format(0));
        labelSamplingSNR.setText(decimalFormat.format(0));
        labelSamplingPSNR.setText(decimalFormat.format(0));
        labelSamplingMD.setText(decimalFormat.format(0));
    }
    
    
    private void showADMeasurements(Signal signal)
    {
        if(signal != null)
        {
            labelQuantizationMSE.setText(decimalFormat.format(signal.getMeanSquareError()));
            labelQuantizationSNR.setText(decimalFormat.format(signal.getSignalNoiseRatio()));
            labelQuantizationPSNR.setText(decimalFormat.format(signal.getPeakSignalNoiseRation()));
            labelQuantizationMD.setText(decimalFormat.format(signal.getMaximumDifference()));
        }
    }

    private void showDAMeasurements(Signal signal)
    {
        if(signal != null)
        {
            labelSamplingMSE.setText(decimalFormat.format(signal.getMeanSquareError()));
            labelSamplingSNR.setText(decimalFormat.format(signal.getSignalNoiseRatio()));
            labelSamplingPSNR.setText(decimalFormat.format(signal.getPeakSignalNoiseRation()));
            labelSamplingMD.setText(decimalFormat.format(signal.getMaximumDifference()));
        }
    }

    private void drawSignalCurve(Signal signal)
    {
        if(signal == null) return;
        if (lineChart.isVisible()) {
            if(lineChart.getData().size() > 1) lineChart.getData().remove(lineChart.getData().size() - 1);
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
    
    
    public void setStage(Stage stage)
    {
        this.stage = stage;
    }

    private boolean saveOutputSignal()
    {
        if(tempOutputSignal == null) return false;
        TextInputDialog dialog = new TextInputDialog(tempOutputSignal.getName());
        dialog.setTitle("Nowa nazwa");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name ->
        {
            outputSignal = tempOutputSignal;
            outputSignal.setName(name);
        });
        return result.isPresent();
    }


    public void setSignal(Signal signal)
    {
        this.signal = signal;
        drawSignalCurve(signal);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        lineChart.setTitle("Krzywa sygnału");
        lineChart.setLegendVisible(false);
        lineChart.getXAxis().setLabel("Czas");
        lineChart.getYAxis().setLabel("Wartość");

        comboBoxQuantizer.getItems().addAll(
                QUANTIZER_TYPE_Q0_VALUE,
                QUANTIZER_TYPE_Q1_VALUE,
                QUANTIZER_TYPE_Q2_VALUE
        );
        comboBoxQuantizer.getSelectionModel().selectFirst();
        comboBoxQuantizer.setVisibleRowCount(3);

        comboBoxDAConversionType.getItems().addAll(
                CONVERSION_TYPE_R0_VALUE,
                CONVERSION_TYPE_R1_VALUE,
                CONVERSION_TYPE_R2_VALUE,
                CONVERSION_TYPE_R3_VALUE);
        comboBoxDAConversionType.getSelectionModel().selectFirst();
        comboBoxDAConversionType.setVisibleRowCount(4);

        buttonConvert.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                resetMeasurements();

                Quantizer quantizer = null;
                if(comboBoxQuantizer.getSelectionModel().getSelectedItem() == QUANTIZER_TYPE_Q1_VALUE)
                {
                    int quantizerBits = Integer.parseInt(textFieldQuantizerBits.getText());
                    quantizer = new TruncationQuantizer(quantizerBits);
                }
                else if(comboBoxQuantizer.getSelectionModel().getSelectedItem() == QUANTIZER_TYPE_Q2_VALUE)
                {
                    int quantizerBits = Integer.parseInt(textFieldQuantizerBits.getText());
                    quantizer = new RoundingQuantizer(quantizerBits);
                }

                ConversionStrategy conversionStrategy = null;
                if(comboBoxDAConversionType.getSelectionModel().getSelectedItem() == CONVERSION_TYPE_R1_VALUE)
                {
                    conversionStrategy = new ZeroOrderHoldConversionStrategy();
                }
                else if(comboBoxDAConversionType.getSelectionModel().getSelectedItem() == CONVERSION_TYPE_R2_VALUE)
                {
                    conversionStrategy = new FirstOrderHoldConversionStrategy();
                }
                else if(comboBoxDAConversionType.getSelectionModel().getSelectedItem() == CONVERSION_TYPE_R3_VALUE)
                {
                    conversionStrategy = new SincFunctionConversionStrategy(Integer.parseInt(textFieldSamplesCount.getText()));
                }

                ADConverter converterAD = new ADConverter(quantizer);
                double samplingFrequency = Double.parseDouble(textFieldFrequency.getText());
                try {
                    tempOutputSignal = converterAD.convert(signal, samplingFrequency);
                } catch (ADConverter.ADConverterException e) {
                    showADConverterAlert(e.getMessage());
                    return;
                }
                showADMeasurements(tempOutputSignal);
                if(conversionStrategy != null)
                {
                    DAConverter converterDA = new DAConverter();
                    converterDA.setConversionStrategy(conversionStrategy);
                    tempOutputSignal = converterDA.convert(tempOutputSignal, signal.getFrequency());
                    signal.calculateMeasurements(tempOutputSignal);
                    showDAMeasurements(tempOutputSignal);
                }
                tempOutputSignal.setName("Converted" + signal.getName());
                drawSignalCurve(tempOutputSignal);
                buttonSave.setDisable(false);
            }
        });

        buttonSave.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                if(saveOutputSignal()) stage.close();
            }
        });
    }

    void showADConverterAlert(String errorMessage)
    {
        Alert incompatibleSignalsAlert = new Alert(Alert.AlertType.ERROR);
        incompatibleSignalsAlert.setTitle("Błąd konwersji");
        incompatibleSignalsAlert.setContentText(errorMessage);
        incompatibleSignalsAlert.setHeaderText(null);
        incompatibleSignalsAlert.showAndWait();
    }

    public void initData(Signal signal)
    {
        setSignal(signal);
    }

    public Signal getOutputSignal()
    {
        return outputSignal;
    }
}
