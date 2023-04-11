package fi.virri.game.sudoku.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fi.virri.game.sudoku.game.Cell;

public class SudokuDataObject implements Parcelable {
    private String key; // Unique key
    private String userId; // Not used currently

    private String imageChildPath; // Child path of the image of the board
    private String name; // Board name given by the user

    // SudokuGame's state can be derived from these
    private int selectedRow;
    private int selectedCol;
    private int boardSize;
    private List<Integer> originalBoard;
    private List<Integer> solvedBoard;
    private List<Cell> boardCells;
    private boolean isTakingNotes;
    private boolean isBoardSolved;
    private boolean solveButtonPressed;
    private int seconds;
    private int difficulty;

    protected SudokuDataObject(@NonNull Parcel in) {
        key = in.readString();
        userId = in.readString();
        imageChildPath = in.readString();

        name = in.readString();
        selectedRow = in.readInt();
        selectedCol = in.readInt();
        boardSize = in.readInt();

        originalBoard = new ArrayList<>();
        in.readList(originalBoard, (Integer.class.getClassLoader()));
        solvedBoard = new ArrayList<>();
        in.readList(solvedBoard, (Integer.class.getClassLoader()));
        boardCells = new ArrayList<>();
        in.readTypedList(boardCells, Cell.CREATOR);

        isTakingNotes = in.readByte() != 0;
        isBoardSolved = in.readByte() != 0;
        solveButtonPressed = in.readByte() != 0;

        seconds = in.readInt();
        difficulty = in.readInt();
    }

    public SudokuDataObject(){

    }

    public static final Creator<SudokuDataObject> CREATOR = new Creator<SudokuDataObject>() {
        @Override
        public SudokuDataObject createFromParcel(Parcel in) {
            return new SudokuDataObject(in);
        }

        @Override
        public SudokuDataObject[] newArray(int size) {
            return new SudokuDataObject[size];
        }
    };

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    // Not used currently
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageChildPath() {
        return imageChildPath;
    }

    public void setImageChildPath(String image) {
        this.imageChildPath = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public void setSelectedCol(int selectedCol) {
        this.selectedCol = selectedCol;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public List<Cell> getBoardCells() {
        return boardCells;
    }

    public void setBoardCells(List<Cell> cells) {
        this.boardCells = cells;
    }

    public List<Integer> getOriginalBoard() {
        return originalBoard;
    }

    public void setOriginalBoard(List<Integer> originalBoard) {
        this.originalBoard = originalBoard;
    }

    public List<Integer> getSolvedBoard() {
        return solvedBoard;
    }

    public void setSolvedBoard(List<Integer> solvedBoard) {
        this.solvedBoard = solvedBoard;
    }

    public boolean isTakingNotes() {
        return isTakingNotes;
    }

    public void setTakingNotes(boolean takingNotes) {
        isTakingNotes = takingNotes;
    }

    public boolean isBoardSolved() {
        return isBoardSolved;
    }

    public void setBoardSolved(boolean boardSolved) {
        isBoardSolved = boardSolved;
    }

    public boolean isSolveButtonPressed() {
        return solveButtonPressed;
    }

    public void setSolveButtonPressed(boolean solveButtonPressed) {
        this.solveButtonPressed = solveButtonPressed;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(userId);

        parcel.writeString(imageChildPath);
        parcel.writeString(name);

        parcel.writeInt(selectedRow);
        parcel.writeInt(selectedCol);
        parcel.writeInt(boardSize);
        parcel.writeList(originalBoard);
        parcel.writeList(solvedBoard);
        parcel.writeTypedList(boardCells);
        parcel.writeByte((byte) (isTakingNotes ? 1 : 0));
        parcel.writeByte((byte) (isBoardSolved ? 1 : 0));
        parcel.writeByte((byte) (solveButtonPressed ? 1 : 0));

        parcel.writeInt(seconds);
        parcel.writeInt(difficulty);
    }
}
