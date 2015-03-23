/**
 * Warlight AI Game Bot
 *
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package bot;

/**
 * This is a simple bot that does random (but correct) moves.
 * This class implements the Bot interface and overrides its Move methods.
 * You can implement these methods yourself very easily now,
 * since you can retrieve all information about the match from variable “state”.
 * When the bot decided on the move to make, it returns an ArrayList of Moves. 
 * The bot is started by creating a Parser to which you add
 * a new instance of your bot, and then the parser is started.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import algorithm.DistanceCalculator;
import map.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import strategy.*;

public class BotStarter implements Bot 
{	
	@Override
	/**
	 * Alege regiunea de start
	 * @return Regiunea de start
	 */
	public Region getStartingRegion(BotState state, Long timeOut) {
		return (new StartingRegionPicker(state)).getStartingRegion();
	}
	
	@Override
	/**
	 *
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
		// debugPrint(state);
		return (new ArmiesPlacer(state)).getPlaceArmiesMoves();
	}

	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
		return (new AttacksCreator(state)).getAttackMoves();
	}
	
	/**
	 * Functie de debug. Testeaza toate functiile de selectare regiuni
	 * @param state
	 */
	public void debugPrint(BotState state) {
		System.err.println("Round number: " + state.getRoundNumber());
		
		System.err.println("\n[DEBUG] getMyRegions(); \n");
		for(Region r : state.getStateAnalyzer().getMyRegions()) {
			System.err.println(r.getDebugInfo(state));
		}
		
		System.err.println("\n[DEBUG] getEnemyRegions(); \n");
		for(Region r : state.getStateAnalyzer().getEnemyRegions()) {
			System.err.println(r.getDebugInfo(state));
		}
		
		System.err.println("\n[DEBUG] getMyBorderRegions(); \n");
		for(Region r : state.getStateAnalyzer().getMyBorderRegions()) {
			System.err.println(r.getDebugInfo(state));
		}
		
		System.err.println("\n[DEBUG] getMyCentralRegions(); \n");
		for(Region r : state.getStateAnalyzer().getMyCentralRegions()) {
			System.err.println(r.getDebugInfo(state));
		}
		
		System.err.println("\n[DEBUG] getMyBorderRegionsWithEnemy(); \n");
		for(Region r : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
			System.err.println(r.getDebugInfo(state));
		}
		
		System.err.println("\n[DEBUG] getMyBorderRegionsWithNeutrals(); \n");
		for(Region r : state.getStateAnalyzer().getMyBorderRegionsWithNeutrals()) {
			System.err.println(r.getDebugInfo(state));
		}		
		
//		System.out.println("\n[DEBUG] getShortesPatH(); \n");
//		for(Region central : state.getStateAnalyzer().getMyCentralRegions()) {
//			System.out.println("Regiune start: " + central.getId());
//			System.out.println("Nod urmator imediat" + DistanceCalculator.nextRegionToBorder(central, state.getStateAnalyzer().getMyBorderRegions()).getId());
//			
//			
//			List<Region> path = DistanceCalculator.getShortestPath(central, state.getStateAnalyzer().getMyBorderRegions());
//			
//			for(Region node : path) {
//				System.out.print(node.getId() + " -> ");
//			}
//			
//			System.out.println();
//		}
		
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
