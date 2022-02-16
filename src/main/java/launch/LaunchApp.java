package launch;
	
import java.io.IOException;

import doctor.params.DoctorParams;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class LaunchApp extends Application {
	
	private static Stage stage;
	private double xOffset = 0, yOffset = 0;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource(DoctorParams.LOG_IN_VIEW));
			primaryStage.setTitle("Telelepsia Hospitals");
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			primaryStage.setScene(scene);
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			
			stage = primaryStage;
			
			// In case the user wants to move the app along the screen
			moveWindow(root, stage);
			
			stage.show();
			
		} catch (IOException fatal_error) {
			fatal_error.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public static Stage getStage() {
		return stage;
	}
	
	public void moveWindow(Parent root, Stage stage) {
		
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});
		
		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.setOpacity(0.8);
				stage.setX(event.getScreenX() - xOffset);
				stage.setY(event.getScreenY() - yOffset);
			}
		});
		
		root.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.setOpacity(1);
			}
		});
	}
}
