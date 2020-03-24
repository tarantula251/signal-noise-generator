package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../view/layout/sample.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());

//        final PieChart chart = new PieChart(
//                FXCollections.observableArrayList(
//                        new PieChart.Data("China",         1344.0),
//                        new PieChart.Data("India",         1241.0),
//                        new PieChart.Data("United States",  310.5)
//                )
//        );
//        chart.setTitle("Population 2011");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
