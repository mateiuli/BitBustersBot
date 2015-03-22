package strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bot.BotState;
import map.Region;
import move.AttackAnalyzer;
import move.AttackTransferMove;

public class AttacksCreator {
	// Starea curenta a jocului
	private BotState state;
	
	public AttacksCreator(BotState state) {
		this.state = state;
	}
	
	public ArrayList<AttackTransferMove> getAttackMoves() {
		System.err.println("Round number: " + state.getRoundNumber());
		
		computeGoForBonus();
		
		computeExpand();
		
		computeAttackEnemy();
		
		return state.attackTransferMoves;
	}
	
	public void computeGoForBonus() {
		// Se mai afla regiuni pe bordura cu o singra neutrals aman2 si nu o ataca niciuna, wtf
		// vezi ca mai ataca si 2 pe 2
		
		/**
		 * Ma extind cu regiunile de pe bordura care au doar zone neutre in jur
		 * si cuceresc acele regiuni care fac parte din aceasi super-regiune
		 */
		System.err.println("GO FOR BONUS");
		
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithNeutrals()) {
			// Nu am cu ce sa plec de pe regiunea asta
			if(region.getArmies() <= 1)
				continue;
			
			// Vecinii din aceasi super-regiune
			for(Region superRegionNeighbor : region.getSuperRegionNeighbors()) {
				// nu transfer, doar atac regiuni
				
				// Verificare sa nu exista toRegion identic cu superRegionNeighbor in state.attackTransferMoves();
				if(existsAttackTo(superRegionNeighbor))
					continue;				

				if(!superRegionNeighbor.isNeutral())
					continue;
				
				if(region.getArmies() <= 1)
					break;
				
				// Daca regiunea pe care o vreau sa o cuceresc are din start mai 
				// multa armata ca mine, nu are sens sa analizez atacul
				if(superRegionNeighbor.getArmies() >= region.getArmies())
					continue;
				
				// Simulez atacul cu toti calaretii posibil pe care ii pot mobiliza
				int possibleArmies = superRegionNeighbor.getArmies() + 1;
				AttackTransferMove expansionMove = null; // new AttackTransferMove(state.getMyPlayerName(), region, enemy, possibleArmies);
				AttackAnalyzer attackAnalyzer = null; //new AttackAnalyzer(expansionMove);
				
				do {
					expansionMove = new AttackTransferMove(state.getMyPlayerName(), region, superRegionNeighbor, possibleArmies);
					attackAnalyzer = new AttackAnalyzer(expansionMove);
					possibleArmies++;
				} while(!attackAnalyzer.canConquere() && possibleArmies < region.getArmies() - 1);
				
				if(!attackAnalyzer.canConquere())
					continue;
				
				// Regiunea ar putea fi cucerita cu toti calaretii, dar eu totusi vreau
				// sa cuceresc cu un numar minim de calareti
				//expansionMove.setArmies(attackAnalyzer.getMinAttackersToConquere());
				
				state.attackTransferMoves.add(expansionMove);
				region.setArmies(region.getArmies() - expansionMove.getArmies());
			}
			
		}
	}
	
	public void computeExpand() {
		// Vezi ca armatele puse in PlaceArmies nu sunt luate in calcul
		// cand pleaca cu toti calaretii la atac
		// Se mai afla regiuni pe bordura cu o singra neutrals aman2 si nu o ataca niciuna, wtf
		// vezi ca mai ataca si 2 pe 2
		
		/**
		 * Ma extind cu regiunile de pe bordura care au doar zone neutre in jur
		 * si cuceresc acele regiuni care fac parte din aceasi super-regiune
		 */
		System.err.println("EXPAND");
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithNeutralsAndEnemy()) {
			// Nu am cu ce sa plec de pe regiunea asta
			if(region.getArmies() <= 1)
				continue;
			
			// Vecinii din aceasi super-regiune
			for(Region neighbor : region.getNeighbors()) {
				// nu transfer, doar atac regiuni
				// SA NU MAI ATAC SUPER_REGIUNEA DACA ASTA S_A FACUT ANTERIOR
				// Verificare sa nu exista toRegion identic cu superRegionNeighbor in state.attackTransferMoves();
				if(existsAttackTo(neighbor))
					continue;				

				// Doar ma extind. Nu atac inamici.
				if(!neighbor.isNeutral())
					continue;
				
				if(region.getArmies() <= 1)
					break;
				
				// Daca regiunea pe care o vreau sa o cuceresc are din start mai 
				// multa armata ca mine, nu are sens sa analizez atacul
				if(neighbor.getArmies() >= region.getArmies())
					continue;
				
				// Simulez atacul cu toti calaretii posibil pe care ii pot mobiliza
				int possibleArmies = neighbor.getArmies() + 1;
				AttackTransferMove expansionMove = null; // new AttackTransferMove(state.getMyPlayerName(), region, enemy, possibleArmies);
				AttackAnalyzer attackAnalyzer = null; //new AttackAnalyzer(expansionMove);
				
				do {
					expansionMove = new AttackTransferMove(state.getMyPlayerName(), region, neighbor, possibleArmies);
					attackAnalyzer = new AttackAnalyzer(expansionMove);
					possibleArmies++;
				} while(!attackAnalyzer.canConquere() && possibleArmies < region.getArmies() - 1);
				
				if(!attackAnalyzer.canConquere())
					continue;
				
				// Regiunea ar putea fi cucerita cu toti calaretii, dar eu totusi vreau
				// sa cuceresc cu un numar minim de calareti
				//expansionMove.setArmies(attackAnalyzer.getMinAttackersToConquere());
				
				state.attackTransferMoves.add(expansionMove);
				region.setArmies(region.getArmies() - expansionMove.getArmies());
			}
			
		}
	}
	
	/**
	 * 
	 * @param toRegion
	 * @return True daca in lista de atacuri mai exista adaugat un atac
	 * catre regiunea toRegion
	 */
	public boolean existsAttackTo(Region toRegion) {
		for(AttackTransferMove move : state.attackTransferMoves)
			if(move.getToRegion().getId() == toRegion.getId())
				return true;
		
		return false;
	}
	
	public void computeAttackEnemy() {
		System.err.println("--------------------------------------");
		System.err.println("ATTACK runda: " + state.getRoundNumber());
		
		Map<Region, ArrayList<Region>> toEnemy = new HashMap<>();
		
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
			System.err.println("--------------------------------------");
			System.err.println("Atac de pe regiune: " + region.getDebugInfo(state));
			
			if(region.getArmies() <= 1)
				continue;
			
			System.err.println("Armata mai mare ca 1");
			
			// Plasez calaretii doar pe regiunile vecine din aceasi super-regiune
			for(Region enemy : region.getEnemiesAround(state.getOpponentPlayerName())) {
				System.err.println("Inamicul: " + enemy.getDebugInfo(state));
				
				if(region.getArmies() <= 1) {
					System.err.println("Nu am suficienta armata - by region " + region.getId());
					break;
				}
				
				System.err.println("Am armata > 1");
				
				// Daca regiunea pe care o vreau sa o cuceresc are din start mai 
				// multa armata ca mine, nu are sens sa analizez atacul
				if(enemy.getArmies() >= region.getArmies())
					continue;
				
				System.err.println("Inamicul nu are mai mult ca mine");
				
				
				// Simulez atacul cu toti calaretii posibil pe care ii pot mobiliza
				int possibleArmies = enemy.getArmies() + 1;
				AttackTransferMove expansionMove = null; // new AttackTransferMove(state.getMyPlayerName(), region, enemy, possibleArmies);
				AttackAnalyzer attackAnalyzer = null; //new AttackAnalyzer(expansionMove);
				
				do {
					expansionMove = new AttackTransferMove(state.getMyPlayerName(), region, enemy, possibleArmies);
					attackAnalyzer = new AttackAnalyzer(expansionMove);
					possibleArmies++;
				} while(!attackAnalyzer.canConquere() && possibleArmies < region.getArmies() - 1);
				
				if(!attackAnalyzer.canConquere()) {
					if(!toEnemy.containsKey(enemy))
						toEnemy.put(enemy, new ArrayList<>());
					
					toEnemy.get(enemy).add(region);					
					continue;
				}
				
				// Daca nu se poate cucerii cu numarul maxim de calareti disponibili
				// atunci nu am ce face si trec mai departe
//				if(!attackAnalyzer.canConquere()) {
//					System.err.println("Nu pot cucerii regiunea + " + enemy.getDebugInfo(state));
//					System.err.println("Cu regiunea + " + region.getDebugInfo(state));
//					System.err.println(attackAnalyzer.getDebugInfo());
//					continue;
//				}
//				
				System.err.println("Il pot cucerii");
				// Regiunea ar putea fi cucerita cu toti calaretii, dar eu totusi vreau
				// sa cuceresc cu un numar minim de calareti
				//expansionMove.setArmies(attackAnalyzer.getMinAttackersToConquere());
				
				state.attackTransferMoves.add(expansionMove);	
				region.setArmies(region.getArmies() - expansionMove.getArmies());
			}
		}
		
		AttackTransferMove expansionMove = null; // new AttackTransferMove(state.getMyPlayerName(), region, enemy, possibleArmies);
		AttackAnalyzer attackAnalyzer = null; //new AttackAnalyzer(expansionMove);
		
		for(Entry<Region, ArrayList<Region>> pair: toEnemy.entrySet()) {
			int sum = 0;
			for(Region attacker : pair.getValue())
				sum += attacker.getArmies() - 1;
			
			if(pair.getValue().size() < 1)
				continue;
			
			expansionMove = new AttackTransferMove(state.getMyPlayerName(), pair.getValue().get(0), pair.getKey(), sum);
			attackAnalyzer = new AttackAnalyzer(expansionMove);
			
			if(attackAnalyzer.canConquere()) {
				for(Region attacker : pair.getValue()) {
					expansionMove = new AttackTransferMove(state.getMyPlayerName(), attacker, pair.getKey(), attacker.getArmies() - 1);
					state.attackTransferMoves.add(expansionMove);
				}				
			}
		}
	}
}
