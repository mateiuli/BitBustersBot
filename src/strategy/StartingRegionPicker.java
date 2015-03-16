package strategy;

import java.util.ArrayList;
import java.util.List;

import bot.BotState;
import map.Region;

public class StartingRegionPicker {
	private BotState state;
	
	public StartingRegionPicker(BotState state) {
		this.state = state;
	}
	
	/**
	 * 
	 * @return Returneaza regiunea optima de start 
	 */
	public Region getStartingRegion() {
		// Calculeaza numarul de wastelands pentru fiecare super-regiune
		setSuperRegionsWastelands(state);
		
		// Mai intai caut regiunile cu numarul minim de wastelands
		List<Region> regions = getRegionsWithMinNoOfWastelands(state.getPickableStartingRegions());
		
		// Din acele regiuni le caut pe acelea cu numarul maxim de bonus
		regions = getRegionsWithMaxNoOfArmiesReward(regions);
		
		// Iar din acele regiuni le caut pe acelea care sunt cat mai putine pe super-regiune
		regions = getRegionsWithMinNoOfChildren(regions);		
		
		// Acum am obtinut niste regiuni optime, aleg random
		// De cele mai multe ori regions va avea la final o singura regiune
		double rand = Math.random();
		int regionNo = (int) rand * regions.size();
		
		return regions.get(regionNo);
	}
	
	/**
	 *
	 * @return Returneaza lista cu regiunile ce contin numarul minim de wastelands
	 */
	private List<Region> getRegionsWithMinNoOfWastelands(List<Region> regions) {
		// Numarul minim de wastelanduri
		int minNumberOfWastelands = Integer.MAX_VALUE;
		
		// Lista cu regiunile din care fac parte posibilele regiuni de start
		ArrayList<Region> minRegions = new ArrayList<>();
			
		for(Region region : regions) {
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
		
		return minRegions;
	}
	
	/**
	 * 
	 * @param regions
	 * @return Returneaza lista de regiuni ce au numarul maxim de armate pe bonus
	 */
	private List<Region> getRegionsWithMaxNoOfArmiesReward(List<Region> regions) {
		// NUMAR MAXIM DE BONUS
		ArrayList<Region> optimalRegions = new ArrayList<>();
		int maxBonus = Integer.MIN_VALUE;
		
		for(Region region : regions) {
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
		
		return optimalRegions;
	}
	
	/**
	 * 
	 * @param regions
	 * @return Returneaza regiunile ale caror super-regiuni au cele mai putine regiuni
	 */
	private List<Region> getRegionsWithMinNoOfChildren(List<Region> regions) {
		ArrayList<Region> minRegions = new ArrayList<>();
		int minChildRegions = Integer.MAX_VALUE;
		
		// Parcurg fiecare regiune din lista cu regiuni posibile de start
		for(Region region : regions) {
			if(region.getSuperRegion().getSubRegions().size() < minChildRegions) {
				minRegions.clear();
				minRegions.add(region);
				minChildRegions = region.getSuperRegion().getSubRegions().size();				
			}
			else if(region.getSuperRegion().getSubRegions().size() == minChildRegions) {
				minRegions.add(region);
			}
		}
		
		return minRegions;
	}
	
	/**
	 * Seteaza pentru fiecare super-regiune numarul de wasteland-uri pe care le contine
	 * @param state
	 */
	public void setSuperRegionsWastelands(BotState state) {
		for(Region wasteland : state.getWasteLands()) {
			wasteland.getSuperRegion().incrementNumberOfWastelands();
		}
	}
}
