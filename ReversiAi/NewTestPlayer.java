package u7a4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import reversi.Arena;
import reversi.Coordinates;
import reversi.GameBoard;
import reversi.OutOfBoundsException;
import reversi.ReversiPlayer;
import reversi.Utils;
import u7a4.MinMaxPlayer.TimeOutException;
import u7a4.Move;

public class NewTestPlayer implements ReversiPlayer
{
	/**
	 * Die Farbe des Spielers.
	 */
	private int  color = 0;
	private int  colorEnemy = 0;
	private long timeLimit = 0;
	private long timeout = 0;
	private int rec = 0;
	private int res = 0;
	private static int MAXDEPTH = 30;
	private int[][]					lookUpTable		= new int[8][8];
	/**
	 * Alle mˆglichen Z¸ge werden in diesem Array gespeichert.
	 */
	private ArrayList<Coordinates> possibleMoves=new ArrayList<Coordinates>();
	
	/**
	 * Konstruktor, der bei der Gr√ºndung eines RandomPlayer eine Meldung auf den
	 * Bildschirm ausgibt.
	 */
	public NewTestPlayer()
	{
		System.out.println("TestPlayer erstellt.");
	}

	/**
	 * Speichert die Farbe und den Timeout-Wert in Instanzvariablen ab. Diese
	 * Methode wird vor Beginn des Spiels von {@link Arena} aufgerufen.
	 * 
	 * @see reversi.ReversiPlayer
	 */
	public void initialize(int color, long timeout)
	{
		this.color = color;
		this.colorEnemy = Utils.other(color);
		if (color == GameBoard.RED)
		{
			System.out.println("GreedyPlayer ist Spieler RED.");
		}
		else if (color == GameBoard.GREEN)
		{
			System.out.println("GreedyPlayer ist Spieler GREEN.");
		}
		this.timeLimit = timeout;
		fillLookUpTable();
		createAllPossibleMoves();
	}

	public Coordinates nextMove(GameBoard gb)
	{
		timeout = System.currentTimeMillis() + timeLimit - 10;
		
		
		Move best = new Move(null, Integer.MIN_VALUE);
		try{
			
			
			
			for(int i=0; i<MAXDEPTH;i++){
				rec = 0;
				res = 0;
				//System.out.println("V1 Depth:"+i);
				best = negaMax(gb,i,0,Integer.MIN_VALUE,Integer.MAX_VALUE,color);
				//Move next = max(gb,Integer.MIN_VALUE,Integer.MAX_VALUE,i,null);
				//best = negaScout(gb,i,0,Integer.MIN_VALUE,Integer.MAX_VALUE,color);
				//best = pvs(gb,i,-Integer.MAX_VALUE,Integer.MAX_VALUE,true);
				System.out.println("Depth:"+i+" "+best.getCoord()+":"+best.getValue()/*+" "+next.getCoord()+":"+next.getValue()*/);
				
			}
		}
		catch (TimeOutException e){
			
		}
		
		return best.getCoord();//nextMoves.get(0).coord;
	} 
	
	
	
	private Move max(GameBoard gb, int alpha, int beta, int depth, Coordinates last) throws TimeOutException{
		if(System.currentTimeMillis() >= timeout) throw new TimeOutException();
		res++;
		if (depth == 0) return new Move(null,eval(gb,color));
		
		ArrayList<Coordinates> currentPossibleMoves = getCurrentPossibleMoves(gb.clone(),color);
		
		if(currentPossibleMoves.isEmpty()){
			if(gb.isMoveAvailable(Utils.other(color))){
				return new Move(null,(min(gb,alpha,beta,depth -1,null)).getValue());
			}
			else
				return new Move(null,eval(gb,color));
		}
		Move best = new Move(null,Integer.MIN_VALUE);
		for(Coordinates coord: currentPossibleMoves){
			GameBoard newGB = gb.clone();
			newGB.checkMove(color, coord);
			newGB.makeMove(color, coord);
			
			Move result = min(newGB,alpha,beta,depth-1,coord);
			alpha = Math.max(alpha, result.getValue());
			
			if(result.getValue() > best.getValue()){
				best.setCoord(coord);
		        best.setValue(result.getValue());
			}
			
			if(alpha >= beta) break;
		}
		
		return new Move(best.getCoord(),alpha);
	}
	
	private Move min(GameBoard gb, int alpha, int beta, int depth, Coordinates last) throws TimeOutException{
		
		if(System.currentTimeMillis() >= timeout) throw new TimeOutException();
		res++;
		if (depth == 0) return new Move(null,eval(gb,color));
		
		ArrayList<Coordinates> currentPossibleMoves = getCurrentPossibleMoves(gb.clone(),Utils.other(color));
		if(currentPossibleMoves.isEmpty()){
			if(gb.isMoveAvailable(color)){
				return new Move(null,(max(gb,alpha,beta,depth -1,null)).getValue());
			}
			else
				return new Move(null,eval(gb,color));
		}
		Move best = new Move(null,Integer.MAX_VALUE);
		for(Coordinates coord: currentPossibleMoves){
			GameBoard newGB = gb.clone();
			newGB.checkMove(Utils.other(color), coord);
			newGB.makeMove(Utils.other(color), coord);
			Move result = max(newGB,alpha,beta,depth-1,coord);
			beta = Math.min(beta, result.getValue());
			
			if(result.getValue() < best.getValue()){
				best.setCoord(coord);
		        best.setValue(result.getValue());
			}
			
			if(beta <= alpha) break;
		}
		
		return new Move(best.getCoord(),beta);
	}
	private Move negaMax(GameBoard gb,int maxDepth,int depth,int alpha,int beta,int playerColor) throws TimeOutException{
		if(System.currentTimeMillis() >= timeout) throw new TimeOutException();
		rec = rec+1;
		int otherPlayer = Utils.other(playerColor);
		if(depth == maxDepth)
			return new Move(null,eval(gb,playerColor));
		
		
		ArrayList<Coordinates> currentPossibleMoves = getCurrentPossibleMoves(gb.clone(),playerColor);
		//ArrayList<Move> currentPossibleMoves = getCurrentPossibleMovesEval(gb.clone(),playerColor);
		//Collections.sort(currentPossibleMoves,(myTurn)?Move.getAscendingComparator():Move.getDescendingComparator());
		if(currentPossibleMoves.isEmpty()){
			
			if(gb.isMoveAvailable(otherPlayer)){
				return new Move(null,(negaMax(gb,maxDepth,depth+1,-beta,-alpha,otherPlayer)).getValue());
			}
			else{
			
				return new Move(null,eval(gb,playerColor));
			}
		}
		Move bestMove = new Move(null,Integer.MIN_VALUE);
		for(Coordinates coord: currentPossibleMoves){
			GameBoard newGB = gb.clone();
			newGB.checkMove(playerColor, coord);
			newGB.makeMove(playerColor, coord);
			
			Move result = negaMax(newGB,maxDepth,depth+1,-beta,-Math.max(bestMove.getValue(), alpha),otherPlayer);
			int currentScore = -result.getValue();
			
			if(currentScore > bestMove.getValue()){
				
				
				bestMove.setCoord(coord);
		        bestMove.setValue(currentScore);
		        
			}
			if (bestMove.getValue() >= beta)
				return bestMove;
			
			
		}
		
		
		return bestMove;
	}
	private Move negaScout(GameBoard gb, int maxDepth, int depth, int alpha, int beta,int playerColor) throws TimeOutException{
		if(System.currentTimeMillis() >= timeout) throw new TimeOutException();
		int otherPlayer = Utils.other(playerColor);
		if(depth == maxDepth){
			Move returns = new Move(null,0);
			if(playerColor != color){
				returns.setValue(-eval(gb,color));
			}
			
			else returns.setValue(eval(gb,color));;
			
			
			return returns;
		}
		
		ArrayList<Coordinates> currentPossibleMoves = getCurrentPossibleMoves(gb.clone(),playerColor);
		//ArrayList<Move> currentPossibleMoves = getCurrentPossibleMovesEval(gb.clone(),playerColor);
		//Collections.sort(currentPossibleMoves,(myTurn)?Move.getAscendingComparator():Move.getDescendingComparator());
		if(currentPossibleMoves.isEmpty()){
			
//			if(gb.isMoveAvailable(otherPlayer)){
//				return new Move(null,(negaMax(gb,maxDepth,depth+1,-beta,-alpha,otherPlayer)).getValue());
//			}
//			else{
				Move returns = new Move(null,0);
				if(playerColor != color){
					returns.setValue(-eval(gb,color));
				}
			
				else returns.setValue(eval(gb,color));;
			
				
				return returns;
			//}
		}
		int adaptiveBeta = beta;
		Move bestMove = new Move(null,Integer.MIN_VALUE);
		for(Coordinates coord: currentPossibleMoves){
			GameBoard newGB = gb.clone();
			newGB.checkMove(playerColor, coord);
			newGB.makeMove(playerColor, coord);
			
			Move result = negaMax(newGB,maxDepth,depth+1,-adaptiveBeta,-alpha,otherPlayer);
			int currentScore = -result.getValue();
			if(currentScore > bestMove.getValue()){
				if(adaptiveBeta == beta || depth >= maxDepth-2){
					bestMove = new Move(coord,currentScore);
				}
				else{
					result = negaScout(newGB,maxDepth,depth+1,-beta,-currentScore,otherPlayer);
					
				}
				
			}
			
			adaptiveBeta = alpha+1;
		}
		
		
		return new Move(bestMove.getCoord(),alpha);//bestMove;
	} 
	
	private Move pvs(GameBoard gb, int depth, int alpha, int beta, boolean myTurn) throws TimeOutException{
		if(System.currentTimeMillis() >= timeout) throw new TimeOutException();
		int playerColor = (myTurn)? color : colorEnemy;
		if(depth == 0){
			return new Move(null,eval(gb,playerColor));
		}
		/*if(alpha > 2000){
			System.out.println("something went wrong!!"+alpha);
		}*/
		ArrayList<Move> currentPossibleMoves = getCurrentPossibleMovesEval(gb.clone(),playerColor);
		Collections.sort(currentPossibleMoves,/*(myTurn)?*/Move.getAscendingComparator()/*:Move.getDescendingComparator()*/);
		Move best = new Move(null,Integer.MIN_VALUE);
		int currentScore = Integer.MIN_VALUE;
		for(int i = 0; i<currentPossibleMoves.size();i++){
			Move move = currentPossibleMoves.get(i);
			GameBoard newGB = gb.clone();
			newGB.checkMove(color, move.getCoord());
			newGB.makeMove(color, move.getCoord());
			Move result = new Move(null,Integer.MIN_VALUE);
			if(i>0){
				int testScore = -(pvs(newGB,depth-1,-alpha-1,-alpha,!myTurn)).getValue();
			
				
				if(alpha < testScore){
					
					
					currentScore = -(pvs(newGB,depth-1,-beta,-alpha,!myTurn)).getValue();
					System.out.println("Fail-Soft:"+testScore+" alpha:"+alpha+" newScore:"+currentScore+" bestScore:"+best.getValue());
				}
				
				
			}
			else
			{
				currentScore = -(pvs(newGB,depth-1,-beta,-alpha,!myTurn)).getValue();
				
			}
			if(currentScore > best.getValue()){
				best.setCoord(move.getCoord());
				best.setValue(currentScore);
			}
			alpha = Math.max(alpha, currentScore);
			if(alpha >= beta) break;
		}
		
		return new Move(best.getCoord(),alpha);
	}
	
	int eval(GameBoard gb, int playerColor){
		
		return nEval(gb.clone(),playerColor,0,0);
		
		
		//if(move == null) return 0;
		//return (lookUpTable[move.getRow() - 1][move.getCol() - 1]);
	}
	int evalMove(GameBoard gb, int playerColor,Coordinates coord){
		GameBoard newGB = gb.clone();
		newGB.checkMove(playerColor, coord);
		newGB.makeMove(playerColor, coord);
		return nEval(newGB,color,0,0);
		
		
		//if(move == null) return 0;
		//return (lookUpTable[move.getRow() - 1][move.getCol() - 1]);
	}
	int nEval(GameBoard gb, int col, int numValidMovesMe, int numValidMovesOpp)
	{
		double score = 0;
		
		try
		{
			int opp_color = Utils.other(col);
			int my_tiles = 0, opp_tiles = 0, i, j, k, my_front_tiles = 0, opp_front_tiles = 0, x, y;
			double p = 0, c = 0, l = 0, m = 0, f = 0, d = 0;

			int X1[] = { -1, -1, 0, 1, 1, 1, 0, -1 };
			int Y1[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

			// Piece difference, frontier disks and disk squares
			for (i = 0; i < 8; i++)
				for (j = 0; j < 8; j++)
				{
					if (gb.getOccupation(new Coordinates(i + 1, j + 1)) == col)
					{
						d += lookUpTable[i][j];
						my_tiles++;
					} else if (gb.getOccupation(new Coordinates(i + 1, j + 1)) == opp_color)
					{
						d -= lookUpTable[i][j];
						opp_tiles++;
					} else
					{
						for (k = 0; k < 8; k++)
						{
							x = i + X1[k];
							y = j + Y1[k];
							if (x >= 0 && x < 8 && y >= 0 && y < 8
									&& (gb.getOccupation(new Coordinates(i + 1, j + 1)) == GameBoard.EMPTY))
							{
								if (gb.getOccupation(new Coordinates(i + 1, j + 1)) == color)
									my_front_tiles++;
								else
									opp_front_tiles++;
								break;
							}
						}
					}
				}
			if (my_tiles > opp_tiles)
				p = (100.0 * my_tiles) / (my_tiles + opp_tiles);
			else if (my_tiles < opp_tiles)
				p = -(100.0 * opp_tiles) / (my_tiles + opp_tiles);
			else
				p = 0;

			if (my_front_tiles > opp_front_tiles)
				f = -(100.0 * my_front_tiles) / (my_front_tiles + opp_front_tiles);
			else if (my_front_tiles < opp_front_tiles)
				f = (100.0 * opp_front_tiles) / (my_front_tiles + opp_front_tiles);
			else
				f = 0;

			// Corner occupancy
			my_tiles = opp_tiles = 0;
			if (gb.getOccupation(new Coordinates(1, 1)) == col)
				my_tiles++;
			else if (gb.getOccupation(new Coordinates(1, 1)) == opp_color)
				opp_tiles++;
			if (gb.getOccupation(new Coordinates(1, 8)) == col)
				my_tiles++;
			else if (gb.getOccupation(new Coordinates(1, 8)) == opp_color)
				opp_tiles++;
			if (gb.getOccupation(new Coordinates(8, 1)) == col)
				my_tiles++;
			else if (gb.getOccupation(new Coordinates(8, 1)) == opp_color)
				opp_tiles++;
			if (gb.getOccupation(new Coordinates(8, 8)) == col)
				my_tiles++;
			else if (gb.getOccupation(new Coordinates(8, 8)) == opp_color)
				opp_tiles++;
			c = 25 * (my_tiles - opp_tiles);

			// Corner closeness
			my_tiles = opp_tiles = 0;
			if (gb.getOccupation(new Coordinates(1, 1)) == GameBoard.EMPTY)
			{
				if (gb.getOccupation(new Coordinates(1, 2)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(1, 2)) == opp_color)
					opp_tiles++;
				if (gb.getOccupation(new Coordinates(2, 2)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(2, 2)) == opp_color)
					opp_tiles++;
				if (gb.getOccupation(new Coordinates(2, 1)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(2, 1)) == opp_color)
					opp_tiles++;
			}
			if (gb.getOccupation(new Coordinates(1, 8)) == GameBoard.EMPTY)
			{
				if (gb.getOccupation(new Coordinates(1, 7)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(1, 7)) == opp_color)
					opp_tiles++;
				if (gb.getOccupation(new Coordinates(2, 7)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(2, 7)) == opp_color)
					opp_tiles++;
				if (gb.getOccupation(new Coordinates(2, 8)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(2, 8)) == opp_color)
					opp_tiles++;
			}
			if (gb.getOccupation(new Coordinates(8, 1)) == GameBoard.EMPTY)
			{
				if (gb.getOccupation(new Coordinates(8, 2)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(8, 2)) == opp_color)
					opp_tiles++;
				if (gb.getOccupation(new Coordinates(7, 2)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(7, 2)) == opp_color)
					opp_tiles++;
				if (gb.getOccupation(new Coordinates(7, 1)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(7, 1)) == opp_color)
					opp_tiles++;
			}
			if (gb.getOccupation(new Coordinates(8, 8)) == GameBoard.EMPTY)
			{
				if (gb.getOccupation(new Coordinates(7, 8)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(7, 8)) == opp_color)
					opp_tiles++;
				if (gb.getOccupation(new Coordinates(7, 7)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(7, 7)) == opp_color)
					opp_tiles++;
				if (gb.getOccupation(new Coordinates(8, 7)) == col)
					my_tiles++;
				else if (gb.getOccupation(new Coordinates(8, 7)) == opp_color)
					opp_tiles++;
			}
			l = -12.5 * (my_tiles - opp_tiles);

			// Mobility
			if (numValidMovesMe > numValidMovesOpp)
				m = (100.0 * numValidMovesMe) / (numValidMovesMe + numValidMovesOpp);
			else if (numValidMovesMe < numValidMovesOpp)
				m = -(100.0 * numValidMovesOpp) / (numValidMovesMe + numValidMovesOpp);
			else
				m = 0;

			// final weighted score
			score = (10 * p) + (801.724 * c) + (382.026 * l) + (/*78.922*/39 * m) + (74.396 * f) + (10 * d);
		} catch (OutOfBoundsException e)
		{
			e.printStackTrace();
		}
		return (int) score;
	}
	
	
	private ArrayList<Coordinates> getCurrentPossibleMoves(GameBoard gb, int pColor)
	{
		ArrayList<Coordinates> currentPossibleMoves = new ArrayList<Coordinates>();
		for (int i = 0; i < possibleMoves.size(); i++)
		{
			if (gb.checkMove(pColor, possibleMoves.get(i)))
			{
				currentPossibleMoves.add(possibleMoves.get(i));
			}
		}
		return currentPossibleMoves;

	}
	private ArrayList<Move> getCurrentPossibleMovesEval(GameBoard gb, int pColor)
	{
		ArrayList<Move> currentPossibleMoves = new ArrayList<Move>();
		for (int i = 0; i < possibleMoves.size(); i++)
		{
			if (gb.checkMove(pColor, possibleMoves.get(i)))
			{
				GameBoard newGB = gb.clone();
				newGB.checkMove(pColor, possibleMoves.get(i));
				newGB.makeMove(pColor, possibleMoves.get(i));
				currentPossibleMoves.add(new Move(possibleMoves.get(i),newGB.countStones(color)));
			}
		}
		return currentPossibleMoves;

	}
	private void createAllPossibleMoves()
	{
		for(int i=1; i<9; i++)
		{
			for(int j=1; j<9; j++)
			{
				possibleMoves.add(new Coordinates(i, j));
			}
		}
	}
	private void fillLookUpTable()
	{
		 lookUpTable = new int[][] { { 99, -32, 8, 6, 6, 8, -32, 99 }, { -32,
		 -32, -4, -3, -3, -4, -32, -32 },
		 { 8, -4, 7, 4, 4, 7, -4, 8 }, { 6, -3, 4, 0, 0, 4, -3, 6 }, { 6, -3,
		 4, 0, 0, 4, -3, 6 },
		 { 8, -4, 7, 4, 4, 7, -4, 8 }, { -32, -32, -4, -3, -3, -4, -32, -32 },
		 { 99, -32, 8, 6, 6, 8, -32, 99 } };
//		lookUpTable = new int[][] { { 99, -100, 0, 0, 0, 0, -100, 99 }, { -100, -100, 0, 0, 0, 0, -100, -100 },
//				{ 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 },
//				{ 0, 0, 0, 0, 0, 0, 0, 0 }, { -100, -100, 0, 0, 0, 0, -100, -100 },
//				{ 99, -100, 0, 0, 0, 0, -100, 99 } };
	}
	
	class TimeOutException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
}