package doctor.treeobjects;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.layout.Pane;

public class MessagePatientTreeObject extends RecursiveTreeObject<MessagePatientTreeObject> {

	//private Pane mainPane;
	private final int patientId;
	
	private final StringProperty patientName;
	private final StringProperty patientEmail;
	private final ObjectProperty<JFXButton> messagePatient;
	
	public MessagePatientTreeObject(int patientId, String patientName, String patientEmail, Pane pane) {
		
		//this.mainPane = pane;
		this.patientId = patientId;
		
		this.patientName = new SimpleStringProperty(patientName);
		this.patientEmail = new SimpleStringProperty(patientEmail);
		
		JFXButton messageButton = new JFXButton("Message");
		messageButton.getStyleClass().add("tree_table_button");
		messageButton.setOnAction((ActionEvent event) -> openMessagePatient());
		
		this.messagePatient = new SimpleObjectProperty<>(messageButton);
	}
	
	private void openMessagePatient() {
		
	}
	
	// Getters methods
	public int getPatientId() {return patientId;}

	public StringProperty getPatientName() {return patientName;}

	public StringProperty getPatientEmail() {return patientEmail;}

	public ObjectProperty<JFXButton> getMessagePatient() {return messagePatient;}
}
