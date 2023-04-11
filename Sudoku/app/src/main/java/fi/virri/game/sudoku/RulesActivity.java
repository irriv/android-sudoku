package fi.virri.game.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class RulesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        TextView rulesTextView = findViewById(R.id.rulesTextView);
        rulesTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    // Handle back button press
    // Exit this intent
    public void onBackButtonPressed(View view) {
        finish();
    }
}