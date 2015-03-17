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
	
	private ArrayList<AttackTransferMove>  attackTransferMoves = new ArrayList<>(); 
	
	@Override
	/**
	 * Alege regiunea de start
	 */
	public Region getStartingRegion(BotState state, Long timeOut) {
		return (new StartingRegionPicker(state)).getStartingRegion();
	}
	
	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{				
		// Golesc lista cu mutarile anterioare - urmeaza miscari noi
		attackTransferMoves.clear();
		
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		int armies = 2;
		int armiesLeft = state.getStartingArmies();

		for(Region centralRegion : state.getStateAnalyzer().getMyCentralRegions()) {
			if(centralRegion.getArmies() < 2)
				continue;
			
			// Regiunea imediat urmatoare din drumul cel mai scurt catre o bordura
			Region nextRegion = DistanceCalculator.nextRegionToBorder(centralRegion, state.getStateAnalyzer().getMyBorderRegions());
			
			// Adaugam miscarea de transfer
			attackTransferMoves.add(new AttackTransferMove(myName, centralRegion, nextRegion, centralRegion.getArmies() - 1));
			
			if(nextRegion.isOnBorder(state)) {	
				// Avertizeaza regiunea urmatoare ca vor veni nu numar de calareti pe regiunea asta
				nextRegion.addUpcomingArmiesOnTransfer(centralRegion.getArmies() - 1);
			} // Verifica sa se faca clear pe upcomingArmies !!				
		}
		
		// Sortam regiunile de pe bordura in functie de raportul dintre armata de pe regiune
		// si suma armatelor inaimcilor din jur
		Collections.sort(state.getStateAnalyzer().getMyBorderRegionsWithEnemy(), new Comparator<Region>() {
			@Override
			public int compare(Region o1, Region o2) {
				Double ratio1 = o1.getMyArmyEnemyArmyRatio(state);
				Double ratio2 = o2.getMyArmyEnemyArmyRatio(state);
				
				return ratio1.compareTo(ratio2);
			}			
		});
		
		for(Region borderRegion : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
			if(borderRegion.getMyArmyEnemyArmyRatio(state) < 1.d) {
				int armyDiff = borderRegion.getNoOfEnemyArmiesAround(state.getOpponentPlayerName()) - borderRegion.getArmiesWithUpcomingArmies();
				
				if(armyDiff < 0)
					System.out.println("ERROR!");
				
				if(armyDiff <= armiesLeft) {
					placeArmiesMoves.add(new PlaceArmiesMove(myName, borderRegion, armyDiff));
					
					armiesLeft -= armyDiff;
				}
				else {
					placeArmiesMoves.add(new PlaceArmiesMove(myName, borderRegion, armiesLeft));
					armiesLeft = 0;
				}
				
				if(armiesLeft <= 0)
					break;				
			}
		}
		
		
		
//		while(armiesLeft > 0) {
//			double rand = Math.random();
//			int r = (int) (rand* state.getVisibleMap().regions.size());
//			Region region = state.getVisibleMap().regions.get(r);
//			
//			if(region.ownedByPlayer(myName)) {
//				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
//				armiesLeft -= armies;
//			}
//		}
		// debugPrint();
		return placeArmiesMoves;
	}

	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		String myName = state.getMyPlayerName();
		int armies = 5;
		int maxTransfers = 10;
		int transfers = 0;
		
		for(Region fromRegion : state.getVisibleMap().getRegions()) {
			// Do an attack
			if(fromRegion.ownedByPlayer(myName)) {
				ArrayList<Region> possibleToRegions = new ArrayList<Region>();
				possibleToRegions.addAll(fromRegion.getNeighbors());
				
				while(!possibleToRegions.isEmpty())
				{
					double rand = Math.random();
					int r = (int) (rand*possibleToRegions.size());
					Region toRegion = possibleToRegions.get(r);
					
					if(!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 6) //do an attack
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else
						possibleToRegions.remove(toRegion);
				}
			}
		}

		return attackTransferMoves;
	}
	
	/**
	 * Functie de debug. Testeaza toate functiile de selectare regiuni
	 * @param state
	 */
	public void debugPrint(BotState state) {
		
		System.out.println("\n[DEBUG] getMyRegions(); \n");
		for(Region r : state.getStateAnalyzer().getMyRegions()) {
			System.out.println(r.getDebugInfo(state));
		}
		
		System.out.println("\n[DEBUG] getEnemyRegions(); \n");
		for(Region r : state.getStateAnalyzer().getEnemyRegions()) {
			System.out.println(r.getDebugInfo(state));
		}
		
		System.out.println("\n[DEBUG] getMyBorderRegions(); \n");
		for(Region r : state.getStateAnalyzer().getMyBorderRegions()) {
			System.out.println(r.getDebugInfo(state));
		}
		
		System.out.println("\n[DEBUG] getMyCentralRegions(); \n");
		for(Region r : state.getStateAnalyzer().getMyCentralRegions()) {
			System.out.println(r.getDebugInfo(state));
		}
		
		System.out.println("\n[DEBUG] getMyBorderRegionsWithEnemy(); \n");
		for(Region r : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
			System.out.println(r.getDebugInfo(state));
		}
		
		System.out.println("\n[DEBUG] getMyBorderRegionsWithNeutrals(); \n");
		for(Region r : state.getStateAnalyzer().getMyBorderRegionsWithNeutrals()) {
			System.out.println(r.getDebugInfo(state));
		}		
		
		System.out.println("\n[DEBUG] getShortesPatH(); \n");
		for(Region central : state.getStateAnalyzer().getMyCentralRegions()) {
			System.out.println("Regiune start: " + central.getId());
			System.out.println("Nod urmator imediat" + DistanceCalculator.nextRegionToBorder(central, state.getStateAnalyzer().getMyBorderRegions()).getId());
			
			
			List<Region> path = DistanceCalculator.getShortestPath(central, state.getStateAnalyzer().getMyBorderRegions());
			
			for(Region node : path) {
				System.out.print(node.getId() + " -> ");
			}
			
			System.out.println();
		}
		
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
