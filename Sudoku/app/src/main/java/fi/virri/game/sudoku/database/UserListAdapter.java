package fi.virri.game.sudoku.database;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import fi.virri.game.sudoku.R;

public class UserListAdapter extends ArrayAdapter<User> {

    public UserListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
    }

    // Convert User into a presentable highscore - listView in LeaderboardsActivity
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.item_highscore, parent, false);
        }
        TextView rankTextView = convertView.findViewById(R.id.rankTextView);
        TextView nameTextView = convertView.findViewById(R.id.userNameTextView);
        TextView solvedTextView = convertView.findViewById(R.id.solvedTextView);
        TextView timeTextView = convertView.findViewById(R.id.fastestTimeTextView);
        User user = getItem(position);
        rankTextView.setText(String.valueOf(user.rank)); // Rank
        nameTextView.setText(user.displayName); // Name
        String solvedText = user.solvedEasy + " / " + user.solvedMedium + " / " + user.solvedHard; // Solve count or 'points'
        solvedTextView.setText(solvedText);
        timeTextView.setText(user.time); // Fastest solve time
        return convertView;
    }
}
