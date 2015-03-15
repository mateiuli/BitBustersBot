package bot;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import map.Region;
import map.SuperRegion;

/**
 * This tool analyzes the current map and builds information
 * that is used later on the pipeline by the upcoming moves creator 
 * @author iulian
 *
 */

public class SituationAnalyzer {
		
	/**
	 * 
	 * @return Returneaza lista cu regiunile vizibile detinute 
	 * de catre mine
	 */
	public static List<Region> getMyRegions(BotState state) {
		List<Region> myRegions = new ArrayList<>();
		
		for(Region region : state.getVisibleMap().regions) 
			if(region.ownedByPlayer(state.getMyPlayerName()))
				myRegions.add(region);
		
		return myRegions;
	}
	
	/**
	 * 
	 * @return Lista cu regiunile mele ce se afla pe frontiera
	 * <br /> indiferent de ce vecini are, enemy sau neutral
	 */
	public static List<Region> getMyBorderRegions(BotState state) {
		List<Region> myBorderRegions = new ArrayList<>();
		
		for(Region region : state.getVisibleMap().regions)
			if(region.isOnBorder(state.getMyPlayerName()))
				myBorderRegions.add(region);
		
		return myBorderRegions;
	}
	
	/**
	 * 
	 * @return Lista cu regiunle ce se afla pe 
	 * frontiera si se invecineaza cu inamici
	 */
	public static List<Region> getMyBorderRegionsWithEnemy(BotState state) {
		List<Region> borderRegionsWithEnemy = new ArrayList<>();
		
		for(Region region : getMyBorderRegions(state)) 
			if(region.hasEnemiesAround(state.getMyPlayerName()))
				borderRegionsWithEnemy.add(region);
				
		return borderRegionsWithEnemy;
	}
	
	/**
	 * 
	 * @return Lista cu regiunile ce se afla pe frontiera care
	 * <br /> nu se invecineaza cu inamici ci cu zone neutre
	 */
	public static List<Region> getMyBorderRegionsWithNeutrals(BotState state) {
		/* 
		 * Lista cu regiunle de pe frontiera care nu se invecineaza
		 * e obtinuta din lista tuturor regiunilor de pe frontiera
		 * mai putin regiunile care se invecineaza cu inamici  
		*/
		
		List<Region> regions = getMyBorderRegions(state);
		regions.removeAll(getMyBorderRegionsWithEnemy(state));
		
		return regions;
	}
	
	/**
	 * 
	 * @param state Starea curenta a jocului
	 * @return Lista cu super-regiunile ocupate de mine
	 */
	public static List<SuperRegion> getMySuperRegions(BotState state) {
		List<SuperRegion> mySuperRegions = new ArrayList<>();
		
		for(SuperRegion superRegion : state.getVisibleMap().getSuperRegions())
			if(superRegion.ownedByPlayer(state.getMyPlayerName()))
				mySuperRegions.add(superRegion);
				
		return mySuperRegions;
	}
	
	/**
	 * 
	 * @param state Starea curenta a jocului
	 * @param superRegion Regiunea ce trebuie analizata
	 * @return True daca toata super-regiunea e ocupata de mine, false daca nu
	 */
	public static boolean isSuperRegionMine(BotState state, SuperRegion superRegion) {
		for(Region region : superRegion.getSubRegions())
			if(!region.ownedByPlayer(state.getMyPlayerName()))
				return false;
		
		return true;
	}
	
	/**
	 * 
	 * @param state Starea curenta a jocului
	 * @param superRegion Regiunea ce trebuie analizata
	 * @return Procentul de ocupare al super-regiunii
	 * <br /> <i>0.0f - Super-regiune ocupata 0%</i>
	 * <br /> <i>0.5f - Super-regiune ocupata 50%</i>
	 * <br /> <i>1.0f - Super-regiune ocupata 100%</i>
	 */
	public static double getSuperRegionOccupationPercentage(BotState state, SuperRegion superRegion) {
		int superRegionSize = superRegion.getSubRegions().size();
		int occupiedRegions = 0;
		
		for(Region region : superRegion.getSubRegions()) 
			if(region.ownedByPlayer(state.getMyPlayerName()))
				occupiedRegions++;
		
		return ((double)(occupiedRegions / superRegionSize));
	}
	
}
