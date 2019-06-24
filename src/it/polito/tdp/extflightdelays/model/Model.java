package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	
	SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	Map<Integer, Airport> idMap;
	ExtFlightDelaysDAO dao;
	List<Rotta> rotte;
	
	//ricorsione
	private List<Airport> pest;
	
	public Model() {
		grafo=new SimpleWeightedGraph<Airport, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		idMap=new HashMap<Integer, Airport>();
		dao=new ExtFlightDelaysDAO();
		dao.loadAllAirports(idMap);
		rotte= new ArrayList<Rotta>();
	}
	
	public void creaGrafo(int numCompagnie) {
		//vertici
		Graphs.addAllVertices(this.grafo, dao.aeroportiXComp(numCompagnie, idMap));
		
		//archi
		for(Rotta r: dao.getRotte(idMap)) {
			if(grafo.containsVertex(r.getA1()) && grafo.containsVertex(r.getA2())) {

				if(grafo.getEdge(r.getA1(), r.getA2())==null) {
					Graphs.addEdge(this.grafo, r.getA1(), r.getA2(), r.getVoli());
					rotte.add(new Rotta(r.getA1(), r.getA2(), r.getVoli()));
				} else  {
					double voli=0;
					voli=grafo.getEdgeWeight(grafo.getEdge(r.getA1(), r.getA2()));
					grafo.setEdgeWeight(grafo.getEdge(r.getA1(), r.getA2()), r.getVoli()+voli);
					for(Rotta t: rotte) {
						if(t.getA1().equals(r.getA2()) && t.getA2().equals(r.getA1())) {
							t.setVoli((int) (r.getVoli()+voli));
						}
					}
				}
			}
		}
		
		System.out.println("Vertici grafo: "+this.grafo.vertexSet().size());
		System.out.println("Archi grafo: "+this.grafo.edgeSet().size());
	}

	public Set<Airport> getGrafo() {
		return grafo.vertexSet();
	}
	
	public List<Rotta> vicini(Airport a) {
		
		List<Airport> vicini=Graphs.neighborListOf(this.grafo, a);
		List<Rotta> res= new ArrayList<Rotta>();
		
		for(Airport t: vicini) {
			double peso= this.grafo.getEdgeWeight(this.grafo.getEdge(a, t));
			res.add(new Rotta(a, t, (int) peso));
		}
		Collections.sort(res);
		return res;
		
	}
	
	public List<Airport> voliMax(Airport p, Airport d, int tratte) {
		pest=null;
		
		List<Airport> parziale= new ArrayList<>();
		List<Airport> visited= new ArrayList<Airport>();
		
		ricorsione(p, d, parziale, tratte);
		
		return pest;
	}

	private void ricorsione(Airport a, Airport d, List<Airport> parziale, int livello) {
		//do always
		parziale.add(a);
		List<Airport> toVisit= new ArrayList<Airport>();
		for (Airport t : Graphs.neighborListOf(this.grafo, a)) {
			if(!parziale.contains(t)) {
				toVisit.add(t);
			}
		}
		
		//term
		if(livello==0 || parziale.size()==this.grafo.vertexSet().size() 
				|| toVisit.size()==0 ) {
			if (a.equals(d)) {
				int voli=calcolaVoli(parziale);
				if (pest==null || voli>calcolaVoli(pest)) {
					pest= new ArrayList<>(parziale);
				}
			}
		} else {
			for (Airport airport : toVisit) {
				ricorsione(airport, d, parziale, livello-1);
			}
		}
		//backtrack
		parziale.remove(a);
		
	}
	
	public int voliCt(List<Airport> pest) {
		return this.calcolaVoli(pest);
	}

	private int calcolaVoli(List<Airport> parziale) {
		int somma=0;
		for (int i=0; i<parziale.size()-1; i++) {
			Airport a1=parziale.get(i);
			Airport a2=parziale.get(i+1);
			
			somma+=this.grafo.getEdgeWeight(this.grafo.getEdge(a1, a2));
		}
		return somma;
	}

	public Map<Integer, Airport> getIdMap() {
		return idMap;
	}

	public List<Airport> getPest() {
		return pest;
	}
	
	

}
