package doctor.treeobjects;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class AddPatientTreeObject extends RecursiveTreeObject<AddPatientTreeObject> {

	private final int patientId;
	private final StringProperty patientName;
	private final StringProperty patientIdNumber;
	private final ObjectProperty<JFXCheckBox> selectedPatient;
	
	public AddPatientTreeObject(int patientId, String patientName, String patientIdNumber) {
		this.patientId = patientId;
		this.patientName = new SimpleStringProperty(patientName);
		this.patientIdNumber = new SimpleStringProperty(patientIdNumber);
		JFXCheckBox checkBox = new JFXCheckBox();
		checkBox.setCheckedColor(Color.web("#009d73",1.0));
		this.selectedPatient = new SimpleObjectProperty<>(checkBox);
	}

	public int getPatientId() {return patientId;}

	public StringProperty getPatientName() {return patientName;}

	public StringProperty getPatientIdNumber() {return patientIdNumber;}

	public ObjectProperty<JFXCheckBox> getSelectedPatient() {return selectedPatient;}
}
