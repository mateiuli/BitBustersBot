
package move;
import map.Region;

public class BorderTransferMove {
	
	private Region fromRegion;
	private Region toRegion;
	private int armies;
	
	public BorderTransferMove(String playerName, Region fromRegion, Region toRegion, int armies)
	{
		this.fromRegion = fromRegion;
		this.toRegion = toRegion;
		this.armies = armies;
	}
        
        public Region getFromRegion() {
		return fromRegion;
	}
	
	/**
	 * @return The Region this Move is attacking or transferring to
	 */
	public Region getToRegion() {
		return toRegion;
	}
	
	/**
	 * @return The number of armies this Move is attacking or transferring with
	 */
	public int getArmies() {
		return armies;
	}
}
