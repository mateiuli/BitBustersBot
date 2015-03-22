package strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
		// Vezi ca armatele puse in PlaceArmies nu sunt luate in calcul
		// cand pleaca cu toti calaretii la atac
		// Se mai afla regiuni pe bordura cu o singra neutrals aman2 si nu o ataca niciuna, wtf
		// vezi ca mai ataca si 2 pe 2
		
		/**
		 * Ma extind cu regiunile de pe bordura care au doar zone neutre in jur
		 * si cuceresc acele regiuni care fac parte din aceasi super-regiune
		 */
		//System.err.println("GO FOR BONUS");
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithNeutrals()) {
			// Nu am cu ce sa plec de pe regiunea asta
			if(region.getArmies() <= 1)
				continue;
			
			// Vecinii din aceasi super-regiune
			for(Region superRegionNeighbor : region.getSuperRegionNeighbors()) {
				// nu transfer, doar atac regiuni
				// SA NU MAI ATAC SUPER_REGIUNEA DACA ASTA S_A FACUT ANTERIOR
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
				AttackTransferMove expansionMove = new AttackTransferMove(state.getMyPlayerName(), region, superRegionNeighbor);
				AttackAnalyzer attackAnalyzer = new AttackAnalyzer(expansionMove);
				
				// Daca nu se poate cucerii cu numarul maxim de calareti disponibili
				// atunci nu am ce face si trec mai departe
				if(!attackAnalyzer.canConquere()) {
//					System.err.println("Nu pot cucerii regiunea + " + superRegionNeighbor.getDebugInfo(state));
//					System.err.println("Cu regiunea + " + region.getDebugInfo(state));
//					System.err.print(attackAnalyzer.getDebugInfo());
					continue;
				}
				
				// Regiunea ar putea fi cucerita cu toti calaretii, dar eu totusi vreau
				// sa cuceresc cu un numar minim de calareti
				//expansionMove.setArmies(attackAnalyzer.getMinAttackersToConquere());
				
				state.attackTransferMoves.add(expansionMove);
				region.setArmies(region.getArmies() - expansionMove.getArmies());
			}
			
		}
				
		
//		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithNeutrals()) {
//			// Plasez calaretii doar pe regiunile vecine din aceasi super-regiune
//			for(Region neighbor : region.getNeighbors()) {
//				if(neighbor.getSuperRegion().getId() != region.getSuperRegion().getId())
//					continue;
//								
//				double armiesNeeded = neighbor.getArmies() + 0.4 * neighbor.getArmies();
//				int armies = (int) Math.round(armiesNeeded);
//				
//				if(armiesNeeded < region.getArmies())
//					state.attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), region, neighbor, armies));				
//			}
//		}
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
		//System.err.println("EXPAND");
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithNeutrals()) {
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

				if(!neighbor.isNeutral())
					continue;
				
				if(region.getArmies() <= 1)
					break;
				
				// Daca regiunea pe care o vreau sa o cuceresc are din start mai 
				// multa armata ca mine, nu are sens sa analizez atacul
				if(neighbor.getArmies() >= region.getArmies())
					continue;
				
				// Simulez atacul cu toti calaretii posibil pe care ii pot mobiliza
				AttackTransferMove expansionMove = new AttackTransferMove(state.getMyPlayerName(), region, neighbor);
				AttackAnalyzer attackAnalyzer = new AttackAnalyzer(expansionMove);
				
				// Daca nu se poate cucerii cu numarul maxim de calareti disponibili
				// atunci nu am ce face si trec mai departe
				if(!attackAnalyzer.canConquere()) {
//					System.err.println("Nu pot cucerii regiunea + " + neighbor.getDebugInfo(state));
//					System.err.println("Cu regiunea + " + region.getDebugInfo(state));
//					System.err.print(attackAnalyzer.getDebugInfo());
					continue;
				}
				
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
		//System.err.println("ATTACK");
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
			if(region.getArmies() <= 1)
				continue;
			
			// Plasez calaretii doar pe regiunile vecine din aceasi super-regiune
			for(Region neighbor : region.getNeighbors()) {
				if(region.getArmies() <= 1)
					break;
				
				// Caut regiunea vecina care e detinuta de inamic
				if(!neighbor.isNeutral())
					continue;
				
				// Daca regiunea pe care o vreau sa o cuceresc are din start mai 
				// multa armata ca mine, nu are sens sa analizez atacul
				if(neighbor.getArmies() >= region.getArmies())
					continue;
				
				// Simulez atacul cu toti calaretii posibil pe care ii pot mobiliza
				AttackTransferMove expansionMove = new AttackTransferMove(state.getMyPlayerName(), region, neighbor);
				AttackAnalyzer attackAnalyzer = new AttackAnalyzer(expansionMove);
				
				// Daca nu se poate cucerii cu numarul maxim de calareti disponibili
				// atunci nu am ce face si trec mai departe
				if(!attackAnalyzer.canConquere()) {
//					System.err.println("Nu pot cucerii regiunea + " + neighbor.getDebugInfo(state));
//					System.err.println("Cu regiunea + " + region.getDebugInfo(state));
//					System.err.println(attackAnalyzer.getDebugInfo());
					continue;
				}
				
				// Regiunea ar putea fi cucerita cu toti calaretii, dar eu totusi vreau
				// sa cuceresc cu un numar minim de calareti
				//expansionMove.setArmies(attackAnalyzer.getMinAttackersToConquere());
				
				state.attackTransferMoves.add(expansionMove);	
				region.setArmies(region.getArmies() - expansionMove.getArmies());
			}
		}
	}
}
