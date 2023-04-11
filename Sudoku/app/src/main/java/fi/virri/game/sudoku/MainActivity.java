package fi.virri.game.sudoku;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String COLLECTION = "users";
    private Button mAuthButton;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private ImageButton mDarkModeButton;

    // Firebase AuthUI activity launcher
    private final ActivityResultLauncher<Intent> mSignInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> {
                onSignInResult();
                // On successful sign in change authentication button text
                if(mCurrentUser != null){
                    changeAuthenticationButtonText();
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDarkModeButton = findViewById(R.id.darkModeButton);
        mAuthButton = findViewById(R.id.authenticationButton);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        if(mCurrentUser != null){
            changeAuthenticationButtonText();
        }
    }

    // Changes text of the authentication button
    private void changeAuthenticationButtonText(){
        String logInText = getResources().getString(R.string.log_in);
        if(mAuthButton.getText().toString().equals(logInText)){
            mAuthButton.setText(getResources().getString(R.string.log_out));
        }
        else{
            mAuthButton.setText(logInText);
        }
    }

    // Set the providers for Firebase AuthUI intent and launch it
    private void startSignInFlow(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.AnonymousBuilder().build());

        Intent signInIntent = AuthUI.getInstance().createSignInIntentBuilder()
                              .setAvailableProviders(providers).build();
        mSignInLauncher.launch(signInIntent);
    }

    // Handle result of Firebase AuthUI intent
    private void onSignInResult() {
        mCurrentUser = mAuth.getCurrentUser();
        if(mCurrentUser == null){
            Toast.makeText(this, "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mCurrentUser.isAnonymous()){
            Toast.makeText(this, "Signed in anonymously.", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Signed in as: " + mCurrentUser.getDisplayName(), Toast.LENGTH_SHORT).show();
            // New users are added into the 'users' collection for the purposes of leaderboards
            FirebaseFirestore.getInstance().collection(COLLECTION).addSnapshotListener((value, error) -> {
                if(value != null){
                    List<String> users = new ArrayList<>();
                    for(DocumentSnapshot snapshot : value){
                        String userId = snapshot.getString("userId");
                        if(userId != null){
                            users.add(userId);
                        }
                    }
                    if(!users.contains(mCurrentUser.getUid())){
                        Map<String,String> newUser = new HashMap<>();
                        newUser.put("userId", mCurrentUser.getUid());
                        newUser.put("displayName", mCurrentUser.getDisplayName());
                        FirebaseFirestore.getInstance().collection(COLLECTION).add(newUser);
                    }
                }
            });
        }
    }

    // Handle authentication button press
    // Sign in or sign out
    public void onAuthenticationButtonPressed(View view) {
        if(mAuthButton.getText().toString().equals(getResources().getString(R.string.log_in))){
            if(mCurrentUser == null){
                startSignInFlow();
            }
        }
        else{
            handleSignOut();
        }
    }

    // Handle play button press
    // Launch MenuActivity
    public void onPlayButtonPressed(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    // Handle rules button press
    // Launch RulesActivity
    public void onRulesButtonPressed(View view) {
        Intent intent = new Intent(this, RulesActivity.class);
        startActivity(intent);
    }

    // Handle leaderboards button press
    // Launch LeaderboardsActivity
    public void onLeaderboardsButtonPressed(View view) {
        Intent intent = new Intent(this, LeaderboardsActivity.class);
        startActivity(intent);
    }

    // Handle quit button press
    // Close the program
    public void onQuitButtonPressed(View view) {
        finishAndRemoveTask();
    }

    // Current anonymous user is deleted if still logged in
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCurrentUser != null){
            if(mCurrentUser.isAnonymous()){
                AuthUI.getInstance().delete(this);
            }
        }
    }

    // Anonymous user is deleted, users logged in with email are signed out
    // Authentication button text is changed
    private void handleSignOut(){
        if(mCurrentUser != null){
            if(mCurrentUser.isAnonymous()){
                AuthUI.getInstance().delete(this)
                .addOnCompleteListener(task -> Toast.makeText(this,"Signed out. Guest account deleted.", Toast.LENGTH_SHORT).show());
            }
            else{
                AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(task -> Toast.makeText(this,"Signed out.", Toast.LENGTH_SHORT).show());
            }
            mCurrentUser = null;
            changeAuthenticationButtonText();
        }
    }

    // Handle dark mode button press
    // Note: requires API level 30 or above
    public void onDarkModeButtonPressed(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!getResources().getConfiguration().isNightModeActive()){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // Set night mode on
                mDarkModeButton.setImageResource(R.drawable.light_mode_white);
            }
            else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Set night mode off
                mDarkModeButton.setImageResource(R.drawable.dark_mode_white);
            }
        }
    }
}