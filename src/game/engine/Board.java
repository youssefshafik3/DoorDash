package game.engine;

import java.util.ArrayList;

public class Board {
	private Cell[][] boardCells;
	private	ArrayList<Monster> stationedMonsters;
	private	ArrayList<Card> originalCards;
	private ArrayList<Card> cards;
	public ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	public void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		this.stationedMonsters = stationedMonsters;
	}
	public ArrayList<Card> getCards() {
		return cards;
	}
	public void setCards(ArrayList<Card> cards) {
		this.cards = cards;
	}
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	public ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	 public Board(ArrayList<Card> readCards) {
	        boardCells = new Cell[BOARD_ROWS][BOARD_COLS];
	        stationedMonsters = new ArrayList<>();
	        cards = new ArrayList<>();
	        originalCards = readCards;
	    }
	}

