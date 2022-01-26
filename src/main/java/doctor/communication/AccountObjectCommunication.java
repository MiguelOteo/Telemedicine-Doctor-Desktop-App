package doctor.communication;

import com.jfoenix.controls.JFXButton;

import doctor.models.Doctor;
import javafx.scene.layout.AnchorPane;

public class AccountObjectCommunication {
	
	// To share the account on all the stages/panes etc
    private static Doctor doctor;
    private static int databaseId;
   
	// To set the blur on the whole menu
    private static AnchorPane anchorPane;
    private static JFXButton buttonControl1;
    
    public static Doctor getDoctor() {return doctor;}
    
    public static void setDoctor(Doctor passedDoctor) {doctor = passedDoctor;}
    
	public static int getDatabaseId() {return databaseId;}

	public static void setDatabaseId(int id) {databaseId = id;}
    
    public static AnchorPane getAnchorPane() {return anchorPane;}
    
    public static void setAnchorPane(AnchorPane anchor) {anchorPane = anchor;}

	public static JFXButton getButtonControl1() {return buttonControl1;}

	public static void setButtonControl1(JFXButton buttonControl) {buttonControl1 = buttonControl;}
}