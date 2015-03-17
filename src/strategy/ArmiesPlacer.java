package strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bot.BotState;
import map.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import algorithm.DistanceCalculator;

public class ArmiesPlacer {
	// Starea curenta a jocului
	private BotState state;
	
	// Numarul de armate disponibile pentru plasat
	private int armiesLeft;
	
	// Lista cu actiunile de transfer
	private ArrayList<PlaceArmiesMove> placeArmiesMoves; 
	
	public ArmiesPlacer(BotState state) {
		this.state = state;
		this.armiesLeft = state.getStartingArmies();
		this.placeArmiesMoves = new ArrayList<>();
	}
	
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves() {
		// Golesc lista cu mutarile anterioare - urmeaza miscari noi
		state.attackTransferMoves.clear();
		
		// Fa transferurile de calareti de pe zonele centrale pe borduri
		computeTransferMoves();
		
		// Sistemul de aparare
		placeArmiesToDefend();
		
		// Sistemul de explorare
		placeArmiesToExplore();
						
		return placeArmiesMoves;
	}
	
	
	/**
	 * Plaseaza calareti pe zonele cu grad ridicat de a fi cucerite de inamic
	 */
	public void placeArmiesToDefend() {
		if(armiesLeft <= 0)
			return;
		
		// Sortam regiunile de pe bordura in functie de raportul dintre 
		// armata de pe regiune si suma armatelor inaimcilor din jur
		Collections.sort(state.getStateAnalyzer().getMyBorderRegionsWithEnemy(), new Comparator<Region>() {
			@Override
			public int compare(Region o1, Region o2) {
				Double ratio1 = o1.getMyArmyEnemyArmyRatio(state);
				Double ratio2 = o2.getMyArmyEnemyArmyRatio(state);
				
				return ratio1.compareTo(ratio2);
			}			
		});
				 
		for(Region borderRegion : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
			if(borderRegion.getMyArmyEnemyArmyRatio(state) >= 1.d) 
				continue;
			
			// Minimul de calarati ce trebuie plasati pentru aparare
			double var = 0.7 * (double) borderRegion.getNoOfEnemyArmiesAround(state.getOpponentPlayerName());
			int armyDiff = (int) (Math.round(var) - borderRegion.getArmiesWithUpcomingArmies());
			
			if(armyDiff <= armiesLeft) {
				placeArmiesMoves.add(new PlaceArmiesMove(state.getMyPlayerName(), borderRegion, armyDiff));
				armiesLeft -= armyDiff;
			}
			else {
				placeArmiesMoves.add(new PlaceArmiesMove(state.getMyPlayerName(), borderRegion, armiesLeft));
				armiesLeft = 0;
			}
			
			if(armiesLeft <= 0)
				break;			
		}
	}
	
	/**
	 * Daca au mai ramas calareti de plasat ii pun sa se extinda
	 * Prioriati: sa ia bonus-ul (cucereste superregiunea);
	 */
	public void placeArmiesToExplore() {
		System.out.println("\nArmies to be placed: " + armiesLeft + "\n");
		if(armiesLeft <= 0)
			return;
		
		// Regiunile cu numarul minim de calareti au prioritate
		// Se asigura o explorare uniforma astfel
		Collections.sort(state.getStateAnalyzer().getMyBorderRegionsWithNeutrals(), new Comparator<Region>() {
			public int compare(Region o1, Region o2) {
				return o2.getArmies() - o1.getArmies();
			}			
		});
		
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithNeutrals()) {
			// TODO: From superregion?
			int armiesToPlace = region.getNoOfNeutralArmies();
			
			if(armiesToPlace > armiesLeft) 
				armiesToPlace = armiesLeft;
			
			placeArmiesMoves.add(new PlaceArmiesMove(state.getMyPlayerName(), region, armiesToPlace));
			armiesLeft -= armiesToPlace;
			
			if(armiesLeft == 0)
				break;
		}
	}
	
	
	/**
	 * Creaza mutarile de transfer de calareti de pe regiuni centrale pe frontiera
	 */
	public void computeTransferMoves() {
		for(Region centralRegion : state.getStateAnalyzer().getMyCentralRegions()) {
			if(centralRegion.getArmies() < 2)
				continue;
			
			// Regiunea imediat urmatoare din drumul cel mai scurt catre o bordura
			Region nextRegion = DistanceCalculator.nextRegionToBorder(centralRegion, state.getStateAnalyzer().getMyBorderRegions());
			
			// Adaugam miscarea de transfer
			state.attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), centralRegion, nextRegion, centralRegion.getArmies() - 1));
			
			if(nextRegion.isOnBorder(state)) {	
				// Avertizeaza regiunea urmatoare ca vor veni nu numar de calareti pe regiunea asta
				nextRegion.addUpcomingArmiesOnTransfer(centralRegion.getArmies() - 1);
			} // Verifica sa se faca clear pe upcomingArmies !!	
			
			// Resorteaza vecinii dupa transfer
			// TODO: Complexitatea e O(N^3 * LogN) cu sortarea aici!
			state.getStateAnalyzer().sortMyRegionsNeighbors();
		}
	}
	
}
