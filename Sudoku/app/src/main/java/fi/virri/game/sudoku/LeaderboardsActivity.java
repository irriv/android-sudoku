package fi.virri.game.sudoku;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import fi.virri.game.sudoku.database.SudokuDataObject;
import fi.virri.game.sudoku.database.User;
import fi.virri.game.sudoku.database.UserListAdapter;

public class LeaderboardsActivity extends AppCompatActivity {
    private UserListAdapter mUserListAdapter;
    private FirebaseFirestore mFirestore;
    private List<Pair<String,String>> foundUsers;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);
        mFirestore = FirebaseFirestore.getInstance();
        foundUsers = new ArrayList<>();
        users = new ArrayList<>();
        ListView highscoreListView = findViewById(R.id.highscoreListView);
        mUserListAdapter = new UserListAdapter(this, R.layout.item_highscore, users);
        highscoreListView.setAdapter(mUserListAdapter);
        getAllUserScores();
    }

    // Retrieve all email authenticated users of the program and start querying scores after all users are retrieved
    private void getAllUserScores(){
        mFirestore.collection("users").addSnapshotListener((value, error) -> {
            if(value != null){
                for(DocumentSnapshot snapshot : value){
                    String userId = snapshot.getString("userId");
                    String displayName = snapshot.getString("displayName");
                    if(userId != null && displayName != null){
                        foundUsers.add(Pair.create(userId,displayName));
                    }
                }
                query(); // Start querying games after all users have been found
            }
        });
    }

    // Retrieve all saved games of all users
    // Create User data object for every user - effectively a highscore object
    // Show highscores when done
    public void query(){
        int lastUser = foundUsers.size()-1;
        for(int i=0; i<foundUsers.size(); i++){
            Pair<String,String> userIdNamePair = foundUsers.get(i);
            int userIndex = i;
            mFirestore.collection(userIdNamePair.first).addSnapshotListener((value, error) -> {
                if(value != null){
                    User user = new User();
                    user.ID = userIdNamePair.first;
                    user.displayName = userIdNamePair.second;
                    for(DocumentSnapshot snapshot : value){
                        SudokuDataObject sudokuDataObject = snapshot.toObject(SudokuDataObject.class);
                        assert sudokuDataObject != null;
                        if(sudokuDataObject.isBoardSolved()){
                            int difficulty = sudokuDataObject.getDifficulty();
                            if(difficulty == 1){
                                user.solvedEasy++;
                            }
                            else if(difficulty == 2){
                                user.solvedMedium++;
                            }
                            else{
                                user.solvedHard++;
                            }
                            if(sudokuDataObject.getSeconds() < user.seconds){
                                user.seconds = sudokuDataObject.getSeconds();
                                int hours = user.seconds / 3600;
                                int minutes = (user.seconds % 3600) / 60;
                                int secs = user.seconds % 60;
                                user.time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                            }
                        }
                    }
                    users.add(user);
                    if(userIndex == lastUser){ // Queries of all games of all users finished
                        Collections.sort(users); // Sort list
                        for (int j = 0; j < users.size(); j++) { // Assign ranks
                            users.get(j).rank = j+1;
                        }
                        mUserListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    // Handle back button press
    // Exit this intent
    public void onBackButtonPressed(View view) {
        finish();
    }
}