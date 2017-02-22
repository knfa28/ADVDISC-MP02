package advdiscmp2;

import java.util.List;

import advdiscmp2.model.State;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class ShowVisualizationController {
	@FXML GridPane gridPane;
	@FXML Button previousButton;
	@FXML Button nextButton;
	
	private int stepNum;
	private Label currHighlighted;
	private List<State> states;
	private List<State> lastStatesGenerated;
	private double[][] stochasticMatrix;
	
	
	protected void initialize(List<State> states, double[][] stochasticMatrix,
			List<State> lastStatesGenerated) {
		gridPane.gridLinesVisibleProperty().set(true);
		
		// initialize label of rows and columns 
		for (int i = 0, j = 1; i < states.size(); i++, j++) {
			Label lbl = new Label(states.get(i).toString());
			gridPane.add(lbl, 0, j);
		}
		
		for (int i = 0, j = 1; i < states.size(); i++, j++) {
			Label lbl = new Label(states.get(i).toString());
			gridPane.add(lbl, j, 0);
		}
	
		this.states = states;
		this.stochasticMatrix = stochasticMatrix;
		this.lastStatesGenerated = lastStatesGenerated;
		previousButton.setDisable(true);
		stepNum = 0;
		displayStep();
	}
	
	private void displayStep() {
		for (int i = 0, r = 1; i < states.size(); i++, r++) {
			for (int j = 0, c = 1; j < states.size(); j++, c++) {
				Label lbl = new Label(stochasticMatrix[i][j] + "");
				gridPane.add(lbl, c, r);
			}
		}
		if (stepNum < lastStatesGenerated.size() - 1) {
			State fromState = lastStatesGenerated.get(stepNum);
			State toState = lastStatesGenerated.get(stepNum + 1); 
			int r = states.indexOf(fromState) + 1;
			int c = states.indexOf(toState) + 1;
			
			highLightCell(r, c);
		}

	}
	
	private void highLightCell(int r, int c) {
		Label lbl = (Label) getNodeByRowColumnIndex(r, c, gridPane);
		lbl.setStyle("-fx-background-color: orange;");
		currHighlighted = lbl;
	}
	
	
	 public Node getNodeByRowColumnIndex(final int row,final int column,GridPane gridPane) {
	        Node result = null;
	        ObservableList<Node> childrens = gridPane.getChildren();
	        for(Node node : childrens) {
	        	Integer colIndex = GridPane.getColumnIndex(node);
	        	Integer rowIndex = GridPane.getRowIndex(node);
	            if (rowIndex != null && rowIndex.intValue() == row 
	            		&& colIndex != null && colIndex.intValue() == column) {
	                result = node;
	                break;
	            }
	        }
	        return result;
	    }
	
	@FXML void previousStep(ActionEvent event) {
		if (stepNum > 0) {
			if (currHighlighted != null)
				currHighlighted.setStyle("");
			--stepNum;
			displayStep();
			nextButton.setDisable(false);
		}
		
		if (stepNum == 0) {
			previousButton.setDisable(true);
		}
		
	}
	
	@FXML void nextStep(ActionEvent event) {
		if (stepNum < states.size() - 1) {
			if (currHighlighted != null)
				currHighlighted.setStyle("");
			++stepNum;
			displayStep();
			previousButton.setDisable(false);
		}
		
		if (stepNum == lastStatesGenerated.size() - 2) {
			nextButton.setDisable(true);
		}
	}

}
