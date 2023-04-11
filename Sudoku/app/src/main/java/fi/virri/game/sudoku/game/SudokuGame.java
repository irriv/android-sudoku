package fi.virri.game.sudoku.game;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fi.virri.game.sudoku.database.SudokuDataObject;

public class SudokuGame {
    public final MutableLiveData<Pair<Integer, Integer>> selectedCellLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Cell>> cellsLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> isBoardSolvedLiveData = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isTakingNotesLiveData = new MutableLiveData<>(false);
    public MutableLiveData<List<Integer>> highlightedKeysLiveData = new MutableLiveData<>();

    private int selectedRow = -1;
    private int selectedCol = -1;
    private Board board = null;
    private int[][] originalBoard;
    private int[][] solvedBoard;
    private boolean isTakingNotes = false;
    private boolean isBoardSolved = false;

    // Digit button pressed - handle input
    public void handleInput(int number){
        if(selectedRow == -1 || selectedCol == -1){ // No selection
            return;
        }
        Cell cell = board.getCell(selectedRow, selectedCol);
        if(cell.isStartingCell){ // Should not be possible but just to be safe
            return;
        }
        if(isTakingNotes){ // Update notes
            if(cell.notes.contains(number)){ // Notes include the number - remove it
                for (int i = cell.notes.size()-1; i >= 0; i--) {
                    if(cell.notes.get(i) == number){
                        cell.notes.remove(i);
                    }
                }
            }
            else{ // Notes do not include the number - add it
                cell.notes.add(number);
            }
            highlightedKeysLiveData.postValue(cell.notes);
            cellsLiveData.postValue(board.cells);
        }
        else{ // Update value
            cell.value = number;
            checkWinState();
        }
    }

    // Erase button pressed - handle erase
    public void handleErase(){
        if(selectedRow == -1 || selectedCol == -1){ // No selection
            return;
        }
        Cell cell = board.getCell(selectedRow, selectedCol);
        if(cell.value == 0){ // Empty cell
            return;
        }
        cell.value = 0;
        cellsLiveData.postValue(board.cells);
    }

    // Cell was touched - update selected cell
    public void updateSelectedCell(int row, int col){
        Cell cell = board.getCell(row, col);
        if(!cell.isStartingCell){
            selectedRow = row;
            selectedCol = col;
            selectedCellLiveData.postValue(Pair.create(row, col));
            if(isTakingNotes){
                if(selectedRow != -1 && selectedCol != -1){
                    highlightedKeysLiveData.postValue(cell.notes);
                }
            }
        }
    }

    // Notes button pressed - change note taking state
    public void changeNoteTakingState(){
        isTakingNotes = !isTakingNotes;
        isTakingNotesLiveData.postValue(isTakingNotes); // Change notes button image
        List<Integer> currentNotes;
        if(isTakingNotes && selectedRow != -1 && selectedCol != -1){
            currentNotes = board.getCell(selectedRow, selectedCol).notes;
        }
        else{
            currentNotes = new ArrayList<>();
        }
        highlightedKeysLiveData.postValue(currentNotes); // Update highlighted keys
    }

    // Load save
    public void loadSaveState(@NonNull SudokuDataObject saveState){
        selectedRow = saveState.getSelectedRow(); // Selected cell
        selectedCol = saveState.getSelectedCol(); // Selected cell
        originalBoard = listTo2DArray(saveState.getOriginalBoard()); // Original board
        solvedBoard = listTo2DArray(saveState.getSolvedBoard()); // Solved board
        isTakingNotes = saveState.isTakingNotes(); // Note taking state
        isBoardSolved = saveState.isBoardSolved(); // Board solved state
        List<Cell> cells = saveState.getBoardCells(); // Cells
        this.board = new Board(saveState.getBoardSize(), cells); // Board
        checkWinState();
        isTakingNotesLiveData.postValue(isTakingNotes);
        if(selectedRow != -1 && selectedCol != -1){ // Selection exists
            highlightedKeysLiveData.postValue(board.getCell(selectedRow, selectedCol).notes);
        }
    }

    // New game - initialize the board with the given board, store solved board
    public void initializeBoard(@NonNull int[][] board, int[][] solvedBoard){
        originalBoard = board;
        this.solvedBoard = solvedBoard;
        this.board = new Board(board.length, board);
        selectedCellLiveData.postValue(Pair.create(selectedRow, selectedCol)); // No selection
        cellsLiveData.postValue(this.board.cells);
    }

    // Check if board is solved
    public void checkWinState(){
        if(board == null || solvedBoard == null){
            return;
        }
        if(Arrays.deepEquals(board.getAs2DArray(), solvedBoard)){ // Board is solved
            selectedRow = -1; // Reset selection
            selectedCol = -1; // Reset selection
            selectedCellLiveData.postValue(Pair.create(selectedRow, selectedCol));
            board.setImmutableDigits(); // Disable board interactivity
            cellsLiveData.postValue(board.cells);
            isBoardSolved = true; // Board is solved
            isBoardSolvedLiveData.postValue(true);
        }
        else{ // Board is not solved
            selectedCellLiveData.postValue(Pair.create(selectedRow, selectedCol));
            cellsLiveData.postValue(board.cells);
            isBoardSolved = false;
            isBoardSolvedLiveData.postValue(false);
        }
    }

    // Reset button pressed - reset board
    public void resetBoard(){
        selectedRow = -1; // Reset selection
        selectedCol = -1; // Reset selection
        selectedCellLiveData.postValue(Pair.create(selectedRow, selectedCol));
        board.cells = new Board(originalBoard.length, originalBoard).cells; // Reset cells
        cellsLiveData.postValue(this.board.cells);
        isBoardSolved = false; // Board not solved
        isBoardSolvedLiveData.postValue(false);
        isTakingNotes = false; // Not taking notes
        isTakingNotesLiveData.postValue(false);
        highlightedKeysLiveData.postValue(new ArrayList<>()); // No highlighted keys
    }

    // Solve board button pressed - solve board
    public void solveBoard(){
        if(isTakingNotes){
            if(selectedRow == -1 || selectedCol == -1){
                return;
            }
            isTakingNotesLiveData.postValue(false);
            highlightedKeysLiveData.postValue(new ArrayList<>());
        }
        this.board = new Board(solvedBoard.length, solvedBoard);
        checkWinState();
    }

    // Solve cell button pressed - solve selected cell
    public void solveCell(){
        if(selectedRow == -1 || selectedCol == -1){
            return;
        }
        Cell cell = board.getCell(selectedRow, selectedCol);
        int value = solvedBoard[selectedRow][selectedCol];
        if(cell.value != value){
            cell.value = value;
            checkWinState();
        }
    }

    // When saving game must convert boards (2D arrays) to lists because of Parcelable
    @NonNull
    private List<Integer> array2DToList(@NonNull int[][] board){
        int len = board.length;
        List<Integer> list = new ArrayList<>(len*len);
        for (int[] ints : board){
            for (int j = 0; j < len; j++){
                list.add(ints[j]);
            }
        }
        return list;
    }

    // When loading game must convert boards (lists) to 2D arrays because of Parcelable
    @NonNull
    private int[][] listTo2DArray(@NonNull List<Integer> list){
        int len = (int) Math.sqrt(list.size());
        int[][] board = new int[len][len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                board[i][j] = list.get(len*i+j);
            }
        }
        return board;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public Board getBoard() {
        return board;
    }

    public List<Integer> getOriginalBoard() {
        return array2DToList(originalBoard);
    }

    public List<Integer> getSolvedBoard() {
        return array2DToList(solvedBoard);
    }

    public boolean getIsTakingNotes() {
        return isTakingNotes;
    }

    public boolean getIsBoardSolved(){
        return isBoardSolved;
    }
}
