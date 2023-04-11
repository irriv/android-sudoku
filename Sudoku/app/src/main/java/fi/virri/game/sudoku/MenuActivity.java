package fi.virri.game.sudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    // Handle Easy, Medium and Hard button presses
    // Launch GameActivity with selected difficulty
    public void startGame(@NonNull View view) {
        int difficulty;
        switch (view.getId()) {
            case (R.id.easyButton):
                difficulty = 1;
                break;
            case (R.id.mediumButton):
                difficulty = 2;
                break;
            case (R.id.hardButton):
                difficulty = 3;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
        Toast.makeText(this, "Generating game", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }

    // Handle load button press
    // Launch LoadSudokuActivity
    // Note: only email authenticated users have access to loading games feature
    public void onLoadButtonPressed(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            if(currentUser.getEmail() != null){
                Intent intent = new Intent(this, LoadSudokuActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(this, "Error: Unauthorized access.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Error: Unauthorized access.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle back button press
    // Exit this intent
    public void onBackButtonPressed(View view) {
        finish();
    }

}