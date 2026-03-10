package game.engine.dataloader;

import game.engine.Role;
import game.engine.cards.Card;
import game.engine.cards.ConfusionCard;
import game.engine.cards.EnergyStealCard;
import game.engine.cards.ShieldCard;
import game.engine.cards.StartOverCard;
import game.engine.cards.SwapperCard;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.monsters.Dasher;
import game.engine.monsters.Dynamo;
import game.engine.monsters.Monster;
import game.engine.monsters.MultiTasker;
import game.engine.monsters.Schemer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DataLoader {
	
	private	final static String CARDS_FILE_NAME = "cards.csv";
	private	final static String CELLS_FILE_NAME = "cells.csv";
	private	final static String MONSTERS_FILE_NAME = "monsters.csv";
	
	public static ArrayList<Card> readCards() throws IOException{
		ArrayList<Card> cards = new ArrayList<Card>();
		try (BufferedReader br = new BufferedReader(new FileReader(CARDS_FILE_NAME))) {
			String line;  
			while ((line = br.readLine()) != null) {
		    	String[] cardValues = line.split(",");
		    	String type = cardValues[0];
		    	String name = cardValues[1];
	    		String description = cardValues[2];
	    		int rarity = Integer.parseInt(cardValues[3]);
		    	switch (type){
		    	case("SWAPPER"):
		    		SwapperCard sc = new SwapperCard(name,description,rarity);
		    		cards.add(sc);
		    		break;
		    	case ("STARTOVER"):
		    		boolean lucky = Boolean.parseBoolean(cardValues[4]);
		    		StartOverCard soc = new StartOverCard(name,description,rarity,lucky);
		    		cards.add(soc);
		    		break;
		    	case ("ENERGYSTEAL"):
		    		int  energy = Integer.parseInt(cardValues[4]);
	    			EnergyStealCard esc = new EnergyStealCard(name,description,rarity,energy);
	    			cards.add(esc);
	    			break;
		    	case ("SHIELD"):
		    		ShieldCard shc = new ShieldCard(name,description,rarity);
		    		cards.add(shc);
		    		break;
		    	case ("CONFUSION"):
		    		int  duration = Integer.parseInt(cardValues[4]);
    				ConfusionCard cc = new ConfusionCard(name,description,rarity,duration);
    				cards.add(cc);
    				break;
		    	default:
    				break;
		    	}
		      }
		}
		return cards;
	}
	public static ArrayList<Cell> readCells() throws IOException{
		ArrayList<Cell> cells = new ArrayList<Cell>();
		try (BufferedReader br = new BufferedReader(new FileReader(CELLS_FILE_NAME))) {
		      String line;
		      int counter = 0;
		      while ((line = br.readLine()) != null){
		    	  counter++;
		    	  line = br.readLine();
		    	  String[] cardValues = line.split(",");
		    	  String name = cardValues[0];
		    	  if (counter<=50){
		    		  Role role = Role.valueOf(cardValues[1]);
		    		  int energy = Integer.parseInt(cardValues[2]);
		    		  DoorCell dc = new DoorCell(name, role , energy);
		    		  cells.add(dc);
		    	  }
		    	  else {
			    	  int effect = Integer.parseInt(cardValues[1]);
				    	  if (effect>0){
				    		  ConveyorBelt cb = new ConveyorBelt(name, effect);
				    		  cells.add(cb);
				    	  }
				    	  else{
				    		  ContaminationSock cs = new ContaminationSock(name,effect);
				    		  cells.add(cs);
				    	  }
		      }
		}
		}
		return cells;
		}
	 public static ArrayList<Monster> readMonsters() throws IOException{
		 ArrayList<Monster> monsters = new ArrayList<Monster>();
		 try (BufferedReader br = new BufferedReader(new FileReader(MONSTERS_FILE_NAME))) {
		      String line;
		      while ((line = br.readLine()) != null) {
		    	  	String[] cardValues = line.split(",");
			    	String type = cardValues[0];
			    	String name = cardValues[1];
		    		String description = cardValues[2];
		    		Role originalRole = Role.valueOf(cardValues[3]);
		    		int energy = Integer.parseInt(cardValues[4]);
		    		switch(type){
		    			case("DYNAMO"):
		    				Dynamo dm = new Dynamo(name,description,originalRole,energy);
		    				monsters.add(dm);
		    				break;
		    			case("DASHER"):
		    				Dasher dsh = new Dasher(name,description,originalRole,energy);
		    				monsters.add(dsh);
		    				break;
		    			case("SCHEMER"):
		    				Schemer sch = new Schemer(name,description,originalRole,energy);
		    				monsters.add(sch);
		    				break;
		    			case("MULTITASKER"):
		    				MultiTasker multi = new MultiTasker(name,description,originalRole,energy);
		    				monsters.add(multi);
		    				break;
		    			default:
		    				break;
		    		}
		      }
		}
		 return monsters;
	 }
}
