package fi.virri.game.sudoku.game;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public class Cell implements Parcelable {
    public int row;
    public int col;
    public int value;
    public boolean isStartingCell;
    public List<Integer> notes;

    // Used by Parcelable - DO NOT DELETE
    Cell(){

    }

    Cell(int row, int col, int value, boolean isStartingCell, List<Integer> notes){
        this.row = row;
        this.col = col;
        this.value = value;
        this.isStartingCell = isStartingCell;
        this.notes = notes;
    }

    protected Cell(@NonNull Parcel in) {
        row = in.readInt();
        col = in.readInt();
        value = in.readInt();
        isStartingCell = in.readByte() != 0;
        notes = new ArrayList<>();
        in.readList(notes, Integer.class.getClassLoader());
    }

    public static final Creator<Cell> CREATOR = new Creator<Cell>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public Cell createFromParcel(Parcel in) {
            return new Cell(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public Cell[] newArray(int size) {
            return new Cell[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(row);
        parcel.writeInt(col);
        parcel.writeInt(value);
        parcel.writeByte((byte) (isStartingCell ? 1 : 0));
        parcel.writeList(notes);
    }
}
