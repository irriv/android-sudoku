package fi.virri.game.sudoku.generator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.util.stream.IntStream;

public class SudokuSolver {
    private final int BOARD_START_INDEX = 0; // First column/row index
    private final int BOARD_SIZE = 9; // Board column/row amount
    private final int SUBSECTION_SIZE = 3; // Sub-grid column/row amount
    private final int NO_VALUE = 0; // Empty value of a cell
    private final int MIN_VALUE = 1; // Min value of a cell
    private final int MAX_VALUE = 9; // Max value of a cell

    // Solve a given Sudoku board
    // Return solution count
    // Recursion is stopped if multiple solutions are found
    public int solve(int[][] board) {
        int[] rowCol = findEmpty(board); // Find an empty cell
        if(rowCol == null){ // No empty cells left -> board is solved
            return 1;
        }
        int solutions = 0;
        int row = rowCol[0];
        int col = rowCol[1];
        for (int k = MIN_VALUE; k <= MAX_VALUE; k++) { // Try all digits into a cell until a valid digit is found
            board[row][col] = k;
            if (isValid(board, row, col)) {
                solutions += solve(board);
                if(solutions > 1){ // Multiple solutions found
                    return solutions;
                }
            }
            board[row][col] = NO_VALUE; // No digit was valid for the position -> set cell as empty
        }
        return solutions;
    }

    // Find empty cell from the Sudoku board
    @Nullable
    @Contract(pure = true)
    private int[] findEmpty(int[][] board){
        for(int row = BOARD_START_INDEX; row < BOARD_SIZE; row++){
            for (int column = BOARD_START_INDEX; column < BOARD_SIZE; column++) {
                if(board[row][column] == NO_VALUE){
                    return new int[]{row, column};
                }
            }
        }
        return null;
    }

    // Check board for validity
    private boolean isValid(int[][] board, int row, int column) {
        return (rowConstraint(board, row)
                && columnConstraint(board, column)
                && subsectionConstraint(board, row, column));
    }

    // Check row for validity
    private boolean rowConstraint(int[][] board, int row) {
        boolean[] constraint = new boolean[BOARD_SIZE];
        return IntStream.range(BOARD_START_INDEX, BOARD_SIZE)
                .allMatch(column -> checkConstraint(board, row, constraint, column));
    }

    // Check column for validity
    private boolean columnConstraint(int[][] board, int column) {
        boolean[] constraint = new boolean[BOARD_SIZE];
        return IntStream.range(BOARD_START_INDEX, BOARD_SIZE)
                .allMatch(row -> checkConstraint(board, row, constraint, column));
    }

    // Check sub-grid for validity
    private boolean subsectionConstraint(int[][] board, int row, int column) {
        boolean[] constraint = new boolean[BOARD_SIZE];
        int subsectionRowStart = (row / SUBSECTION_SIZE) * SUBSECTION_SIZE;
        int subsectionRowEnd = subsectionRowStart + SUBSECTION_SIZE;

        int subsectionColumnStart = (column / SUBSECTION_SIZE) * SUBSECTION_SIZE;
        int subsectionColumnEnd = subsectionColumnStart + SUBSECTION_SIZE;

        for (int r = subsectionRowStart; r < subsectionRowEnd; r++) {
            for (int c = subsectionColumnStart; c < subsectionColumnEnd; c++) {
                if (!checkConstraint(board, r, constraint, c)) return false;
            }
        }
        return true;
    }

    private boolean checkConstraint(@NonNull int[][] board, int row, boolean[] constraint, int column) {
        if (board[row][column] != NO_VALUE) {
            if (!constraint[board[row][column] - 1]) {
                constraint[board[row][column] - 1] = true;
            } else {
                return false;
            }
        }
        return true;
    }
}
