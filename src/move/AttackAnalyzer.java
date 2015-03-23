package move;
import java.util.List;

import map.*;
import move.*;

public class AttackAnalyzer {
	
	// Miscarea ce trebuie analizata
	private AttackTransferMove attackMove;
	
	// Armate inamice distruse in atac
	private int minDefendersDestroyed;
	private int maxDefendersDestroyed;
	
	// Armate d-ale mele distruse in atac
	private int minAttackersDestroyed;
	private int maxAttackersDestroyed;
	
	public AttackAnalyzer(AttackTransferMove attackMove) {
		this.attackMove = attackMove;
		simulateAttack();
	}
	
	/**
	 * Calculeaza numarul minim/maxim de armate proprii/inamice distruse in urma atacului
	 */
	private void simulateAttack() {
		// Armate inamice distruse in atac
		minDefendersDestroyed = getMinDefendersDestroyed(attackMove);
		maxDefendersDestroyed = getMaxDefendersDestroyed(attackMove);
		
		// Armate d-ale mele distruse in atac
		minAttackersDestroyed = getMinAttackersDestroyed(attackMove);
		maxAttackersDestroyed = getMaxAttackersDestroyed(attackMove);
	}
	
	/**
	 * Numarul minim de armate cu care trebuie sa atac ca sa cuceresc e egal
	 * cu 1 + numarul maxim de armate pe care mi-l poate distruge inamicul in aparare
	 * @return Numarul minim de armate cu care trebuie sa atac ca sa cuceresc
	 */
	public int getMinAttackersToConquere() {
		return maxAttackersDestroyed + 1;
	}
	
	public String getDebugInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append("\nAttackers = " + attackMove.getArmies());
		sb.append("\nDefenders = " + attackMove.getToRegion().getArmies());
		sb.append("\nMin attackers = " + getMinAttackersToConquere());
		sb.append("\nminDefendersDestroyed = " + minDefendersDestroyed);
		sb.append("\nmaxDefendersDestroyed = " + maxDefendersDestroyed);
		sb.append("\nminAttackersDestroyed = " + minAttackersDestroyed);
		sb.append("\nmaxAttackersDestroyed = " + maxAttackersDestroyed);
		
		return sb.toString();
	}
	
	
	/**
	 * Determina numarul minim de calareti ce trebuie folositi pentru a ataca regiunea
	 * Daca exista un acest numar minim, atunci functia returneaza True.
	 * Daca nu exista una cest numar inseamna ca regiunea nu poate fi cucerita -> return False
	 * @param attackMove
	 * @return True daca regiunea poate fi cucerita, False daca nu
	 */
	public static boolean canConquereWithMinNumberOfAttackers(AttackTransferMove attackMove) {
		return canConquereWithMinNumberOfAttackers(attackMove, 0);
	}
	
	public static boolean canConquereWithMinNumberOfAttackers(AttackTransferMove attackMove, double error) {
		// Presupun ca as avea nevoie de atata armata
		int possibleArmies = attackMove.getToRegion().getArmies() + 1;
		
		// Numarul maxim de armate cu care pot ataca
		int maxArmies = attackMove.getFromRegion().getArmies() - 1;
		
		// Numarul de armate ale inamicului
		int enemyArmies = attackMove.getToRegion().getArmies();
		
		if(enemyArmies >= maxArmies)
			return false;
		
		// Numarul initial de armate din miscarea de atac
		int initialArmies = attackMove.getArmies();
		
		while(possibleArmies < maxArmies) {
			// Setez numarul de armate posibil
			attackMove.setArmies(possibleArmies);
			
			// Numarul minim de armate distruse cu acest numarul de calareti
			int minDefendersDestroyed = getMinDefendersDestroyed(attackMove);
		
			// Daca cu acest numar de calareti pot distruge mai multi
			// decat sunt deja pe teritoriul inamic
			if(minDefendersDestroyed >= enemyArmies + error * enemyArmies) {
				return true;
			}
				
			possibleArmies++;			
		}

		// Daca am ajuns aici inseamna ca nu am suficienta armata cat sa cuceresc
		attackMove.setArmies(initialArmies);
		return false;
	}
	
	/**
	 * 
	 * @param attackMoves
	 * @return True daca atacurile succesive catre fix aceasi regiune inamica vor avea success, 
	 * False daca nu vor avea success
	 */
	public static boolean canConquereWithSuccessiveAttacks(List<AttackTransferMove> attackMoves) {
		if(attackMoves == null || attackMoves.size() < 1)
			return false;
		
		int totalArmies = 0;
		int enemyArmies = attackMoves.get(0).getToRegion().getArmies();
		
		for(AttackTransferMove attackMove : attackMoves) {
			totalArmies += attackMove.getArmies();
		}
		
		// Calculez cate armate inamice ar distruge toata armata acumulata
		int minDefendersDestroyed = getMinDefendersDestroyed(totalArmies);
		
		System.err.println("Armata totala acumulata = " + totalArmies);
		System.err.println("Armata inamica = " + enemyArmies);
		System.err.println("Armata inamica pe care o pot distruge = " + minDefendersDestroyed);
		
		if(minDefendersDestroyed >= enemyArmies)
			return true;
		
		return false;		
	}
	
	/**
	 * 
	 * @param myRegion
	 * @param fromRegion
	 * @return Numarul minim de inamici distrusi intr-un atac cu toti calaretii
	 */
	private static int getMinDefendersDestroyed(AttackTransferMove attackMove) {
		double myArmies = attackMove.getArmies();
		double defendersDestroyed = myArmies * 0.504d;
		
		return ((int)Math.round(defendersDestroyed));
	}
	
	private static int getMinDefendersDestroyed(int attackers) {
		double defendersDestroyed = (double) attackers * 0.504d;
		
		return ((int)Math.round(defendersDestroyed));
	}
	
	
	/**
	 * 
	 * @param myRegion
	 * @param fromRegion
	 * @return Numarul maxim de inamici distrusi intr-un atac cu toti calaretii
	 */
	private static int getMaxDefendersDestroyed(AttackTransferMove attackMove) {
		double myArmies = attackMove.getArmies();
		double defendersDestroyed = myArmies * 0.664d;
		
		return ((int)Math.round(defendersDestroyed));
	}
	
	/**
	 * 
	 * @param myRegion
	 * @param fromRegion
	 * @return Numarul minim de atacatori (de-ai mei) care vor fi distrusi 
	 * intr-un atac cu toti calaretii
	 */
	private static int getMinAttackersDestroyed(AttackTransferMove attackMove) {
		double enemyArmies = attackMove.getToRegion().getArmies();
		double attackersDestroyed = (enemyArmies * 0.588d);
		
		return ((int)Math.round(attackersDestroyed));
	}
	
	/**
	 * 
	 * @param myRegion
	 * @param fromRegion
	 * @return Numarul minim de atacatori (de-ai mei) care vor fi distrusi 
	 * intr-un atac cu toti calaretii
	 */
	private static int getMaxAttackersDestroyed(AttackTransferMove attackMove) {
		double enemyArmies = attackMove.getToRegion().getArmies();
		double attackersDestroyed = (enemyArmies * 0.748d);
		
		return ((int)Math.round(attackersDestroyed));
	}
}
