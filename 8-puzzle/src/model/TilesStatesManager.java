package model;

import java.awt.Point;
import java.util.ArrayList;

/*
 * This class implements the methods for the functions managing the states 
 * as changing the parent, getting the frontiers etc. where all the required logic is here.
 */

public class TilesStatesManager {
	private final int numberValues = 9;
	private final int goalStateValue = 912345678;
	// This class is a singleton
	private static TilesStatesManager single_instance = null;

	// static method to create instance of the class
	public static TilesStatesManager getInstance() {
		if (single_instance == null)
			single_instance = new TilesStatesManager();
		return single_instance;
	}

	// A method takes the state and a parent and sets the parent node to it
	public void changeParent(TilesState state, TilesState newParent) {
		state.setParent(newParent);
		state.setPathCost(newParent.getPathCost() + 1);
		// to be revised
		state.setDepth(Math.max(state.getDepth(), newParent.getDepth() + 1));
	}

	// change parent only if it leads to a path with less cost to reach this state
	public void changeParentIfLessCost(TilesState state, TilesState newParent) {
		if (state.getPathCost() > newParent.getPathCost() + 1)
			changeParent(state, newParent);
	}

	/*
	 * given a location, the number in that location in the state value is returned
	 * numbers are read from left to right and locations start from 0
	 */
	public int getNumberAt(TilesState state, int location) {
		// Check for out of bounds
		if (location > numberValues - 1 || location < 0)
			return -1;
		return (int) state.getValue() / ((int) Math.pow(10, numberValues - location - 1)) % 10;
	}

	// Calculating the heuristics given a state
	public float getHeuristicManhattan(TilesState state) {
		// calculating the total Manhattan distance for each of the numbers in this
		// state and getting the total.
		float total = 0;
		for (int location = 0; location < numberValues; location++) {
			int currentNumber = getNumberAt(state, location);
			Point finalLocation = finalGridLocation(currentNumber);
			Point currentLocation = correspondingGridLocation(location);
			total += Math.abs(finalLocation.x - currentLocation.x) + Math.abs(finalLocation.y - currentLocation.y);
		}
		return total;
	}

	public float getHeuristicEuclidean(TilesState state) {
		// calculating the total Euclidean distance for each of the numbers in this
		// state and getting the total.
		float total = 0;
		for (int location = 0; location < numberValues; location++) {
			int currentNumber = getNumberAt(state, location);
			Point finalLocation = finalGridLocation(currentNumber);
			Point currentLocation = correspondingGridLocation(location);
			total += Math.sqrt(Math.pow((finalLocation.x - currentLocation.x), 2)
					+ Math.pow((finalLocation.y - currentLocation.y), 2));
		}
		return total;
	}

	/*
	 * returns the grid location of a certain number to help in heuristics
	 * calculation zero indexed where 1 is in location (0,1) 2 in (0,2) , 3 in (1,0)
	 * etc. this is to be used in the heuristic functions calculations.
	 */
	private Point finalGridLocation(int number) {
		// to work with the found functions, we need the empty value to be represented
		// by 0
		if (number == TilesState.getEmptyValue())
			number = 0;
		// for the first value of the ordered pair, by inspecting the values as a
		// function of the input number we found that
		int x = (int) number / 3;
		// for the second value of the ordered pair, by inspecting the values as a
		// function of the input number we found that
		int y = (int) number % 3;
		return new Point(x, y);
	}

	private Point correspondingGridLocation(int location) {
		// By close inspection we found that it corresponds to the final grid location
		// corresponding to this location as a number.
		return finalGridLocation(location);
	}

	// This function checks if the state is a goal state
	public boolean isGoalState(TilesState state) {
		return state.getValue() == goalStateValue;
	}

	// This function checks whether two states are equivalent
	public boolean sameStates(TilesState state1, TilesState state2) {
		return state1.getValue() == state2.getValue();
	}

	/*
	 * This function gets takes state and returns the frontiers (next states)
	 * generated by it we generate frontiers by checking if swapping the empty value
	 * with the number at its: 1- left 2- top 3- right 4- bottom is possible
	 */
	public ArrayList<TilesState> getNextStates(TilesState state) {
		ArrayList<TilesState> nextStates = new ArrayList<TilesState>();
		// getting the blank location to act accordingly
		int blankLocation = getBlankLocation(state);
		// Adding states if possible starting by checking left swaps and so on
		int leftLocation = getLeftLocation(blankLocation);
		if (leftLocation != -1)
			nextStates.add(TilesStatesFactory.getInstance()
					.createState(swapLocations(state, blankLocation, leftLocation), state));
		int topLocation = getTopLocation(blankLocation);
		if (topLocation != -1)
			nextStates.add(TilesStatesFactory.getInstance()
					.createState(swapLocations(state, blankLocation, topLocation), state));
		int rightLocation = getRightLocation(blankLocation);
		if (rightLocation != -1)
			nextStates.add(TilesStatesFactory.getInstance()
					.createState(swapLocations(state, blankLocation, rightLocation), state));
		int bottomLocation = getBottomLocation(blankLocation);
		if (bottomLocation != -1)
			nextStates.add(TilesStatesFactory.getInstance()
					.createState(swapLocations(state, blankLocation, bottomLocation), state));
		return nextStates;
	}

	private int getBlankLocation(TilesState state) {
		int i;
		for (i = 0; i < numberValues; i++) {
			if (getNumberAt(state, i) == TilesState.getEmptyValue())
				break;
		}
		return i;
	}

	private int swapLocations(TilesState state, int loc1, int loc2) {
		// using some mathematics we swap the two locations
		int totalValue = state.getValue();
		int value1 = getNumberAt(state, loc1);
		int value2 = getNumberAt(state, loc2);
		// removing the two numbers
		int result = totalValue - ((int) (value1 * Math.pow(10, numberValues - loc1 - 1)
				+ value2 * Math.pow(10, numberValues - loc2 - 1)));
		// adding them at swapped locations
		result += ((int) (value1 * Math.pow(10, numberValues - loc2 - 1)
				+ value2 * Math.pow(10, numberValues - loc1 - 1)));
		return result;
	}

	// These function obtain the proximity of a location and return -1 if out of
	// bounds
	// These functions were designed based on careful observation of the tiles
	// indices
	private int getLeftLocation(int location) {
		if (location % 3 == 0) {
			return -1;
		}
		return location - 1;
	}

	private int getTopLocation(int location) {
		if (location < 3) {
			return -1;
		}
		return location - 3;
	}

	private int getRightLocation(int location) {
		if (location % 3 == 2) {
			return -1;
		}
		return location + 1;
	}

	private int getBottomLocation(int location) {
		if (location > 5) {
			return -1;
		}
		return location + 3;
	}
}
