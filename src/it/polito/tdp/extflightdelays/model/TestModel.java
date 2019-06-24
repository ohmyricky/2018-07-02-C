package it.polito.tdp.extflightdelays.model;

public class TestModel {

	public static void main(String[] args) {
		
		Model model = new Model();
		
		model.creaGrafo(10);
		
		System.out.println(model.voliMax(model.getIdMap().get(60), model.getIdMap().get(51), 5));
		System.out.println(model.voliCt(model.getPest()));
		
	}

}
