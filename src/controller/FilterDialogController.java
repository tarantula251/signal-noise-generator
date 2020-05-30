package controller;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import model.signal.Sample;
import model.signal.Signal;
import model.signal.filter.Filter;
import model.signal.filter.FilterGenerator;

import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

public class FilterDialogController implements Initializable {
    @FXML
    private Button filterBtn;
    @FXML private Button buttonSave;
    @FXML private TextField f0Input;
    @FXML private TextField mCountInput;
    @FXML private ComboBox filterTypeComboBox;
    @FXML private ComboBox windowTypeComboBox;
    @FXML private LineChart<Number, Number> filterLineChart;
    @FXML private LineChart<Number, Number> responseLineChart;

    private Signal signal;
    private Signal outputSignal = null;
    private Signal filteredOutputSignal = null;
    private Signal responseOutputSignal = null;
    private Stage stage;

    public static final String WINDOW_TYPE_O1_VALUE = "(O1) Okno prostokątne";
    public static final String WINDOW_TYPE_O2_VALUE = "(O2) Okno Blackmana";

    public static final String FILTER_TYPE_F1_VALUE = "(F1) Fitr dolnoprzepustowy";
    public static final String FILTER_TYPE_F2_VALUE = "(F2) Fitr środkowoprzepustowy";

    private void drawFilteredSignalCurve(Signal signal) {
        if(signal == null) return;
        if (filterLineChart.isVisible()) {
            if(filterLineChart.getData().size() > 1) filterLineChart.getData().remove(filterLineChart.getData().size() - 1);
            filterLineChart.getData().add(new XYChart.Series<Number, Number>());
            XYChart.Series<Number, Number> series = filterLineChart.getData().get(filterLineChart.getData().size() - 1);

            if(!signal.isContinuous()) series.nodeProperty().get().setStyle("-fx-stroke: transparent;");
            series.setName(signal.getName());

            for(Sample sample : signal.getSamples())
            {
                series.getData().add(new XYChart.Data<Number, Number>(sample.time, sample.value));
                if(signal.isContinuous()) series.getData().get(series.getData().size() - 1).getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: transparent");
            }
        }
    }

    private void drawResponseSignalCurve(Signal signal) {
        if(signal == null) return;
        if (responseLineChart.isVisible()) {
            if(responseLineChart.getData().size() > 1) responseLineChart.getData().remove(responseLineChart.getData().size() - 1);
            responseLineChart.getData().add(new XYChart.Series<Number, Number>());
            XYChart.Series<Number, Number> series = responseLineChart.getData().get(responseLineChart.getData().size() - 1);

            if(!signal.isContinuous()) series.nodeProperty().get().setStyle("-fx-stroke: transparent;");
            series.setName(signal.getName());

            for(Sample sample : signal.getSamples())
            {
                series.getData().add(new XYChart.Data<Number, Number>(sample.time, sample.value));
                if(signal.isContinuous()) series.getData().get(series.getData().size() - 1).getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: transparent");
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private boolean saveOutputSignal() {
        if(filteredOutputSignal == null) return false;
        TextInputDialog dialog = new TextInputDialog(filteredOutputSignal.getName());
        dialog.setTitle("Nowa nazwa");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name ->
        {
            outputSignal = filteredOutputSignal;
            outputSignal.setName(name);
        });
        return result.isPresent();
    }


    public void setSignal(Signal signal) {
        this.signal = signal;
        drawFilteredSignalCurve(signal);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filterLineChart.setTitle("Sygnał przefiltrowany");
        filterLineChart.setLegendVisible(false);
        filterLineChart.getXAxis().setLabel("Czas");
        filterLineChart.getYAxis().setLabel("Wartość");

        responseLineChart.setTitle("Odpowiedź impulsowa");
        responseLineChart.setLegendVisible(false);
        responseLineChart.getXAxis().setLabel("Czas");
        responseLineChart.getYAxis().setLabel("Wartość");

        filterTypeComboBox.getItems().addAll(
                FILTER_TYPE_F1_VALUE,
                FILTER_TYPE_F2_VALUE
        );
        filterTypeComboBox.getSelectionModel().selectFirst();
        filterTypeComboBox.setVisibleRowCount(2);

        windowTypeComboBox.getItems().addAll(
                WINDOW_TYPE_O1_VALUE,
                WINDOW_TYPE_O2_VALUE
        );
        windowTypeComboBox.getSelectionModel().selectFirst();
        windowTypeComboBox.setVisibleRowCount(2);

        setTextFieldsValidation();
        enableDisableFilterBtn();

        filterBtn.setOnAction(actionEvent -> {
            double f0Frequency = Double.parseDouble(f0Input.getText());
            int mCount = Integer.parseInt(mCountInput.getText());
            Filter filter = null;
            if (!filterTypeComboBox.getSelectionModel().isEmpty() &&
                    !windowTypeComboBox.getSelectionModel().isEmpty()) {
                filter = new FilterGenerator((String) windowTypeComboBox.getSelectionModel().getSelectedItem(), (String) filterTypeComboBox.getSelectionModel().getSelectedItem());
            }

            HashMap<String, Signal> signalsMap = filter.filter(signal, f0Frequency, mCount);
            filteredOutputSignal = signalsMap.get(Filter.FILTERED_SIGNAL);
            responseOutputSignal = signalsMap.get(Filter.IMPULSE_RESPONSE_SIGNAL);

            filteredOutputSignal.setName("Filtered" + signal.getName());
            responseOutputSignal.setName("Impulse Response" + signal.getName());
            drawFilteredSignalCurve(filteredOutputSignal);
            drawResponseSignalCurve(responseOutputSignal);
            buttonSave.setDisable(false);
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

    public void initData(Signal signal) {
        setSignal(signal);
    }

    public Signal getOutputSignal() {
        return outputSignal;
    }

    private void setTextFieldsValidation() {
        mCountInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if (!newValue.matches("^\\d*[13579]$")) {
                    mCountInput.setText(oldValue);
                }
            }
        });
        f0Input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if (!newValue.matches("\\d*(\\.\\d*)?")) {
                    f0Input.setText(oldValue);
                }
            }
        });
    }

    private void enableDisableFilterBtn() {
        BooleanBinding binding = new BooleanBinding() {
            {
                super.bind(f0Input.textProperty(),
                        mCountInput.textProperty());
            }
            @Override
            protected boolean computeValue() {
                return (f0Input.getText().isEmpty()
                        || mCountInput.getText().isEmpty());
            }
        };
        filterBtn.disableProperty().bind(binding);
    }
}
