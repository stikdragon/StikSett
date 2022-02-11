package uk.co.stikman.sett.astar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.Map.Entry;

public class Pathfinder<T> {

	public List<T> search(T startNode, T finishNode) {

	/*	Set<PathfinderNode<T>> closed = new HashSet<>();
		Set<PathfinderNode<T>> open = new HashSet<>();

		PathfinderNode<T> start = new PathfinderNode<>(startNode);
		PathfinderNode<T> finish = new PathfinderNode<>(finishNode);
		
		open.add(start);
		MutableBoolean finished = new MutableBoolean(false);

		while (!open.isEmpty()) {
			PathfinderNode<T> p = findBestF(open);
			open.remove(p);

			getNeighbours(p.getNode(), n -> {
				if (finished.getValue())
					return;
				if (n == finish.getNode()) {					
					finish.previous = p;
					finished.setValue(true);
					return;
				}
				
				PathfinderNode<T> newp = new PathfinderNode<>(n);
				
				
			});
			
						if (finish.key.equals(newp.key)) {
							finish.previous = p;
							finished = true;
							break;
						}

						if (closed.contains(newp.key))
							continue;

						newp.height = world.get(x, y);
						newp.previous = p;
						float dist = 1.00000000414f;
						if (i == 0 || j == 0)
							dist = 1.0f;

						//
						// Scale dist by the upwards gradient here 
						//
						float dh = newp.height - p.height;
						//						if (dh > 0.0f)
						//							dist *= (1.0f + dh * terrainHeightCost);

						newp.g = newp.previous.g + dist;
						float dx = finish.key.x - x;
						float dy = finish.key.y - y;
						float h = (float) Math.sqrt(dx * dx + dy * dy);
						newp.f = h + newp.g;

						PathfinderNode existing = open.get(newp.key);
						if (existing != null) {
							if (existing.g > newp.g) {
								//
								// Found a better one
								//
								existing.previous = p;
								existing.g = newp.g;
								existing.f = newp.f;
							}
						} else {
							open.put(newp.key, newp);
						}
					} // validCoord
				} // for y
				if (finished)
					break;
			} // for x
			if (finished)
				break;
			closed.add(p.key);
		}

		//
		// Couldn't find a path
		//
		if (!finished)
			return null;

		LinkedList<PathfinderNode> res = new LinkedList<>();
		PathfinderNode p = finish;
		while (!p.key.equals(start.key)) {
			res.addFirst(p);
			p = p.previous;
		}

		return res;
		*/
		return null;
	
	}

	private void getNeighbours(T node, Consumer<T> consume) {
		
	}

	private PathfinderNode<T> findBestF(Set<PathfinderNode<T>> open) {
	/*	if (open.isEmpty())
			return null;
		float bestF = 0.0f;
		PathfinderNode best = null;
		for (Entry<NodeKey, PathfinderNode> e : open.entrySet()) {
			PathfinderNode p = e.getValue();
			if (best == null || p.f < bestF) {
				best = p;
				bestF = best.f;
			}
		}
		return best;*/
		return null;
	}

}
