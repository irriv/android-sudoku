package fi.virri.game.sudoku;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import fi.virri.game.sudoku.database.SudokuDataObject;
import fi.virri.game.sudoku.game.Board;
import fi.virri.game.sudoku.game.Cell;
import fi.virri.game.sudoku.game.SudokuBoardView;
import fi.virri.game.sudoku.game.SudokuViewModel;
import fi.virri.game.sudoku.generator.Sudoku;

public class GameActivity extends AppCompatActivity implements SudokuBoardView.OnTouchListener {
    private final String BUCKET_NAME = "gs://" + FirebaseApp.getInstance().getOptions().getStorageBucket(); // Bucket name of board image storage
    private int mDifficulty; // 0 for save, 1 for easy, 2 for medium, 3 for hard
    private int mSeconds = 0; // Stopwatch starts counting from 0
    private boolean isRunning = false; // Stopwatch state
    private boolean wasRunning = false; // Stopwatch state memory in case of pauses or resumes
    private boolean wasBoardSolved = false; // Solve state of loaded game/freshly solved game
    private boolean solveButtonPressed = false; // Solve buttons used with this Sudoku game - invalidates solve for leaderboards

    // UI stuff
    private SudokuBoardView mSudokuBoardView;
    private final List<Button> numberButtons = new ArrayList<>();
    private ImageButton notesButton;
    private TextView timeView;

    private Sudoku mSudoku; // New generated Sudoku board
    private SudokuDataObject mLoadedState; // Loaded game
    private SudokuDataObject mSavedThisSessionState; // A game saved this session
    private SudokuViewModel mSudokuViewModel;

    // Firebase stuff
    private CollectionReference collectionReference; // For saved games
    private StorageReference storageReference; // For board images of saves
    private FirebaseUser currentUser; // Current user

    private final MutableLiveData<String> mSudokuNameLiveData = new MutableLiveData<>(); // Name for game being saved - On change start saving the game
    private final Handler mHandler = new Handler(); // Handles running of stopwatch
    private Runnable mRunnable; // Runs stopwatch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Firebase references updated only if user is email authenticated
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            if(currentUser.getEmail() != null){
                collectionReference = FirebaseFirestore.getInstance().collection(currentUser.getUid());
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(BUCKET_NAME);
            }
        }

        notesButton = findViewById(R.id.notesButton);
        numberButtons.add(findViewById(R.id.oneButton));
        numberButtons.add(findViewById(R.id.twoButton));
        numberButtons.add(findViewById(R.id.threeButton));
        numberButtons.add(findViewById(R.id.fourButton));
        numberButtons.add(findViewById(R.id.fiveButton));
        numberButtons.add(findViewById(R.id.sixButton));
        numberButtons.add(findViewById(R.id.sevenButton));
        numberButtons.add(findViewById(R.id.eightButton));
        numberButtons.add(findViewById(R.id.nineButton));
        timeView = findViewById(R.id.timeTextView);

        mSudokuBoardView = findViewById(R.id.sudokuBoardView);
        mSudokuBoardView.registerListener(this); // Register listener for touch events on the board

        mSudokuNameLiveData.observe(this, this::saveSudokuData); // Save name changes -> save the game
        mSudokuViewModel = new ViewModelProvider(this).get(SudokuViewModel.class);
        mSudokuViewModel.sudokuGame.selectedCellLiveData.observe(this, this::updateSelectedCellUI); // Selected cell changes -> update SudokuBoardView
        mSudokuViewModel.sudokuGame.cellsLiveData.observe(this, this::updateCells); // Something in cells changes -> update SudokuBoardView
        mSudokuViewModel.sudokuGame.isBoardSolvedLiveData.observe(this, this::setSolvedState); // Board is solved -> end the game
        mSudokuViewModel.sudokuGame.isTakingNotesLiveData.observe(this, this::updateNoteTakingUI); // Note taking state changes -> change icon of notes button
        mSudokuViewModel.sudokuGame.highlightedKeysLiveData.observe(this, this::updateHighlightedKeys); // Selected cell has notes -> update highlighted keys

        // Get the difficulty from extras
        mDifficulty = (int) getIntent().getExtras().get("difficulty");
        int SAVE_STATE = 0;
        if(mDifficulty == SAVE_STATE){ // Game is a save state
            mLoadedState = getIntent().getParcelableExtra("sudokuDataObject");
            loadSaveState();
        }
        else{ // Game is a new game
            generateSudoku();
            mSudokuViewModel.sudokuGame.initializeBoard(mSudoku.board, mSudoku.solvedBoard);
            isRunning = true;
        }

        // Stopwatch
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    int hours = mSeconds / 3600;
                    int minutes = (mSeconds % 3600) / 60;
                    int secs = mSeconds % 60;

                    String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

                    timeView.setText(time);
                    mSeconds++;
                }
                mHandler.postDelayed(this, 1000);
            }
        };
        startTimer(); // Start the stopwatch
    }

    // Generate a new game with the given difficulty
    private void generateSudoku(){
        int EASY = 1;
        int MEDIUM = 2;
        int HARD = 3;
        if(mDifficulty == EASY) {
            mSudoku = Sudoku.generate(9, 40);
        }
        else if(mDifficulty == MEDIUM) {
            mSudoku = Sudoku.generate(9, 45);
        }
        else if(mDifficulty == HARD){
            mSudoku = Sudoku.generate(9, 50);
        }
    }

    // Load a save state
    private void loadSaveState() {
        mDifficulty = mLoadedState.getDifficulty(); // Difficulty
        mSeconds = mLoadedState.getSeconds(); // Elapsed time
        solveButtonPressed = mLoadedState.isSolveButtonPressed(); // Solve button pressed -> not valid for leaderboards
        if(mLoadedState.isBoardSolved()){ // Board is solved
            wasBoardSolved = true;
            int elapsed = mLoadedState.getSeconds();
            int hours = elapsed / 3600;
            int minutes = (elapsed % 3600) / 60;
            int secs = elapsed % 60;
            String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
            timeView.setText(time); // Update elapsed time to the UI
        }
        else{ // Board not solved
            isRunning = true; // Stopwatch is running
        }
        mSudokuViewModel.sudokuGame.loadSaveState(mLoadedState); // Load save in SudokuGame
    }

    // Starts the stopwatch
    private void startTimer() {
        mHandler.post(mRunnable); // Handler starts posting
    }

    // Update cells data -> board is drawn
    private void updateCells(List<Cell> cells){
        mSudokuBoardView.updateCells(cells);
    }

    // Update selected cell -> board is drawn
    private void updateSelectedCellUI(@NonNull Pair<Integer, Integer> cell){
        mSudokuBoardView.updateSelectedCellUI(cell.first, cell.second);
    }

    // Update highlighted keys
    // Key is highlighted if the selected cell contains a note of the key's number
    private void updateHighlightedKeys(List<Integer> list) {
        int i = 0;
        for(Button button : numberButtons){
            i++;
            int color;
            if(list.contains(i)){ // Notes contains the number of the key
                color = ContextCompat.getColor(this, R.color.green_noted);
            }
            else{ // Notes does not contain the number of the key
                color = ContextCompat.getColor(this, R.color.purple_500);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Note: requires API level 30 or above
                    if(getResources().getConfiguration().isNightModeActive()){ // Dark mode is on
                        color = ContextCompat.getColor(this, R.color.purple_200);
                    }
                }
            }
            button.setBackgroundColor(color);
        }
    }

    // Change icon of notes button when note taking state changes
    private void updateNoteTakingUI(Boolean isNoteTaking) {
        if(isNoteTaking != null){
            if(isNoteTaking){
                notesButton.setImageResource(R.drawable.notes_off_black);
            }
            else{
                notesButton.setImageResource(R.drawable.notes_black);
            }
        }
    }

    // Implemented SudokuBoardView.OnTouchListener - update selected cell
    @Override
    public void onCellTouched(int row, int col) {
        mSudokuViewModel.sudokuGame.updateSelectedCell(row, col);
    }

    // Handle digit button press
    public void onDigitButtonPressed(View view) {
        Button button = (Button) view;
        int num = Integer.parseInt(button.getText().toString());
        mSudokuViewModel.sudokuGame.handleInput(num);
    }

    // Handle erase button press
    public void onEraseButtonPressed(View view) {
        mSudokuViewModel.sudokuGame.handleErase();
    }

    // Handle notes button press
    public void onNotesButtonPressed(View view) {
        mSudokuViewModel.sudokuGame.changeNoteTakingState();
    }

    // Handle reset button press - reset needs confirmation
    public void onResetButtonPressed(View view) {
        confirmReset();
    }

    // Confirm reset
    private void confirmReset() {
        AlertDialog.Builder alert = new AlertDialog.Builder(GameActivity.this);
        alert.setTitle("Reset");
        alert.setMessage("Reset the board?");
        alert.setPositiveButton("Yes", (dialog, which) -> { // Yes pressed
            mSudokuViewModel.sudokuGame.resetBoard();
            mSeconds = 0;
            isRunning = true;
        });
        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()); // Cancel pressed
        alert.show();
    }

    // Handle solve board button press - invalidates solve for leaderboards
    public void onSolveBoardButtonPressed(View view) {
        if(!mSudokuViewModel.sudokuGame.getIsBoardSolved()){
            solveButtonPressed = true;
            mSudokuViewModel.sudokuGame.solveBoard();
        }
    }

    // Handle solve cell button press - invalidates solve for leaderboards
    public void onSolveCellButtonPressed(View view) {
        if(!mSudokuViewModel.sudokuGame.getIsBoardSolved()){
            solveButtonPressed = true;
            mSudokuViewModel.sudokuGame.solveCell();
        }
    }

    // Set board solved state
    // When board is solved, previous save of the game is deleted if it exists and the solved game is saved for the leaderboards
    // Note: user must be email authenticated to use the save feature.
    private void setSolvedState(boolean isBoardSolved){
        if(isBoardSolved && !wasBoardSolved){ // Board is solved and was not solved before
            isRunning = false; // Stop timer
            if(currentUser != null){
                if(currentUser.getEmail() != null){ // Current user is email authenticated
                    wasBoardSolved = true;
                    if(!solveButtonPressed){ // Solve buttons were not pressed
                        if(mLoadedState != null){ // Game was loaded from a save - delete save
                            storageReference.child(mLoadedState.getImageChildPath()).delete();
                            collectionReference.document(mLoadedState.getKey()).delete();
                            mLoadedState = null;
                        }
                        if(mSavedThisSessionState != null){ // Game was saved this session - delete save
                            storageReference.child(mSavedThisSessionState.getImageChildPath()).delete();
                            collectionReference.document(mSavedThisSessionState.getKey()).delete();
                            mSavedThisSessionState = null;
                        }
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                        s.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Helsinki")));
                        mSudokuNameLiveData.postValue(s.format(new Date())); // Save solved game for the leaderboards
                    }
                }
            }
        } else if(isBoardSolved){ // Board is solved
            isRunning = false; // Stop the stopwatch
        }
        mSudokuBoardView.setSolvedState(isBoardSolved); // Update SudokuBoardView if board is solved
    }

    // Handle save button press
    // Note: user must be email authenticated to use the save feature.
    public void onSaveButtonPressed(View view) {
        if(currentUser != null){
            if(currentUser.getEmail() != null){ // Current user is email authenticated
                if(!mSudokuViewModel.sudokuGame.getIsBoardSolved()){ // Board is not solved - solved games are saved automatically and can not be saved by the user
                    getGameNameInput(); // Ask the user for the name of the save - save game if the given name is valid
                }
                else{
                    Toast.makeText(this, "Error: Board is solved.", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Error: Unauthorized access.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Error: Unauthorized access.", Toast.LENGTH_SHORT).show();
        }
    }

    // Ask the user for the name of the save - save game if the given name is valid
    private void getGameNameInput(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Save game");
        alert.setMessage("Give your game a name!");
        final EditText input = new EditText(this);
        input.setMaxLines(1);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(20); // Max length for the save name
        input.setFilters(filterArray);
        alert.setView(input);
        alert.setPositiveButton("Ok", (dialog, whichButton) -> { // Ok pressed
            String inputStr = input.getText().toString();
            System.out.println(inputStr);
            if(!inputStr.equals("")){
                mSudokuNameLiveData.postValue(input.getText().toString()); // Valid name given -> save game
            }
            else{
                Toast.makeText(this, "Error: Saving aborted. Name was blank.", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNeutralButton("Set as timestamp", (dialog, whichButton) -> { // Set as timestamp pressed -> save game
            @SuppressLint("SimpleDateFormat") SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            s.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Helsinki")));
            mSudokuNameLiveData.postValue(s.format(new Date()));
        });
        alert.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss()); // Cancel pressed
        alert.show();
    }

    // Save the game with the given name
    private void saveSudokuData(String name) {
        if(mSavedThisSessionState != null){ // Game was saved before during this session - delete old save
            Toast.makeText(this, "Overwriting save...", Toast.LENGTH_SHORT).show();
            storageReference.child(mSavedThisSessionState.getImageChildPath()).delete();
            collectionReference.document(mSavedThisSessionState.getKey()).delete();
            mSavedThisSessionState = null;
        }
        else if(mLoadedState != null){ // Game was loaded from a save - delete old save
            Toast.makeText(this, "Overwriting save...", Toast.LENGTH_SHORT).show();
            storageReference.child(mLoadedState.getImageChildPath()).delete();
            collectionReference.document(mLoadedState.getKey()).delete();
            mLoadedState = null;
        }
        SudokuDataObject sudokuDataObject = new SudokuDataObject();
        sudokuDataObject.setUserId(currentUser.getUid());

        sudokuDataObject.setName(name);

        sudokuDataObject.setSelectedRow(mSudokuViewModel.sudokuGame.getSelectedRow());
        sudokuDataObject.setSelectedCol(mSudokuViewModel.sudokuGame.getSelectedCol());
        Board board = mSudokuViewModel.sudokuGame.getBoard();
        sudokuDataObject.setBoardSize(board.size);
        sudokuDataObject.setBoardCells(board.cells);

        sudokuDataObject.setOriginalBoard(mSudokuViewModel.sudokuGame.getOriginalBoard());
        sudokuDataObject.setSolvedBoard(mSudokuViewModel.sudokuGame.getSolvedBoard());
        sudokuDataObject.setTakingNotes(mSudokuViewModel.sudokuGame.getIsTakingNotes());
        sudokuDataObject.setBoardSolved(mSudokuViewModel.sudokuGame.getIsBoardSolved());
        sudokuDataObject.setSolveButtonPressed(solveButtonPressed);

        sudokuDataObject.setSeconds(mSeconds);
        sudokuDataObject.setDifficulty(mDifficulty);

        byte[] byteImageData = takeImageOfBoard(mSudokuBoardView); // Image of the board as byte data
        uploadImageByteData(sudokuDataObject, byteImageData); // Upload the image to FirebaseStorage - if successful, save the game to FirebaseFirestore
    }

    // Take an image of the SudokuBoardView
    // Return byte data of the image
    @NonNull
    private byte[] takeImageOfBoard(@NonNull View view){
        Bitmap boardImage = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(boardImage);
        view.draw(canvas); // Image taken
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boardImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray(); // Image as byte array
    }

    // Upload image of the board to FirebaseStorage
    // If successful, save the game to FirebaseFirestore
    private void uploadImageByteData(SudokuDataObject sudokuDataObject, byte[] data) {
        DocumentReference newDocRef = collectionReference.document();
        String childPath = "images/" + currentUser.getUid() + "/" + newDocRef.getId() + ".jpg";
        StorageReference imageRef = storageReference.child(childPath);
        UploadTask uploadTask = imageRef.putBytes(data); // Upload the image
        uploadTask.addOnFailureListener(exception -> { // Upload failed
            newDocRef.delete();
            Toast.makeText(GameActivity.this, "Error: Uploading image failed. Game not saved.", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> { // Upload successful
            sudokuDataObject.setImageChildPath(childPath);
            sudokuDataObject.setKey(newDocRef.getId());
            mSavedThisSessionState = sudokuDataObject;
            newDocRef.set(sudokuDataObject); // Game saved
            Toast.makeText(GameActivity.this, "Saved game: " + sudokuDataObject, Toast.LENGTH_SHORT).show();
        });
    }

    // Stop the stopwatch when the activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        wasRunning = isRunning;
        isRunning = false;
    }

    // Start the stopwatch if it was running previously
    @Override
    protected void onResume() {
        super.onResume();
        if (wasRunning) {
            isRunning = true;
        }
    }

    // Stop the stopwatch when exiting GameActivity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }
}