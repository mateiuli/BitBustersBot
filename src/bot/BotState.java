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

import java.util.ArrayList;

import map.Map;
import map.Region;
import map.SuperRegion;

import move.AttackTransferMove;
import move.PlaceArmiesMove;
import move.Move;

public class BotState {
	
	// Numele meu de jucator
	private String myName = "";
	
	// Numele de jucator al inamicului
	private String opponentName = "";
	
	// Harta completa ca structura
	private final Map fullMap = new Map();
	
	// Harta ce vad eu
	private Map visibleMap; 
	
	// Regiunile posibile de start
	private ArrayList<Region> pickableStartingRegions; 
	
	// Regiunile nasoale - watelands
	private ArrayList<Region> wastelands;
	
	// Miscarile inamicului de la sfarsitul rundei
	private ArrayList<Move> opponentMoves;

	// Numarul de armate pe care jucatorul pe poate plasa pe harta
	private int startingArmies;
	
	// Numarul maxim de runde
	private int maxRounds;
	
	// Runda curenta a jocului
	private int roundNumber;
	
	// Timpul total ce poate fi in timebank
	private long totalTimebank;
	
	// Timpul ce se adauga in timebank pe fiecare mutare
	private long timePerMove; 
	
	// Analizator de situatie
	private StateAnalyzer stateAnalyzer = null;
	
	public BotState()
	{
		opponentMoves = new ArrayList<Move>();
		roundNumber = 0;
	}
	
	public void updateSettings(String key, String[] parts)
	{
		String value;

		if(key.equals("starting_regions") && parts.length > 3) {
			setPickableStartingRegions(parts);
			return;
		} 
		value = parts[2];

		if(key.equals("your_bot")) //bot's own name
			myName = parts[2];
		else if(key.equals("opponent_bot")) //opponent's name
			opponentName = value;
		else if(key.equals("max_rounds"))
			maxRounds = Integer.parseInt(value);
		else if(key.equals("timebank"))
			totalTimebank = Long.parseLong(value);
		else if(key.equals("time_per_move"))
			timePerMove = Long.parseLong(value);
		else if(key.equals("starting_armies")) 
		{
			startingArmies = Integer.parseInt(value);
			roundNumber++; //next round
		}
	}
	
	/**
	 * Initial map is given to the bot with all the information except for player and armies info
	 * @param mapInput
	 */
	public void setupMap(String[] mapInput)
	{
		int i, regionId, superRegionId, wastelandId, reward;
		
		if(mapInput[1].equals("super_regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					superRegionId = Integer.parseInt(mapInput[i]);
					i++;
					reward = Integer.parseInt(mapInput[i]);
					fullMap.add(new SuperRegion(superRegionId, reward));
				}
				catch(Exception e) {
					System.err.println("Unable to parse SuperRegions");
				}
			}
		}
		else if(mapInput[1].equals("regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					regionId = Integer.parseInt(mapInput[i]);
					i++;
					superRegionId = Integer.parseInt(mapInput[i]);
					SuperRegion superRegion = fullMap.getSuperRegion(superRegionId);
					fullMap.add(new Region(regionId, superRegion));
				}
				catch(Exception e) {
					System.err.println("Unable to parse Regions " + e.getMessage());
				}
			}
		}
		else if(mapInput[1].equals("neighbors"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					Region region = fullMap.getRegion(Integer.parseInt(mapInput[i]));
					i++;
					String[] neighborIds = mapInput[i].split(",");
					for(int j=0; j<neighborIds.length; j++)
					{
						Region neighbor = fullMap.getRegion(Integer.parseInt(neighborIds[j]));
						region.addNeighbor(neighbor);
					}
				}
				catch(Exception e) {
					System.err.println("Unable to parse Neighbors " + e.getMessage());
				}
			}
		}
		else if(mapInput[1].equals("wastelands"))
		{
			wastelands = new ArrayList<Region>();
			for(i=2; i<mapInput.length; i++)
			{
				try {
					wastelandId = Integer.parseInt(mapInput[i]);
					wastelands.add(fullMap.getRegion(wastelandId));
				}
				catch(Exception e) {
					System.err.println("Unable to parse wastelands " + e.getMessage());
				}
			}
		}
	}
	
	/**
	 * regions from wich a player is able to pick his preferred starting region
	 * @param input
	 */
	public void setPickableStartingRegions(String[] input)
	{
		pickableStartingRegions = new ArrayList<Region>();
		for(int i=2; i<input.length; i++)
		{
			int regionId;
			try {
				regionId = Integer.parseInt(input[i]);
				Region pickableRegion = fullMap.getRegion(regionId);
				pickableStartingRegions.add(pickableRegion);
			}
			catch(Exception e) {
				System.err.println("Unable to parse pickable regions " + e.getMessage());
			}
		}
	}
	
	/**
	 * visible regions are given to the bot with player and armies info
	 * @param mapInput
	 */
	public void updateMap(String[] mapInput)
	{
		visibleMap = fullMap.getMapCopy();
		for(int i=1; i<mapInput.length; i++)
		{
			try {
				Region region = visibleMap.getRegion(Integer.parseInt(mapInput[i]));
				String playerName = mapInput[i+1];
				int armies = Integer.parseInt(mapInput[i+2]);
				
				region.setPlayerName(playerName);
				region.setArmies(armies);
				// region.clearEnemiesList();
				i += 2;
			}
			catch(Exception e) {
				System.err.println("Unable to parse Map Update " + e.getMessage());
			}
		}
		ArrayList<Region> unknownRegions = new ArrayList<Region>();
		
		// remove regions which are unknown.
		for(Region region : visibleMap.regions)
			if(region.getPlayerName().equals("unknown"))
				unknownRegions.add(region);
		
		for(Region unknownRegion : unknownRegions)
			visibleMap.getRegions().remove(unknownRegion);
		
		// Reanalizeaza situatia curenta
		stateAnalyzer = new StateAnalyzer(this);
	}

	/**
	 * Parses a list of the opponent's moves every round. 
	 * Clears it at the start, so only the moves of this round are stored.
	 * @param moveInput
	 */
	public void readOpponentMoves(String[] moveInput)
	{
		opponentMoves.clear();
		for(int i=1; i<moveInput.length; i++)
		{
			try {
				Move move;
				if(moveInput[i+1].equals("place_armies")) {
					Region region = visibleMap.getRegion(Integer.parseInt(moveInput[i+2]));
					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i+3]);
					move = new PlaceArmiesMove(playerName, region, armies);
					i += 3;
				}
				else if(moveInput[i+1].equals("attack/transfer")) {
					Region fromRegion = visibleMap.getRegion(Integer.parseInt(moveInput[i+2]));
					if(fromRegion == null) //might happen if the region isn't visible
						fromRegion = fullMap.getRegion(Integer.parseInt(moveInput[i+2]));

					Region toRegion = visibleMap.getRegion(Integer.parseInt(moveInput[i+3]));
					if(toRegion == null) //might happen if the region isn't visible
						toRegion = fullMap.getRegion(Integer.parseInt(moveInput[i+3]));

					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i+4]);
					move = new AttackTransferMove(playerName, fromRegion, toRegion, armies);
					i += 4;
				}
				else { //never happens
					continue;
				}
				opponentMoves.add(move);
			}
			catch(Exception e) {
				System.err.println("Unable to parse Opponent moves " + e.getMessage());
			}
		}
	}
	
	public String getMyPlayerName(){
		return myName;
	}
	
	public String getOpponentPlayerName(){
		return opponentName;
	}
	
	public int getStartingArmies(){
		return startingArmies;
	}
	
	public int getRoundNumber(){
		return roundNumber;
	}
	
	public Map getVisibleMap(){
		return visibleMap;
	}
	
	public Map getFullMap(){
		return fullMap;
	}

	public ArrayList<Move> getOpponentMoves(){
		return opponentMoves;
	}
	
	public ArrayList<Region> getPickableStartingRegions(){
		return pickableStartingRegions;
	}

	public ArrayList<Region> getWasteLands(){
		return wastelands;
	}
	
	public StateAnalyzer getStateAnalyzer() {
		return stateAnalyzer;
	}

}
