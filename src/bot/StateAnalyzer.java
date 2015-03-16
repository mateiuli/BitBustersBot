package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import map.Region;
import map.SuperRegion;

/**
 * This tool analyzes the current map and builds information
 * that is used later on the pipeline by the upcoming moves creator 
 * @author iulian
 *
 */

public class StateAnalyzer {
	
	// Starea curenta a jocului
	BotState state = null;
	
	// Lista cu toate regiunile mele
	private List<Region> myRegions = null;
	
	// Lista cu teritoriie inamicului pe care le putem vedea
	private List<Region> enemyRegions = null;
	
	// Lista cu toate regiunile mele de pe frontiera
	private List<Region> myBorderRegions = null;
	
	// Lista cu regiunile mele centrale
	private List<Region> myCentralRegions = null;
	
	// Lista cu regiunile mele de pe bordura ce se invecineaza cu inamicul
	private List<Region> myBorderRegionsWithEnemy = null;
	
	// Lista cu regiunile mele de pe bordura ce se invecineaza numai cu zone neutre
	private List<Region> myBorderRegionsWithNeutrals = null;
	
	public StateAnalyzer(BotState state) {
		this.state = state;
		analizeSituation();
	}
	
	
	/**
	 * Gaseste toate cazurile de liste de regiuni
	 */
	public void analizeSituation() {
		// Ordinea e importanta deoarece unele determinari
		// Se bazeaza pe altele facute anterior
		computeMyRegions();
		computeEnemyRegions();
		computeMyCentralRegions();
		computeMyBorderRegions();
		computeMyBorderRegionsWithEnemy();
		computeMyBorderRegionsWithNeutrals();		
	}
	
	/**
	 * Gaseste regiunle detinute de mine
	 */
	private void computeMyRegions() {
		myRegions = new ArrayList<>();
		
		for(Region region : state.getVisibleMap().regions) 
			if(region.ownedByPlayer(state.getMyPlayerName()))
				myRegions.add(region);
	}
	
	/**
	 * Gaseste regiunle detinute de inamic
	 */
	private void computeEnemyRegions() {
		enemyRegions = new ArrayList<>();
		
		for(Region region : state.getVisibleMap().regions)
			if(region.ownedByPlayer(state.getOpponentPlayerName()))
				enemyRegions.add(region);
	}
	
	/**
	 * Gaseste toate regiunle mele de pe frontiera
	 * (inclusiv cele ce se invecineaza cu inamic sau cu zone neutre)
	 */
	private void computeMyBorderRegions() {
		myBorderRegions = new ArrayList<>();
		
		for(Region region : myRegions)
			if(region.isOnBorder(state))
				myBorderRegions.add(region);
	}
	
	/**
	 * Gaseste toate regiunile mele centrale
	 */
	private void computeMyCentralRegions() {
		myCentralRegions = new ArrayList<>();
		
		for(Region region : myRegions) 
			if(!region.isOnBorder(state))
				myCentralRegions.add(region);
	}
	
	/**
	 * Gaseste toate regiunile mele de pe bordura ce se invecineaza cu inamicul
	 */
	private void computeMyBorderRegionsWithEnemy() {
		myBorderRegionsWithEnemy = new ArrayList<>();
		
		for(Region region : myBorderRegions) 
			if(region.hasEnemiesAround(state))
				myBorderRegionsWithEnemy.add(region);
	}
	
	/**
	 * Gaseste toate regiunile mele de pe bordura ce se invecineaza numai cu zone neutre
	 */
	private void computeMyBorderRegionsWithNeutrals() {
		myBorderRegionsWithNeutrals = new ArrayList<>();
		
		for(Region region : myBorderRegions) 
			if(!region.hasEnemiesAround(state))
				myBorderRegionsWithNeutrals.add(region);
	}
		
	/**
	 * 
	 * @return Lista cu regunle detinute de mine
	 */
	public List<Region> getMyRegions() {
		// Daca inca harta nu a fost setata complet
		// atunci returneaza o lista vida ca sa evit NullPointerException
		if(state.getVisibleMap() == null || myRegions == null)
			return new ArrayList<>(); 
		
		return myRegions;
	}
	
	/**
	 * 
	 * @param state Starea curenta a jocului
	 * @return Lista cu teritoriile vizibile ale inamicului
	 */
	public List<Region> getEnemyRegions() {
		if(state.getVisibleMap() == null || enemyRegions == null)
			return new ArrayList<>(); 
		
		return enemyRegions;
	}
	
	/**
	 * 
	 * @return Lista cu toate regiunile mele ce se afla pe frontiera
	 */
	public List<Region> getMyBorderRegions() {
		if(state.getVisibleMap() == null || myBorderRegions == null)
			return new ArrayList<>(); 
		
		return myBorderRegions;
	}
	
	/**
	 * 
	 * @param state Starea curenta a jocului
	 * @return Lista cu regiunile mele centrale - inconjurate
	 * <br /> numai de regiuni tot de ale mele
	 */
	public List<Region> getMyCentralRegions() {
		if(state.getVisibleMap() == null || myCentralRegions == null)
			return new ArrayList<>(); 
		
		return myCentralRegions;
	}
	
	/**
	 * 
	 * @return Lista cu regiunle ce se afla pe 
	 * frontiera si se invecineaza cu inamici
	 */
	public List<Region> getMyBorderRegionsWithEnemy() {
		if(state.getVisibleMap() == null || myBorderRegionsWithEnemy == null)
			return new ArrayList<>();
				
		return myBorderRegionsWithEnemy;
	}
	
	/**
	 * 
	 * @return Lista cu regiunile ce se afla pe frontiera care
	 * <br /> nu se invecineaza cu inamici ci cu zone neutre
	 */
	public List<Region> getMyBorderRegionsWithNeutrals() {		
		if(state.getVisibleMap() == null || myBorderRegionsWithNeutrals == null)
			return new ArrayList<>();
		
		return myBorderRegionsWithNeutrals;
	}
	
	/**
	 * 
	 * @param state Starea curenta a jocului
	 * @return Lista cu super-regiunile ocupate de mine
	 */
	public static List<SuperRegion> getMySuperRegions(BotState state) {
		if(state.getVisibleMap() == null)
			return null;
		
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
