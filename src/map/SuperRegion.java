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
import java.util.LinkedList;

public class SuperRegion {
	
	private int id;
	private int armiesReward;
	private LinkedList<Region> subRegions;
	private int numberOfWastelands;
	
	public SuperRegion(int id, int armiesReward)
	{
		this.id = id;
		this.armiesReward = armiesReward;
		this.numberOfWastelands = 0;
		subRegions = new LinkedList<Region>();
	}
	
	/**
	 * Verifica daca super regiunea are cel putin un wasteland
	 * @return
	 */
	public boolean hasWastelands()
	{
		return numberOfWastelands > 0 ? true : false;
	}
	
	/**
	 * Incrementeaza numarul de wastelanduri ale super-regiunii 
	 */
	public void incrementNumberOfWastelands()
	{
		numberOfWastelands++;
	}
	
	/**
	 * Intoarce numarul de wastelanduri
	 * @return
	 */
	public int getNumberOfWastelands()
	{
		return numberOfWastelands;
	}
	
	public void addSubRegion(Region subRegion)
	{
		if(!subRegions.contains(subRegion))
			subRegions.add(subRegion);
	}
	
	/**
	 * @return A string with the name of the player that fully owns this SuperRegion
	 */
	public String ownedByPlayer()
	{
		String playerName = subRegions.getFirst().getPlayerName();
		for(Region region : subRegions)
		{
			if (!playerName.equals(region.getPlayerName()))
				return null;
		}
		return playerName;
	}
	
	/**
	 * 
	 * @param playerName
	 * @return True daca playerName detin intreaga super-regiune, false daca nu
	 */
	public boolean ownedByPlayer(String playerName) {
		for(Region region : subRegions)
			if(!region.ownedByPlayer(playerName))
				return false;
		
		return true;
	}
	
	/**
	 * @return The id of this SuperRegion
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return The number of armies a Player is rewarded when he fully owns this SuperRegion
	 */
	public int getArmiesReward() {
		return armiesReward;
	}
	
	/**
	 * @return A list with the Regions that are part of this SuperRegion
	 */
	public LinkedList<Region> getSubRegions() {
		return subRegions;
	}
}
