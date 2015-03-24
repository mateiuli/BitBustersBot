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
import map.SuperRegion;
import move.AttackAnalyzer;
import move.AttackTransferMove;

public class AttacksCreator {
	// Starea curenta a jocului
	private BotState state;
	
	// Ponderea deschiderii fata de bonus
	private static final double magicPonder = 0.6d;
	
	public AttacksCreator(BotState state) {
		this.state = state;
	}
	
	public ArrayList<AttackTransferMove> getAttackMoves() {
		System.err.println("Round number: " + state.getRoundNumber());
		
		computeGoForBonus();
		
		computeExpand();
		// vezi ca daca e de castigat repede nu mai are prioritate explorarea ci atacul!
		// adica daca mai e o regiune care o pot lua si un inamic langa pe care il pot cucerii, ia inamicul
		computeAttackEnemy();
		
		return state.attackTransferMoves;
	}
	
	public void computeGoForBonus() {
		// Daca mai e o singura regiune vecina ---> ia-o cu toti! nu mai calcula minimul
		// Se mai afla regiuni pe bordura cu o singra neutrals aman2 si nu o ataca niciuna, wtf
		// vezi ca mai ataca si 2 pe 2
		
		/**
		 * da, ar trebui sa aiba prioritate regiunile din super-regiunile cu bonus maxim
		 * si pe care le poate cucerii in numar minim de pasi
		 */
		
		/**
		 * Ma extind cu regiunile de pe bordura care au doar zone neutre in jur
		 * si cuceresc acele regiuni care fac parte din aceasi super-regiune
		 */
		System.err.println("GO FOR BONUS");
		
		for(Region region : state.getStateAnalyzer().getMyBorderRegions()) { // getMyBorderRegionsWithNeutrals()
			System.err.println("De pe regiunea: " + region.getId());
			// Nu am cu ce sa plec de pe regiunea asta
			if(region.getArmies() <= 1)
				continue;
			
			// Vecinii din aceasi super-regiune
			for(Region superRegionNeighbor : region.getSuperRegionNeighbors()) {
				System.err.println("pe regiunea: " + superRegionNeighbor.getId());
				// nu transfer, doar atac regiuni
				
				// Verificare sa nu exista toRegion identic cu superRegionNeighbor in state.attackTransferMoves();
				if(existsAttackTo(superRegionNeighbor)) {
					System.err.println("Atacul exista");
					continue;
				}
									
				if(!superRegionNeighbor.isNeutral()) {
					System.err.println("Nu e neutra");
					continue;
				}
				
				if(region.getArmies() <= 1) {
					System.err.println("Nu am armata");
					break;
				}
				
				// Daca regiunea pe care o vreau sa o cuceresc are din start mai 
				// multa armata ca mine, nu are sens sa analizez atacul
				if(superRegionNeighbor.getArmies() >= region.getArmies()) {
					System.err.println("Nu am armata suficienta");
					continue;
				}
				
				// Generez miscarea de atac
				AttackTransferMove attackMove = new AttackTransferMove(state.getMyPlayerName(), region, superRegionNeighbor);
				
				// Verifica daca se poate cuceri cu un numar minim de calareti
				// Daca da, atunci cucereste cu ei
				if(AttackAnalyzer.canConquereWithMinNumberOfAttackers(attackMove)) {
					System.err.println("Il pot cucerii cu numar minim");
					
					// Daca eu nu am inamic langa, dar regiunea pe care ma voi extinde are - ma mut cu toata armata
					if(!region.hasEnemiesAround(state) && superRegionNeighbor.hasEnemiesAround(state)) {
						System.err.println("Are inamic langa, ma duc cu toti");
						attackMove.setArmiesToMax();
					}
					
					// Daca am inamic langa mine, si ma poate cucerii dupa ce atac regiunea pentru bonus
					// atunci nu mai atac. Mai bine o las pe aparare.
					if(region.hasEnemiesAround(state)) {
						System.err.println("Nu iau regiunea de bonus pentru ca risc sa fiu atacat.");
						int enemyArmies = region.getNoOfEnemyArmiesAround(state.getOpponentPlayerName());

						// Armata cu care voi ramane daca voi cuceri regiunea vecina
						int armiesLeft = region.getArmies() - attackMove.getArmies();

						// Verific sa vad daca pot fi cucerit de toate regiunile inamicului intr-un atac succesiv
						if(AttackAnalyzer.canIBeConquered(armiesLeft, enemyArmies))
							continue;
					}					
						
					state.attackTransferMoves.add(attackMove);
					region.setArmies(region.getArmies() - attackMove.getArmies());
				}
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
				
		for(Region region : state.getStateAnalyzer().getMyBorderRegions()) {
			System.err.println("De pe regiunea: " + region.getId());
			// Nu am cu ce sa plec de pe regiunea asta
			if(region.getArmies() <= 1)
				continue;
			
			// Ma extind cucerind mai intai acele super-regiuni care au bonusul maxim!
			// magicValue = bonus * (1 - R) + deschidere * R, R = 0.6
			// Sortare dupa valoarea magica
			
			// Deschiderea regiunii fata re super-regiune
			// latime de drum
			Map<SuperRegion, Integer> pathWidth = new HashMap<>();
			for(Region neighbor : region.getNeighbors()) {
				// Super-regiunea din care face parte vecinul
				SuperRegion superRegion = neighbor.getSuperRegion();
				
				// Daca nu am mai pus aceasta super-regiune, pune-o cu deschidere 1
				if(!pathWidth.containsKey(superRegion)) {
					pathWidth.put(superRegion, 1);
					continue;	
				}
				
				// Daca a mai fost gasita aceasta regiune, incrementeaza numarul de deschideri
				pathWidth.put(superRegion, pathWidth.get(superRegion) + 1);
			}
			
			// Acum am pentru aceasta regiune super-regiunile in care ma pot duce
			// Dar si prin cate regiuni ma pot duce deodata catre o super-regiune
			
			// Sortez descrescator dupa valoarea magica
			Collections.sort(region.getNeighbors(), new Comparator<Region>() {
				public int compare(Region o1, Region o2) {
					Double magicValue1 = calculateMagicValue(o1, pathWidth.get(o1.getSuperRegion()));
					Double magicValue2 = calculateMagicValue(o2, pathWidth.get(o2.getSuperRegion()));
					
					return magicValue2.compareTo(magicValue1);
				}				
			});
			
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
				
				System.err.println("pe regiunea: " + neighbor.getId());
				System.err.println("-> are magicValue = " + calculateMagicValue(neighbor, pathWidth.get(neighbor.getSuperRegion())));
				
				// Generez miscarea de atac
				AttackTransferMove attackMove = new AttackTransferMove(state.getMyPlayerName(), region, neighbor);
				
				// Verifica daca se poate cuceri cu un numar minim de calareti
				// Daca da, atunci cucereste cu ei
				if(AttackAnalyzer.canConquereWithMinNumberOfAttackers(attackMove)) {
					// Daca am inamic langa mine, si ma poate cucerii dupa ce atac regiunea pentru extindere
					// atunci nu ma mai extind. Mai bine o las pe aparare.
					if(region.hasEnemiesAround(state)) {
						System.err.println("Nu iau regiunea de extindere pentru ca risc sa fiu atacat.");
						int enemyArmies = region.getNoOfEnemyArmiesAround(state.getOpponentPlayerName());

						// Armata cu care voi ramane daca voi cuceri regiunea vecina
						int armiesLeft = region.getArmies() - attackMove.getArmies();

						// Verific sa vad daca pot fi cucerit de toate regiunile inamicului intr-un atac succesiv
						if(AttackAnalyzer.canIBeConquered(armiesLeft, enemyArmies))
							continue;
					}		
					
					state.attackTransferMoves.add(attackMove);
					region.setArmies(region.getArmies() - attackMove.getArmies());
				}
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
		// # problema atac cumulat -> muta toata armata!!!
		// # nu merge atacul cumulat
		System.err.println("Attack Enemy");
		
		// Atac (daca se poate) mai intai cu regiunile care au un numar minim de armate inamice in jur
		Collections.sort(state.getStateAnalyzer().getMyBorderRegionsWithEnemy(), new Comparator<Region>() {
			@Override
			public int compare(Region o1, Region o2) {
				return o1.getNoOfEnemyArmiesAround(state.getOpponentPlayerName()) - o2.getNoOfEnemyArmiesAround(state.getOpponentPlayerName());
			}
		});
		
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
//			System.err.println("--------------------------------------");
//			System.err.println("Atac de pe regiune: " + region.getDebugInfo(state));
			
			if(region.getArmies() <= 1)
				continue;
			
			// Regiunile inamice din jur
			List<Region> enemiesAround = region.getEnemiesAround(state.getOpponentPlayerName());
			
			// Atac mai intai regiunile inamice cu numar minim de armate
			Collections.sort(enemiesAround, new Comparator<Region>() {
				@Override
				public int compare(Region o1, Region o2) {
					return o1.getNoOfEnemyArmiesAround(state.getOpponentPlayerName()) - o2.getNoOfEnemyArmiesAround(state.getOpponentPlayerName());
				}
			});
			
			for(Region enemy : enemiesAround) {
				// System.err.println("Inamicul: " + enemy.getDebugInfo(state));
				
				if(region.getArmies() <= 1) {
					// System.err.println("Nu am suficienta armata - by region " + region.getId());
					break;
				}
				
				// System.err.println("Am armata > 1");
				
				// Daca regiunea pe care o vreau sa o cuceresc are din start mai 
				// multa armata ca mine, nu are sens sa analizez atacul
				if(enemy.getArmies() >= region.getArmies())
					continue;
				
				//System.err.println("Inamicul nu are mai mult ca mine");
				
				// Generez miscarea de atac
				AttackTransferMove attackMove = new AttackTransferMove(state.getMyPlayerName(), region, enemy);
				
				// Verifica daca se poate cuceri cu un numar minim de calareti
				// Daca da, atunci cucereste cu ei
				if(AttackAnalyzer.canConquereWithMinNumberOfAttackers(attackMove, 0.4)) {
					// Daca eu am un singur inamic langa mine, ma duc cu toata armata
					if(enemiesAround.size() == 1)
						attackMove.setArmiesToMax();
					
					state.attackTransferMoves.add(attackMove);
					region.setArmies(region.getArmies() - attackMove.getArmies());
				}
			}
		}
		
		// Incerc sa vad daca in urma unor atacuri succesive asupra regiunilor inamice
		// voi reusi sa cuceresc regiunea.
		Map<Region, ArrayList<Region>> successiveAttack = new HashMap<>();
		
		for(Region enemy : state.getStateAnalyzer().getEnemyRegions()) {
			// Atac succesiv doar inamicii care sunt singuri, adica nu au ajutoare
			if(!enemy.isAlone())
				continue;
			
			if(!successiveAttack.containsKey(enemy))
				successiveAttack.put(enemy, new ArrayList<>());
			
			for(Region region : enemy.getNeighbors()) {
				// Atac numai cu regiunile mele care se invecineaza cu inamicul
				if(!region.ownedByPlayer(state.getMyPlayerName()))
					continue;
				
				// Atac numai daca am cu ce
				if(region.getArmies() < 2)
					continue;
				
				// Adaug regiunea la succesiunea de atacuri catre acest inamic
				successiveAttack.get(enemy).add(region);
			}
		}
		
		System.err.println("Atack acumulat -> sucesiv");
		// Atacurile directe nereusite catre toate regiunile inamice; se poate finaliza 
		// cu success daca se acumuleaza suficienta armata pentru atacuri succesive
		for(Entry<Region, ArrayList<Region>> pair: successiveAttack.entrySet()) {	
			if(pair.getValue().size() < 2)
				continue;
			
			// Atacurile succesive ce pot avea loc asupra acestui inamic
			List<AttackTransferMove> attackMoves = new ArrayList<>();
			
			for(Region attacker : pair.getValue()) {
				attackMoves.add(new AttackTransferMove(state.getMyPlayerName(), attacker, pair.getKey()));
			}
		
			System.err.println("Atac pe " + pair.getKey().getId() + " cu: ");
			for(Region attacker : pair.getValue()) {
				System.err.print(attacker.getId() + ", ");
			}
			
			// Verific sa vad daca atacurile succesive vor fi cu success
			if(AttackAnalyzer.canConquereWithSuccessiveAttacks(attackMoves)) {
				System.err.println("Atacul succesiv va avea loc");
				state.attackTransferMoves.addAll(attackMoves);
			}
		}
	}
	
	/**
	 * 
	 * @param region
	 * @param pathWidth
	 * @return
	 */
	public static double calculateMagicValue(Region region, int pathWidth) {
		return (region.getSuperRegion().getArmiesReward() * (1 - magicPonder) + pathWidth * magicPonder);
	}
}
