package advdiscmp2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

public class MarkovChain {
	private List<State> states;
	private List<State> initialStates;
	private List<State> terminalStates;
	private List<State> lastStatesGenerated;
	private double[][] stochasticMatrix;
	
	public MarkovChain() {
		initialStates = new ArrayList<>();
		terminalStates = new ArrayList<>();
	}
	
	public void setInput(String[] input, int stateLength) {
		train(input, stateLength);
	}
	
	private void train(String[] input, int stateLength) {
		Map<State, List<String>> trainingMap = new HashMap<>();
		
		for (int i = 0; i < input.length; i++) {
			trainUsingTextLine(trainingMap, stateLength, input[i]);
		}
		
		stochasticMatrix = convertToStochasticMatrix(trainingMap, stateLength);
	}
	
	private void trainUsingTextLine(Map<State, List<String>> trainingMap,
			int stateLength, String trainingTextLine) {
		String[] words =
				trainingTextLine.toLowerCase().split(" "); // change to " "
												   // if markov words
		
		if ((0 + stateLength) <= words.length) {
			State initialFromState = getFromState(words, 0, stateLength);
			initialStates.add(initialFromState);
			
			State terminalFromState =
					getFromState(words, words.length - stateLength, stateLength);
			terminalStates.add(terminalFromState);
		}
		
		for (int i = 0; (i + stateLength) <= words.length; i++) {
			State fromState = getFromState(words, i, stateLength);
			String value = getLastWordToState(words, i, stateLength);
			
			List<String> valueList = trainingMap.get(fromState);
			if (valueList == null) {
				valueList = new ArrayList<>();
			}
			
			if (value != null) {
				valueList.add(value);
			}
			
			trainingMap.put(fromState, valueList);
		}
	}
	
	private State getFromState(String[] words, int start, int stateLength) {
		String[] fromStateString = new String[stateLength];
		for (int j = 0; j < stateLength; j++) {
			fromStateString[j] = words[start + j];
		}
		return new State(fromStateString);
	}
	
	private String getLastWordToState(String[] words, int start, int stateLength) {
		String value = 
				(start + stateLength  < words.length) 
					? words[start + stateLength] 
					: null;
		return value;
	}
	
	private double[][] convertToStochasticMatrix(Map<State,
			List<String>> trainingMap, int stateLength) {
		int numStates = trainingMap.keySet().size();
		states = new ArrayList<>(trainingMap.keySet());
		double[][] stochasticMatrix = 
				new double[numStates][numStates];

		for (int i = 0; i < states.size(); i++) {
			State state = states.get(i);
			double[] transProbability =
					computeTransitionProbability(state, stateLength, 
							trainingMap.get(state));
			for (int j = 0; j < transProbability.length; j++) {
				stochasticMatrix[i][j] = transProbability[j];
			}
		}
		
		return stochasticMatrix;
	}
	
	private double[] computeTransitionProbability(State fromState, int stateLength, 
			List<String> wordsAfterFromStateList) {
		double[] transitionProbability = new double[states.size()];
		int total = states.size();
		
		if (wordsAfterFromStateList.size() == 0) {
			for (int i = 0; i < total; i++) {
				transitionProbability[i] = (double) 1 / (double) total;
			}
		} else {
			total = wordsAfterFromStateList.size(); 
			
			for (String word : wordsAfterFromStateList) {
				int occurences = Collections.frequency(wordsAfterFromStateList, word);
				double probability = (double) occurences/ (double) total;
				
				State toState = fromState.getToState(word, stateLength);
				int index = states.indexOf(toState);
				transitionProbability[index] = probability;
			}
		}
		
		return transitionProbability;
	}
	
	public String generateText(int wordLength) {
		lastStatesGenerated = new ArrayList<>();
		List<State> states = new ArrayList<>();
		
		State randomInitialState = getRandomInitialState();
		states.add(0, randomInitialState);
		lastStatesGenerated.add(randomInitialState);
		
		int stateLength = randomInitialState.size(); 
		for (int i = 1, w = stateLength; true; i++, w+= stateLength) {
			State k =  getWeightedRandomState(states.get(i - 1));
			states.add(i, k);
			lastStatesGenerated.add(k);
			if (terminalStates.contains(k) && w >= wordLength) { 
				break;
			}
		}
		
		return format(convertToString(states));
	}
	
	private State getRandomInitialState() {
		Random rand = new Random(); 
		int index = rand.nextInt(initialStates.size()); 
		State randomState = initialStates.get(index);

		return randomState;
	}
	
	private State getWeightedRandomState(State state) {
		List<Pair<State, Double>> stateWeights = new ArrayList<>();
		
		int r = states.indexOf(state);
		for (int c = 0; c < states.size(); c++) {
			stateWeights.add(new Pair<>(states.get(c), stochasticMatrix[r][c]));
		}
		
		State weightedRandomState = 
				new EnumeratedDistribution<State>(stateWeights).sample();
		return weightedRandomState;
	}
	
	private String convertToString(List<State> states) {
		StringBuilder sb = new StringBuilder();
		
		// add all elements of inital state
		State initialState = states.get(0);
		for (int i = 0; i < initialState.size(); i++) {
			sb.append(initialState.get(i) + " "); // change to " " if markov words
		}
		
		// add only last element of state
		for (int i = 1; i < states.size(); i++) {
			State k = states.get(i);
			String lastStringInState = k.get(k.size() - 1);
			sb.append(lastStringInState + " "); // change to " " if markov words
		}
		
		return sb.toString();
	}
	
	private String format(final String line) {
		   return Character.toUpperCase(line.charAt(0)) 
				   + line.substring(1).trim();
	}
	
	public boolean isTrained() {
		return stochasticMatrix != null;
	}
	
	public int getNumStates() {
		return (states == null) ? 0 : states.size(); 
	}

	public List<State> getStates() {
		return states;
	}

	public double[][] getStochasticMatrix() {
		return stochasticMatrix;
	}

	public List<State> getLastStatesGenerated() {
		return lastStatesGenerated;
	}
}
