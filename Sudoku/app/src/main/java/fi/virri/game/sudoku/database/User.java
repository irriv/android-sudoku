package fi.virri.game.sudoku.database;

import androidx.annotation.NonNull;

public class User implements Comparable<User> {
    public String ID;
    public String displayName;
    public int rank;
    public int solvedEasy = 0;
    public int solvedMedium = 0;
    public int solvedHard = 0;
    public int seconds = Integer.MAX_VALUE;
    public String time = "- : -- : -- ";

    // Highscores are compared primarily with weighted solves, secondarily with fastest solve time
    @Override
    public int compareTo(@NonNull User user) {
        int solvedThis  = this.solvedEasy + this.solvedMedium*2 + this.solvedHard*3;
        int solvedOther = user.solvedEasy + user.solvedMedium*2 + user.solvedHard*3;
        if(solvedOther - solvedThis  != 0){
            return solvedOther - solvedThis;
        }
        else {
            return this.seconds - user.seconds;
        }
    }
}
