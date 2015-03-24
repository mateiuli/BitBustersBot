/**
 * Warlight AI Game Bot
 *
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import bot.BotState;


public class Region {
	
	// ID-ul unic al regiunii
	private int id;
	
	// Vecinii regiunii
	private LinkedList<Region> neighbors;
	
	// Super-regiunea din care face parte regiunea
	private SuperRegion superRegion;
	
	// Numarul de armate de pe regiune
	private int armies;
	
	// Numarul de armate care vor fi transferate in viitor
	// ca sa nu mai plasam armate pe el in prima etapa
	private int upcomingArmiesOnTransfer;
	
	// Numele player-ului ce detine regiunea
	private String playerName;
	
	public Region(int id, SuperRegion superRegion) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = "unknown";
		this.armies = 0;
		this.upcomingArmiesOnTransfer = 0;
		
		superRegion.addSubRegion(this);
	}
	
	public Region(int id, SuperRegion superRegion, String playerName, int armies) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = playerName;
		this.armies = armies;
		
		superRegion.addSubRegion(this);
	}
	
	public void addNeighbor(Region neighbor) {
		if(!neighbors.contains(neighbor)) {
			neighbors.add(neighbor);
			neighbor.addNeighbor(this);
		}
	}
	
	/**
	 * @param region a Region object
	 * @return True if this Region is a neighbor of given Region, false otherwise
	 */
	public boolean isNeighbor(Region region) {
		if(neighbors.contains(region))
			return true;
		return false;
	}

	/**
	 * @param playerName A string with a player's name
	 * @return True if this region is owned by given playerName, false otherwise
	 */
	public boolean ownedByPlayer(String playerName) {
		if(playerName.equals(this.playerName))
			return true;
		
		return false;
	}
	
	/**
	 * @param armies Sets the number of armies that are on this Region
	 */
	public void setArmies(int armies) {
		this.armies = armies;
	}
	
	/**
	 * Adauga un numar de calareti la armata curenta
	 * @param armies
	 */
	public void addArmies(int armies) {
		this.armies += armies;
	}
	
	
	/**
	 * @param playerName Sets the Name of the player that this Region belongs to
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	/**
	 * @return The id of this Region
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return A list of this Region's neighboring Regions
	 */
	public LinkedList<Region> getNeighbors() {
		return neighbors;
	}
	
	/**
	 * @return The SuperRegion this Region is part of
	 */
	public SuperRegion getSuperRegion() {
		return superRegion;
	}
	
	/**
	 * @return The number of armies on this region
	 */
	public int getArmies() {
		return armies;
	}
	
	/**
	 * @return A string with the name of the player that owns this region
	 */
	public String getPlayerName() {
			return playerName;
	}
	
	/**
	 * 
	 * @return True if this region is not owned by anyone
	 */
	public boolean isNeutral() {
		return ownedByPlayer("neutral");
	}
	
	/**
	 * 
	 * @param armies
	 */
	public void setUpcomingArmiesOnTransfer(int armies) {
		upcomingArmiesOnTransfer = armies;
	}
	
	/**
	 * 
	 * @return Numarul de armate ce vor fi plsate pe regiune in vitor dupa transfer
	 */
	public int getUpcomingArmiesOnTransfer() {
		return upcomingArmiesOnTransfer;
	}
	
	/**
	 * Adauga armies la valoarea curenta a upcomingArmiesOnTransfer
	 * @param armies 
	 */
	public void addUpcomingArmiesOnTransfer(int armies) {
		upcomingArmiesOnTransfer += armies;
	}
	
	/**
	 * Set upcomingArmiesOnTransfer = 0
	 */
	public void clearUpcomingArmiesOnTransfer() {
		setUpcomingArmiesOnTransfer(0);
	}
	
	/**
	 * Verifica daca regiunea se afla pe frontiera
	 * @param playerName Jucatorul care ar trebui sa o detina
	 * @return
	 */
	public boolean isOnBorder(BotState state) {
		for(Region neighbor : neighbors)
			if(!neighbor.ownedByPlayer(playerName))
				return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param superRegionId
	 * @return Lista cu vecinii din aceasi super-regiune ca regiunea
	 */
	public List<Region> getSuperRegionNeighbors() {
		List<Region> superRegionNeighbors = new ArrayList<>();
		
		for(Region neighbor : neighbors)
			if(neighbor.superRegion.getId() == superRegion.getId())
				superRegionNeighbors.add(neighbor);
		
		return superRegionNeighbors;
	}
	
	/**
	 * 
	 * @param playeName
	 * @return Lista cu inamici vecini
	 */
	public List<Region> getEnemiesAround(String opponentPlayerName) {
		//if(enemiesAround != null)
		//	return enemiesAround;
		
		List<Region> enemiesAround = new ArrayList<>();
		
		for(Region neighbor : neighbors) {
			if(neighbor.ownedByPlayer(opponentPlayerName))
				enemiesAround.add(neighbor);
		}
		
		return enemiesAround; 
	}
	
	/**
	 * 
	 * @return Numarul de armate neutre din vecinatatea regiunii
	 */
	public int getNoOfNeutralArmies() {
		int noOfNeutralArmies = 0;
		
		for(Region neighbor : neighbors)
			if(neighbor.isNeutral())
				noOfNeutralArmies += neighbor.armies;
		
		return noOfNeutralArmies;
	}
	
	/**
	 * 
	 * @param opponentPlayerName
	 * @return Numarul de calareti de pe toate regiunile inamice din vecinatate 
	 */
	public int getNoOfEnemyArmiesAround(String opponentPlayerName) {
		int noOfEnemies = 0;
		
		for(Region neighbor : neighbors) {
			if(neighbor.ownedByPlayer(opponentPlayerName))
				noOfEnemies += neighbor.armies - 1;
		}
		
		return noOfEnemies;
	}
	
	/**
	 * 
	 * @param playerName
	 * @return True daca regiunea se invecineaza cu inamici, fals daca nu
	 */
	public boolean hasEnemiesAround(BotState state) {
		for(Region neighbor : neighbors)
			if(neighbor.ownedByPlayer(state.getOpponentPlayerName()))
				return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param state
	 * @return Raportul dintre numarul de calarati ai regiunii dupa posibile transferuri pe regiune
	 * si numarul de calareti de pe toate regiunile inamice din zona
	 */
    public double getMyArmyEnemyArmyRatio(BotState state) {
    	double noOfEnemyArmies = getNoOfEnemyArmiesAround(state.getOpponentPlayerName());
    	
    	if(noOfEnemyArmies == 0)
    		noOfEnemyArmies = 1;
    	
        return ((double) ((getArmiesWithUpcomingArmies() - 1)/ noOfEnemyArmies));
    }
        
    /**
     * 
     * @return Numarul de calareti ce vor fi pe regiune dupa transferurile ce vor fi executate
     */
    public int getArmiesWithUpcomingArmies() {
    	return armies + upcomingArmiesOnTransfer;
    }
	
    
    /**
     * Sorteaza vecinii regiunii dupa un criteriu dat
     * @param comparator
     */
    public void sortNeighbors(Comparator<? super Region> comparator) {
    	Collections.sort(neighbors, comparator);
    }
    
    /**
     * Afiseaza informatii de debug
     * @param state
     * @return
     */
	public String getDebugInfo(BotState state) {
		StringBuffer sb = new StringBuffer();
		sb.append("ID: " + id + ", SuperRegion ID: " + superRegion.getId() + "\n");
		sb.append("Owned by: " + playerName + "\nArmies: " + armies + "\n");
		sb.append("Neighbors: ");
		
		for(Region n : neighbors)
			sb.append("Region " + n.getId() + "("+ n.getArmies() +" + " + n.getUpcomingArmiesOnTransfer() + "), ");
		
		sb.append("\nIs on border: " + (isOnBorder(state) ? "Yes" : "No") + "\n");
		sb.append("Has enemies around: " + (hasEnemiesAround(state) ? "Yes" : "No") + "\n");
		sb.append("Is neutral: " + (isNeutral() ? "Yes" : "No") + "\n");
		sb.append("Upcoming armies: " + getUpcomingArmiesOnTransfer() + "\n");
		sb.append("My army/enemy army ratio: " + getMyArmyEnemyArmyRatio(state) + "\n");
		sb.append("Super-region neighbors: " + getSuperRegionNeighbors().size() + "\n");
		
		return sb.toString();
	}
	
	public boolean isAlone() {
		for(Region neighbor : neighbors) {
			if(neighbor.ownedByPlayer(this.playerName))
				return false;
		}
		
		return true;
	}
	
}
