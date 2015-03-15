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
import java.util.LinkedList;
import java.util.List;


public class Region {
	
	// ID-ul unic al regiunii
	private int id;
	
	// Vecinii regiunii
	private LinkedList<Region> neighbors;
	
	// Super-regiunea din care face parte regiunea
	private SuperRegion superRegion;
	
	// Numarul de armate de pe regiune
	private int armies;
	
	// Numele player-ului ce detine regiunea
	private String playerName;
	
	// Lista cu inamici din jur
	private List<Region> enemiesAround;
			
	public Region(int id, SuperRegion superRegion)
	{
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = "unknown";
		this.armies = 0;
		this.enemiesAround = null;
		
		superRegion.addSubRegion(this);
	}
	
	public Region(int id, SuperRegion superRegion, String playerName, int armies)
	{
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = playerName;
		this.armies = armies;
		
		superRegion.addSubRegion(this);
	}
	
	public void addNeighbor(Region neighbor)
	{
		if(!neighbors.contains(neighbor))
		{
			neighbors.add(neighbor);
			neighbor.addNeighbor(this);
		}
	}
	
	/**
	 * @param region a Region object
	 * @return True if this Region is a neighbor of given Region, false otherwise
	 */
	public boolean isNeighbor(Region region)
	{
		if(neighbors.contains(region))
			return true;
		return false;
	}

	/**
	 * @param playerName A string with a player's name
	 * @return True if this region is owned by given playerName, false otherwise
	 */
	public boolean ownedByPlayer(String playerName)
	{
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
		return ownedByPlayer("unknown");
	}
	
	/**
	 * Verifica daca regiunea se afla pe frontiera
	 * @param playerName Jucatorul care ar trebui sa o detina
	 * @return
	 */
	public boolean isOnBorder(String playerName) {
		for(Region neighbor : neighbors)
			if(!neighbor.ownedByPlayer(playerName))
				return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param playeName
	 * @return Lista cu inamici vecini
	 */
	public List<Region> getEnemiesAround(String playerName) {
		//if(enemiesAround != null)
		//	return enemiesAround;
		
		List<Region> enemiesAround = new ArrayList<>();
		
		for(Region neighbor : neighbors) {
			if(!neighbor.ownedByPlayer(playerName) && !neighbor.isNeutral())
				enemiesAround.add(neighbor);
		}
		
		return enemiesAround; 
	}
	
	/**
	 * 
	 * @param playerName
	 * @return True daca regiunea se invecineaza cu inamici, fals daca nu
	 */
	public boolean hasEnemiesAround(String playerName) {
		for(Region neighbor : neighbors)
			if(!neighbor.ownedByPlayer(playerName) && !neighbor.isNeutral())
				return true;
		
		return false;
	}
				

}
