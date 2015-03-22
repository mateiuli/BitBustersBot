package move;
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
	 * 
	 * @param attackMove
	 * @return True daca am sanse sa cuceresc teritoriul inamicului, False daca nu
	 */
	public boolean canConquere() {
		// Armatele cu care se apara inamicul
		int enemyArmies = attackMove.getToRegion().getArmies();
		
		// Daca numarul maxim de armate inamice care pot fi distruse in atac
		// e mai mic decat numarul total de armate al inamicului, atunci 
		// nu exista sanse de reusita ale atacului
		if(maxDefendersDestroyed < enemyArmies)
			return false;
		
		return true;
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
	 * 
	 * @param myRegion
	 * @param fromRegion
	 * @return Numarul minim de inamici distrusi intr-un atac cu toti calaretii
	 */
	private static int getMinDefendersDestroyed(AttackTransferMove attackMove) {
		int myArmies = attackMove.getArmies();
		double defendersDestroyed = myArmies * 0.504d;
		
		return ((int)Math.round(defendersDestroyed));
	}
	
	/**
	 * 
	 * @param myRegion
	 * @param fromRegion
	 * @return Numarul maxim de inamici distrusi intr-un atac cu toti calaretii
	 */
	private static int getMaxDefendersDestroyed(AttackTransferMove attackMove) {
		int myArmies = attackMove.getArmies();
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
		int enemyArmies = attackMove.getToRegion().getArmies();
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
		int enemyArmies = attackMove.getToRegion().getArmies();
		double attackersDestroyed = (enemyArmies * 0.748d);
		
		return ((int)Math.round(attackersDestroyed));
	}
}
