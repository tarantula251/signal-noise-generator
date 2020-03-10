package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ViewController implements Initializable {
    // menu elements
    @FXML private ComboBox signalTypeComboBox;
    @FXML private TextField durationInput;
    @FXML private TextField startingTimeInput;
    @FXML private TextField amplitudeInput;
    @FXML private TextField frequencyInput;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        signalTypeComboBox.setVisibleRowCount(3);
    }
}
