package fi.virri.game.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fi.virri.game.sudoku.database.SudokuDataObject;
import fi.virri.game.sudoku.database.SudokuDataObjectListAdapter;

public class LoadSudokuActivity extends AppCompatActivity {
    private String COLLECTION_NAME;
    private final String BUCKET_NAME = "gs://" + FirebaseApp.getInstance().getOptions().getStorageBucket();
    private StorageReference storageReference;
    private CollectionReference collectionReference;
    private SudokuDataObjectListAdapter mSudokuDataObjectListAdapter;
    private List<SudokuDataObject> sudokuDataObjectList;
    private ListView mSudokuDataObjectListView;
    private FirebaseFirestore mFirestore;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_sudoku);

        // Retrieve current user's saved games from the cloud
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            if(currentUser.getEmail() != null){
                COLLECTION_NAME = currentUser.getUid();
                mFirestore = FirebaseFirestore.getInstance();
                collectionReference = mFirestore.collection(COLLECTION_NAME);
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(BUCKET_NAME);
                mSudokuDataObjectListView = findViewById(R.id.listView);
                sudokuDataObjectList = new ArrayList<>();
                makeQuery();
            }
        }
    }

    // Retrieve saved games from the cloud
    public void makeQuery(){
        mFirestore.collection(COLLECTION_NAME).whereEqualTo("userId",
                Objects.requireNonNull(currentUser).getUid()).addSnapshotListener((queryDocumentSnapshots, e) -> {
                    sudokuDataObjectList.clear();
                    if(queryDocumentSnapshots!=null){
                        for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                            SudokuDataObject sudokuDataObject = snapshot.toObject(SudokuDataObject.class);
                            sudokuDataObjectList.add(sudokuDataObject);

                        }
                        mSudokuDataObjectListAdapter = new SudokuDataObjectListAdapter(
                                LoadSudokuActivity.this, R.layout.item_sudoku_data_object, sudokuDataObjectList);
                        mSudokuDataObjectListAdapter.notifyDataSetChanged();
                        mSudokuDataObjectListView.setAdapter(mSudokuDataObjectListAdapter);
                    }
                });
    }

    // Handle load save button press
    // Load the selected save
    public void onLoadSaveButtonPressed(int selectedPosition) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("sudokuDataObject", sudokuDataObjectList.get(selectedPosition));
        int SAVE_STATE = 0;
        intent.putExtra("difficulty", SAVE_STATE);
        startActivity(intent);
        finish();
    }

    // Handle delete save button press
    // Delete the selected save, needs confirmation
    public void onDeleteSaveButtonPressed(int selectedPosition) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete game");
        alert.setMessage("Are you sure?");
        alert.setPositiveButton("Yes", (dialog, whichButton) -> {
            SudokuDataObject sudokuDataObject = sudokuDataObjectList.get(selectedPosition);
            storageReference.child(sudokuDataObject.getImageChildPath()).delete();
            collectionReference.document(sudokuDataObject.getKey()).delete();
        });
        alert.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());
        alert.show();
    }

    // Handle delete all button press
    // Delete all saves of the current user, needs confirmation
    public void onDeleteAllButtonPressed(View view){
        if(sudokuDataObjectList.isEmpty()){
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete all games");
        alert.setMessage("Are you sure?");
        alert.setPositiveButton("Yes", (dialog, whichButton) -> {
            for(SudokuDataObject sudokuDataObject : sudokuDataObjectList){
                storageReference.child(sudokuDataObject.getImageChildPath()).delete();
                collectionReference.document(sudokuDataObject.getKey()).delete();
            }
        });
        alert.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());
        alert.show();
    }

    // Handle back button press
    // Exit this intent
    public void onBackButtonPressed(View view) {
        finish();
    }
}