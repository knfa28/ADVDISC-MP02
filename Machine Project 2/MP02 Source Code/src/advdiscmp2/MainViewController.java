package advdiscmp2;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.controlsfx.control.Notifications;

import advdiscmp2.model.MarkovChain;
import advdiscmp2.view.SoftwareNotification;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainViewController {
	@FXML Spinner<Integer> numWordsSpinner;
	@FXML TextArea textArea;
	@FXML TextField fileOpenedTextField;
	@FXML Label stateLengthLabel;
	@FXML Label statusLabel;

	private File file;
	private MarkovChain markovChain;
	private int stateLength;
	
	@FXML private void initialize() {
		statusLabel.setText("");
		stateLength = getStateLength();
		stateLengthLabel.setText(stateLength + "");
		markovChain = new MarkovChain();

		numWordsSpinner.setValueFactory(
				new SpinnerValueFactory
					.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 3));
	}
	
	private int getStateLength() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Enter State Length");
		dialog.setHeaderText("");
		dialog.setContentText("State Length:");
        dialog.setGraphic(null);
        
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            if (!isInteger(dialog.getEditor().getText())) {
    			String errorMsg = "Please enter a positive integer";
    			
    			Notifications.create().title("Error").text(errorMsg)
    				.position(Pos.TOP_RIGHT).showError();
                event.consume();
            }
        });
        Optional<String> intStr = dialog.showAndWait();
        if (intStr.isPresent()){
        	return Integer.parseInt(intStr.get());
        }
       return 1; 
	}
	
	private boolean isInteger(String intStr) {
		try {
			int i = Integer.parseInt(intStr);
			return i > 0;
		} catch (Exception e) {
			return false;
		}	
	}
	
	@FXML protected void generateText(ActionEvent event) throws IOException {
		if (!markovChain.isTrained()) {
			SoftwareNotification.notifyError("Please select a training text "
					+ "file before generating text.");
		} else {
			textArea.setText(markovChain
					.generateText(numWordsSpinner.getValue()));
		}
	}
	
	@FXML protected void openFile(ActionEvent event) throws IOException {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter =
				new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extFilter);
		Stage stage = new Stage();
		file = fileChooser.showOpenDialog(stage);
		if (file != null) {
			fileOpenedTextField.setText(file.getName());
			String[] input = fileToString(file);
			markovChain.setInput(input, stateLength);
			statusLabel.setText(markovChain.getNumStates() + " states loaded");
		} else {
			statusLabel.setText("");
		}
		
	}
	
	private String[] fileToString(File file) {
		try {
			List<String> ret = Files.readAllLines(file.toPath());
			return ret.toArray(new String[ret.size()]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@FXML protected void showVisualization(ActionEvent event) {
		try {
			if (markovChain.getLastStatesGenerated() == null) {
				SoftwareNotification.notifyError("Please generate something"
						+ " first to be able to see the visualization");
			} else {
				FXMLLoader loader = new FXMLLoader(getClass()
						.getResource("view/show_visualization.fxml"));
				Parent root = loader.load();
				ShowVisualizationController controller = loader.getController();
				controller.initialize(markovChain.getStates(),
						markovChain.getStochasticMatrix(),
						markovChain.getLastStatesGenerated());
				
				Stage stage = new Stage();
				stage.setScene(new Scene(root));
				stage.show();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
