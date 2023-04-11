package fi.virri.game.sudoku.game;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public int size; // Row/column amount
    public List<Cell> cells;

    Board(int size, List<Cell> cells){
        this.size = size;
        this.cells = cells;
    }

    Board(int size, int[][] board2DArray){
        this.size = size;
        List<Cell> cells = new ArrayList<>(size*size);
        for(int row=0; row<size; row++){
            for(int col=0; col<size; col++){
                int value = board2DArray[row][col];
                boolean isStartingCell = value != 0;
                Cell cell = new Cell(row, col, value, isStartingCell, new ArrayList<>());
                cells.add(cell);
            }
        }
        this.cells = cells;
    }

    // Get cell from specified position
    public Cell getCell(int row, int col){
        return cells.get(row * size + col);
    }

    // Get board as 2D array
    public int[][] getAs2DArray(){
        int[][] result = new int[size][size];
        for (int i = 0; i < size*size; i++) {
            Cell cell = cells.get(i);
            result[cell.row][cell.col] = cell.value;
        }
        return result;
    }

    // Set all cells to starting cells
    // This disables interaction with the board when board is solved
    public void setImmutableDigits(){
        for (int i = 0; i < size*size; i++) {
            cells.get(i).isStartingCell = true;
        }
    }
}
