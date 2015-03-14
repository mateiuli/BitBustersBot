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
import java.util.LinkedList;

import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.Move;
import move.PlaceArmiesMove;

public class BotStarter implements Bot 
{
	@Override
	/**
	 * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
	 * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
	 * This method returns one random region from the given pickable regions.
	 */
	public Region getStartingRegion(BotState state, Long timeOut)
	{
		/**
		 * Regiunea de start este aleasa ca acea regiune a carei super 
		 * regiune are un numar minim de copii
		 */
		
		// NUMAR MINIM DE WASTELANDS
		
		// Inainte de toate am nevoie sa stiu cate wastelanduri are fiecare super-regiune
		setSuperRegionsWastelands(state);
		
		// Super-regiunile cu numarul minim de wastelanduri 
		ArrayList<SuperRegion> minSuperRegions = new ArrayList<>();
		
		// Numarul minim de wastelanduri
		int minNumberOfWastelands = Integer.MAX_VALUE;
		
		// Lista cu regiunile din care fac parte posibilele regiuni de start
		ArrayList<Region> minRegions = new ArrayList<>();
			
		for(Region region : state.getPickableStartingRegions()) {
			// Daca s-a gasit un minim nou, sterg lista si adaug 
			if(region.getSuperRegion().getNumberOfWastelands() < minNumberOfWastelands) {
				minRegions.clear();
				minRegions.add(region);
				// noul minim
				minNumberOfWastelands = region.getSuperRegion().getNumberOfWastelands();
			}
			// Daca au acelasi minim decat o adaug in lista
			else if(region.getSuperRegion().getNumberOfWastelands() == minNumberOfWastelands) {
				minRegions.add(region);
			} 			
		}
		
		// NUMAR MAXIM DE BONUS
		ArrayList<Region> optimalRegions = new ArrayList<>();
		int maxBonus = Integer.MIN_VALUE;
		
		for(Region region : minRegions) {
			if(region.getSuperRegion().getArmiesReward() > maxBonus) {
				optimalRegions.clear();
				optimalRegions.add(region);
				// noul maxim
				maxBonus = region.getSuperRegion().getArmiesReward();				
			}
			else if(region.getSuperRegion().getArmiesReward() == maxBonus) {
				optimalRegions.add(region);
			}
		}		
		
		// NUMAR MINIM DE COPII
		
		// Iar acum aici am o lista cu super-regiunile regiunilor posibila cu numarul
		// minim de wastlanduri
		Region startingRegion = null;
		int minChildRegions = Integer.MAX_VALUE;
		
		// Parcurg fiecare regiune din lista cu regiuni posibile de start
		for(Region region : optimalRegions) {
			// Daca regiunea face parte dintr-o superregiune cu un 
			// numar mai mic de copii decat am presupus, o aleg				
			if(region.getSuperRegion().getSubRegions().size() < minChildRegions) {
				minChildRegions = region.getSuperRegion().getSubRegions().size();
				startingRegion = region;
			}
		}
		
		return startingRegion;
	}
	
	/**
	 * Seteaza pentru fiecare super-regiune numarul de wasteland-uri din ea
	 * @param state
	 */
	public void setSuperRegionsWastelands(BotState state)
	{
		for(Region wasteland : state.getWasteLands()) {
			wasteland.getSuperRegion().incrementNumberOfWastelands();
		}
	}

	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{
		
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		//int armies = 2;
		int armiesLeft = state.getStartingArmies();
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();
		
				
		/**
		 * Eu aici cred asa. noi trebuie sa parcurgem lista cu regiuni. Vedem care sunt ale noastre (vizibile)
		 * La fiecare regiune din asta, trebuie sa ii parcurgem vecinii (neighbours) si le marcam, le dam niste etichete ceva
		 * de exemplu o marcam cu INAMIC_LANGA, sau NEUTRU_LANGA, sau daca sunt numia d-ale noastre langa, si tot asa :) (transfer armata)
		 * Noi mai intai ar trebui sa facem o statistica, sa construim statistica asta, adica sa parcurgem regiunile si le marcam
		 * Ca dupa, eventual pe cele neutre le bagam intr-o lista, pe cele cu inamici in alta, si pe cele ale noastre in alta
		 * si dupa vedem, cati inamici sunt, cate neutre, si cate d-ale noastre
		 * Da, exact :) camp la Region.java
		 * Uite. Uitativa ca nu stium cum se scrie U-i-t-a-t-v-a. detaliii.... :))
		 * Le parcurgem o data, le marcam, si salvam si pointeri (referinte) in vectori ca sa nu mai cautam din nou sa vedem care e liber
		 * 
		 */	
		
		// Lista cu regiuni ce se afla pe granita		
		ArrayList<Region> borderRegions = new ArrayList<>();
		
		// Lista cu regiuni centrale
		ArrayList<Region> centralRegions = new ArrayList<>();
		
		// care ziceai ca e al 3-lea?
		ArrayList<Region> nearEnemies = new ArrayList<>();
		
		for(Region region : state.getVisibleMap().getRegions()) {
			
		}
		
		// Acum aici am lista cu regiuni de pe margine
		
		while(armiesLeft > 0)
		{
			/*double rand = Math.random();
			int r = (int) (rand*visibleRegions.size());
			Region region = visibleRegions.get(r);
			
			if(region.ownedByPlayer(myName))
			{
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
				armiesLeft -= armies;
			}*/
		
		}
		
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
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		String myName = state.getMyPlayerName();
		int armies = 5;
		int maxTransfers = 10;
		int transfers = 0;
		
		for(Region fromRegion : state.getVisibleMap().getRegions())
		{
			if(fromRegion.ownedByPlayer(myName)) //do an attack
			{
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
					else if(toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 1
								&& transfers < maxTransfers) //do a transfer
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						transfers++;
						break;
					}
					else
						possibleToRegions.remove(toRegion);
				}
			}
		}
		
		return attackTransferMoves;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
