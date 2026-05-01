package game.engine;


import java.util.ArrayList;
import java.util.Collections;

import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.monsters.Monster;

public class Board {
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters; 
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	
	public Board(ArrayList<Card> readCards) {
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		originalCards = readCards;
		cards = new ArrayList<Card>();
		setCardsByRarity();
		reloadCards();
	}
	
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	
	public static ArrayList<Card> getCards() {
		return cards;
	}
	
	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}
	private int[] indexToRowCol(int index) {
		int col;
		int row = (index/10);
		if(((index/10)%2)==0)
			col = (index%10);
		else
			col = (9-(index%10));
		return new int[] {row,col};
	}
	private Cell getCell(int index) {
		int row = this.indexToRowCol(index)[0];
		int col = this.indexToRowCol(index)[1];
		return this.boardCells[row][col];
	}
	private void setCell(int index,Cell cell) {
		int row = this.indexToRowCol(index)[0];
		int col = this.indexToRowCol(index)[1];
		this.boardCells[row][col] = cell;
	}
	public void initializeBoard(ArrayList<Cell>specialCells) {
		int j = 0;
		for(int i=0;i<100;i++) {
			if(i%2 != 0) {
				this.setCell(i,specialCells.get(j));
				j++;
			}
			else {
				this.setCell(i,new Cell(""));
			}
		}
		for(int i=0;i<Constants.CARD_CELL_INDICES.length;i++) {
			int index = Constants.CARD_CELL_INDICES[i];
			this.setCell(index,specialCells.get(50+i));
		}
		for(int i=0;i<Constants.MONSTER_CELL_INDICES.length;i++) {
			int index = Constants.MONSTER_CELL_INDICES[i];
			this.setCell(index, specialCells.get(70+i));
		}
		for(int i=0;i<Constants.CONVEYOR_CELL_INDICES.length;i++) {
			int index = Constants.CONVEYOR_CELL_INDICES[i];
			this.setCell(index, specialCells.get(60+i));
		}
		for(int i=0;i<Constants.SOCK_CELL_INDICES.length;i++) {
			int index = Constants.SOCK_CELL_INDICES[i];
			this.setCell(index,specialCells.get(i+65));
		}
		
	}
		
	
	private void setCardsByRarity() {
		ArrayList<Card> n = new ArrayList<>();
		for(int i=0; i<originalCards.size() ;i++) {
			for(int j=0;j< originalCards.get(i).getRarity();j++)	
				n.add(originalCards.get(i));	
		}
		originalCards=n;	
	}
	public static void reloadCards() {
		cards = new ArrayList<Card>(originalCards);
		Collections.shuffle(cards);
	}
	public static Card drawCard() {
	 if (cards.isEmpty())
		 reloadCards();
	 return cards.remove(0);
	}
	public void moveMonster(Monster currentMonster,int roll,Monster opponentMonster) throws InvalidMoveException{
		int oldPos = currentMonster.getPosition();
		int newPos = (oldPos+roll)%100;
		Cell newCell = getCell(newPos);
		newCell.onLand(currentMonster,opponentMonster);
		if(currentMonster.getPosition()==opponentMonster.getPosition()) {
			currentMonster.setPosition(oldPos);
			throw new InvalidMoveException();
			}
		if(currentMonster.getConfusionTurns()>0)
			currentMonster.setConfusionTurns(currentMonster.getConfusionTurns()-1);
		if(opponentMonster.getConfusionTurns()>0)
			opponentMonster.setConfusionTurns(opponentMonster.getConfusionTurns()-1);
		updateMonsterPositions(currentMonster,opponentMonster);
	}
	private void updateMonsterPositions(Monster player,Monster opponent) {
		for(int i=0;i<100;i++)
			this.getCell(i).setMonster(null);	
		int Pp = player.getPosition();
		int Op = opponent.getPosition();
		getCell(Pp).setMonster(player);
		getCell(Op).setMonster(opponent);
	}
	
}
