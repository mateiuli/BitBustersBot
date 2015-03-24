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
		
		// Mai transfera
		// computeFinalTransferMoves();
		
		// Update calareti regiune pentru atac
		for(PlaceArmiesMove move : this.placeArmiesMoves) {
			move.getRegion().addArmies(move.getArmies());
		}
						
		return placeArmiesMoves;
	}
	
	
	/**
	 * Plaseaza calareti pe zonele cu grad ridicat de a fi cucerite de inamic
	 */
	public void placeArmiesToDefend() {
		//System.err.println("Place armies to DEFEND");
		//System.err.println("armiesLeft = " + armiesLeft);
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
		
		//System.err.println("My border regions with enemy");
		for(Region borderRegion : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
			//System.err.println(borderRegion.getDebugInfo(state));
		}
				
		for(Region borderRegion : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
			//System.err.println("armiesLeft = " + armiesLeft);
			if(armiesLeft <= 0)
				break;
			
			if(borderRegion.getMyArmyEnemyArmyRatio(state) > 1.d) 
				continue;
			
			
			// Minimul de calarati ce trebuie plasati pentru aparare
			double var = 0.8 * (double) borderRegion.getNoOfEnemyArmiesAround(state.getOpponentPlayerName());
			int armyDiff = (int) (Math.round(var) - borderRegion.getArmiesWithUpcomingArmies());
			
			//System.err.println("armyDiff = " + armyDiff);
			
			if(armyDiff > 0 && armyDiff <= armiesLeft) {
				placeArmiesMoves.add(new PlaceArmiesMove(state.getMyPlayerName(), borderRegion, armyDiff));
				armiesLeft -= armyDiff;
			}
			else {
				placeArmiesMoves.add(new PlaceArmiesMove(state.getMyPlayerName(), borderRegion, armiesLeft));
				armiesLeft = 0;
			}			
		}
	}
	
	/**
	 * Daca au mai ramas calareti de plasat ii pun sa se extinda
	 * Prioriati: sa ia bonus-ul (cucereste superregiunea);
	 */
	public void placeArmiesToExplore() {
		// TODO: sa nu ma extind cu acele regiuni care au inamic langa?
		if(armiesLeft <= 0)
			return;
		
		// Cele cu mai multi vecini in aceasu super-regiune au prioritate
		Collections.sort(state.getStateAnalyzer().getMyBorderRegionsWithNeutrals(), new Comparator<Region>() {
			public int compare(Region o1, Region o2) {
				return o1.getSuperRegionNeighbors().size() - o2.getSuperRegionNeighbors().size();
			}			
		});
		
		// Pun toate armatele disponibile
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithNeutrals()) {
			//System.err.println("armiesLeft = " + armiesLeft);
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
	
	public boolean existsTransferTo(Region toRegion) {
		for(AttackTransferMove move : state.attackTransferMoves)
			if(move.getToRegion().getId() == toRegion.getId())
				return true;
		
		return false;
	}
	
	public boolean existsTransferFrom(Region fromRegion) {
		for(AttackTransferMove move : state.attackTransferMoves)
			if(move.getFromRegion().getId() == fromRegion.getId())
				return true;
		
		return false;
	}
	
	/**
	 * Creaza mutarile de transfer de calareti de pe regiuni centrale pe frontiera
	 */
	public void computeTransferMoves() {
		// Transfer de pe regiuni centrale pe regiuni de bordura
		for(Region centralRegion : state.getStateAnalyzer().getMyCentralRegions()) {
			if(centralRegion.getArmies() < 2)
				continue;
			
			// TODO: Complexitatea e O(N^3 * LogN) cu sortarea aici!
			state.getStateAnalyzer().sortMyRegionsNeighbors();
			
			// Regiunea imediat urmatoare din drumul cel mai scurt catre o bordura
			Region nextRegion = DistanceCalculator.nextRegionToBorder(centralRegion, state.getStateAnalyzer().getMyBorderRegions());
			
			// Adaugam miscarea de transfer
			state.attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), centralRegion, nextRegion, centralRegion.getArmies() - 1));
			
			if(nextRegion.isOnBorder(state)) {	
				// Avertizeaza regiunea urmatoare ca vor veni nu numar de calareti pe regiunea asta
				nextRegion.addUpcomingArmiesOnTransfer(centralRegion.getArmies() - 1);
			}
		}
	}	
}
