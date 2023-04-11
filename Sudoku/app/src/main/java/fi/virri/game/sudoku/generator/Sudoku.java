package fi.virri.game.sudoku.generator;

import java.util.ArrayList;
import java.util.Collections;

public class Sudoku {
    public int[][] board;
    public int[][] solvedBoard;
    private final int dimension; // Number of columns/rows
    private final int SRN; // Number of column/rows in a sub-grid

    public Sudoku(int dimension) {
        this.dimension = dimension;
        SRN = (int) Math.sqrt(dimension);
        board = new int[dimension][dimension];
        solvedBoard = new int[dimension][dimension];
    }

    // Random number generator
    private int randomGenerator(int num)
    {
        return (int) Math.floor((Math.random()*num+1));
    }

    // Check if safe to put in cell
    private boolean CheckIfSafe(int i,int j,int num) {
        return (unUsedInRow(i, num) &&
                unUsedInCol(j, num) &&
                unUsedInBox(i-i%SRN, j-j%SRN, num));
    }

    // Check in the row for existence of digit
    private boolean unUsedInRow(int i,int num) {
        for(int j = 0; j< dimension; j++){
            if(board[i][j] == num){
                return false;
            }
        }
        return true;
    }

    // Check in the row for existence of digit
    private boolean unUsedInCol(int j,int num) {
        for(int i = 0; i< dimension; i++){
            if(board[i][j] == num){
                return false;
            }
        }
        return true;
    }

    // Check in the sub-grid for existence of digit
    private boolean unUsedInBox(int rowStart, int colStart, int num) {
        for (int i = 0; i<SRN; i++){
            for (int j = 0; j<SRN; j++){
                if (board[rowStart+i][colStart+j]==num){
                    return false;
                }
            }
        }
        return true;
    }

    // Fill a Sudoku board with digits
    private void fillValues() {
        // Fill the diagonal of SRN x SRN matrices
        fillDiagonal();

        // Fill remaining blocks
        fillRemaining(0, SRN);
    }

    // Fill all independent sub-grids with digits
    private void fillDiagonal() {
        for(int i = 0; i< dimension; i=i+SRN){
            // for diagonal box, start coordinates->i==j
            fillBox(i, i);
        }
    }

    // Fill a sub-grid
    private void fillBox(int row,int col) {
        int num;
        for(int i=0; i<SRN; i++){
            for(int j=0; j<SRN; j++){
                do{
                    num = randomGenerator(dimension);
                }
                while(!unUsedInBox(row, col, num));

                board[row+i][col+j] = num;
                solvedBoard[row+i][col+j] = num;
            }
        }
    }

    // A recursive function to fill rest of the board
    private boolean fillRemaining(int i, int j) {
        if(j>= dimension && i< dimension -1){
            i = i + 1;
            j = 0;
        }
        if (i>= dimension && j>= dimension){
            return true;
        }

        if(i < SRN){
            if(j < SRN){
                j = SRN;
            }
        }
        else if(i < dimension -SRN){
            if(j== (i/SRN) *SRN){
                j =  j + SRN;
            }

        }
        else {
            if(j == dimension -SRN){
                i = i + 1;
                j = 0;
                if(i>= dimension){
                    return true;
                }
            }
        }

        for(int num = 1; num<= dimension; num++){
            if(CheckIfSafe(i, j, num)){
                board[i][j] = num;
                solvedBoard[i][j] = num;
                if(fillRemaining(i, j+1)){
                    return true;
                }
                board[i][j] = 0;
                solvedBoard[i][j] = 0;
            }
        }
        return false;
    }

    // Remove digits until enough digits are removed
    // Sudoku must have a unique solution in the end
    private int removeDigits(int digitsToRemove) {
        ArrayList<Integer> list = new ArrayList<>(81);
        for (int i = 0; i < dimension*dimension; i++) {
            list.add(i);
        }
        Collections.shuffle(list);

        SudokuSolver sudokuSolver = new SudokuSolver();
        int index = list.size()-1;
        while(!list.isEmpty() && digitsToRemove != 0){ // All positions not checked OR Enough digits removed
            int cellId = list.get(index); // Get random position from the board
            list.remove(index); // Position is used
            int row = (cellId/ dimension); // Get coordinates and value
            int col = cellId%9;
            int num = board[row][col];

            if(num != 0){ // Non-empty cell
                board[row][col] = 0; // Make cell empty
                int solutions = sudokuSolver.solve(board); // Solve the board
                if(solutions == 1){ // If unique solution is found
                    digitsToRemove--; // Digit successfully removed
                }
                else{
                    board[row][col] = num; // No unique solution, put the number back in
                }
            }
            index--;
        }

        // Count the empty cells in the board
        int emptyCount = 0;
        for (int i = 0; i < 81; i++) {
            int row = (i/ dimension);
            int col = i%9;
            if(board[row][col] == 0){
                emptyCount++;
            }
        }
        return emptyCount;
    }

    // Generate a Sudoku board with the given column/row amount and empty cell count
    public static Sudoku generate(int dimension, int digitsToRemove) {
        Sudoku sudoku = null;
        int emptyCount = 0;
        while(emptyCount < digitsToRemove){ // Not enough digits removed -> Generate new board and try again
            sudoku = new Sudoku(dimension);
            sudoku.fillValues();
            emptyCount = sudoku.removeDigits(digitsToRemove);
        }
        return sudoku;
    }
}
