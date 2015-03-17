package strategy;

import java.util.ArrayList;

import bot.BotState;
import map.Region;
import move.AttackTransferMove;

public class AttacksCreator {
	// Starea curenta a jocului
	private BotState state;
	
	public AttacksCreator(BotState state) {
		this.state = state;
	}
	
	public ArrayList<AttackTransferMove> getAttackMoves() {
		computeGoForBonus();
		return state.attackTransferMoves;
	}
	
	public void computeGoForBonus() {
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithNeutrals()) {
			// Plasez calaretii doar pe regiunile vecine din aceasi super-regiune
			for(Region neighbor : region.getNeighbors()) {
				if(neighbor.getSuperRegion().getId() != region.getSuperRegion().getId())
					continue;
								
				double armiesNeeded = neighbor.getArmies() + 0.4 * neighbor.getArmies();
				int armies = (int) Math.round(armiesNeeded);
				
				if(armiesNeeded < region.getArmies())
					state.attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), region, neighbor, armies));				
			}
		}
	}
	
	public void computeAttackEnemy() {
		for(Region region : state.getStateAnalyzer().getMyBorderRegionsWithEnemy()) {
			// Plasez calaretii doar pe regiunile vecine din aceasi super-regiune
			for(Region neighbor : region.getNeighbors()) {
				// Caut regiunea vecina care e detinuta de inamic
				if(!neighbor.ownedByPlayer(state.getOpponentPlayerName()))
					continue;
				
				double armiesNeeded = neighbor.getArmies() + 0.4 * neighbor.getArmies();
				int armies = (int) Math.round(armiesNeeded);
				
				if(armiesNeeded < region.getArmies())
					state.attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), region, neighbor, armies));				
			}
		}
	}
}
