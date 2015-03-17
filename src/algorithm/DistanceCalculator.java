package algorithm;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import map.*;

public class DistanceCalculator {
	
	public static LinkedList<Region> getShortestPath(Region start, List<Region> destinations) {
		LinkedList<Region> shortestPath = new LinkedList<>();
		
		// Initializare coada
		LinkedList<Region> Q = new LinkedList<>();
		
		// Marcare regiuni vizitate
		Map<Region, Boolean> visited = new HashMap<>();
		
		// Pentru reconstruire drum
		// key:v -> value:u - means that node u was discovered
		// coming from node v
		Map<Region, Region> regionDiscoveredBy = new HashMap<>();
		
		// Nodul de inceput
		Q.push(start);
		regionDiscoveredBy.put(start, null);
		
		// Macare ca vizitat
		visited.put(start, true);
		
		Region destinationRegion = null;
		
		while(!Q.isEmpty() && destinationRegion == null) {
			// Regiune careia sa-i vizitez vecinii
			Region region = Q.pop();
			
			for(Region neighbor : region.getNeighbors()) {
				// Daca veciniul a fost vizitat, treci peste
				if(visited.containsKey(neighbor))
					continue;
				
				// Adauga vecinul in coada
				Q.push(neighbor);
				
				// Marcare ca vizitat
				visited.put(neighbor, true);	
				
				regionDiscoveredBy.put(neighbor, region);
				
				// Ma opresc daca am gasit nod de destinatie
				if(destinations.contains(neighbor)) {
					destinationRegion = neighbor;
					break;
				}
			}
		}
		
		while(destinationRegion != null) {
			shortestPath.push(destinationRegion);
			destinationRegion = regionDiscoveredBy.get(destinationRegion);
		}
		
		return shortestPath;
	}
	
	/**
	 * 
	 * @param start
	 * @param destinations
	 * @return Imediat regiuea urmatoare din drumul cel mai scurt catre o bordura
	 */
	public static Region nextRegionToBorder(Region start, List<Region> destinations) {
		LinkedList<Region> shortestPath = getShortestPath(start, destinations);
		// Primul element este defapt nodul de start
		shortestPath.pop();		
		
		return shortestPath.peekFirst();
		
	}
	
	
}
