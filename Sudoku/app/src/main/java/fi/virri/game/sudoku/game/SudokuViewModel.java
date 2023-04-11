package fi.virri.game.sudoku.game;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

// Separate GameActivity and SudokuGame classes
public class SudokuViewModel extends AndroidViewModel {
    public final SudokuGame sudokuGame = new SudokuGame();

    public SudokuViewModel(@NonNull Application application) {
        super(application);
    }
}
