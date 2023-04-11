package fi.virri.game.sudoku.database;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import fi.virri.game.sudoku.LoadSudokuActivity;
import fi.virri.game.sudoku.R;

public class SudokuDataObjectListAdapter extends ArrayAdapter<SudokuDataObject> {
    private final String BUCKET_NAME = "gs://" + FirebaseApp.getInstance().getOptions().getStorageBucket();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(BUCKET_NAME);
    private final Context mContext;

    public SudokuDataObjectListAdapter(Context context, int resource, List<SudokuDataObject> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    // Convert SudokuDataObject into a presentable save - listView in LoadSudokuActivity
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.item_sudoku_data_object, parent, false);
        }
        ImageView sudokuBoardImageView = convertView.findViewById(R.id.sudokuBoardImageView);
        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView difficultyTextView = convertView.findViewById(R.id.difficultyTextView);

        // Image of the board
        SudokuDataObject sudokuDataObject = getItem(position);
        StorageReference imageRef = storageRef.child(sudokuDataObject.getImageChildPath());
        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> sudokuBoardImageView.
                        setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length))).
                addOnFailureListener(exception -> Log.d("ArrayAdapter", "Downloading image failed."));

        // Name of the save
        nameTextView.setText(String.valueOf(sudokuDataObject.getName()));

        // Difficulty of save
        String difficultyStr = "Difficulty";
        int difficulty = sudokuDataObject.getDifficulty();
        if(difficulty == 1){
            difficultyStr = "Easy";
        }
        else if(difficulty == 2){
            difficultyStr = "Medium";
        }
        else if(difficulty == 3){
            difficultyStr = "Hard";
        }
        difficultyTextView.setText(difficultyStr);

        // Set button handlers
        Button loadSaveButton = convertView.findViewById(R.id.loadSaveButton);
        Button deleteSaveButton = convertView.findViewById(R.id.deleteSaveButton);
        loadSaveButton.setOnClickListener(view -> {
            if(mContext instanceof LoadSudokuActivity){
                ((LoadSudokuActivity)mContext).onLoadSaveButtonPressed(position);
            }
        });
        deleteSaveButton.setOnClickListener(view -> {
            if(mContext instanceof LoadSudokuActivity){
                ((LoadSudokuActivity)mContext).onDeleteSaveButtonPressed(position);
            }
        });

        return convertView;
    }
}
