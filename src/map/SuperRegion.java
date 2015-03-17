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

import bot.BotState;

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
		String owner = ownedByPlayer();
		
		if(owner == null)
			return false;
		
		return owner.equals(playerName);
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
	
	public String getDebugInfo(BotState state) {
		StringBuffer sb = new StringBuffer();
		sb.append("ID: " + id + "\n");
		sb.append("Armies reward: " + armiesReward + "\n");
		sb.append("No of wastelands: " + "\n");
		
		return sb.toString();
	}		
}
