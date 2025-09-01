# Android Sudoku Game
This is a Sudoku game for Android integrating Firebase.
## Demo Video
https://github.com/user-attachments/assets/4cf0f582-164e-45b8-a69b-a52191cf3125

Players can solve randomly generated Sudoku boards based on the chosen difficulty. 
Users logged in by email have access to saving/loading gamestates. 
Only a single instance of the same generated Sudoku game can appear in the database. 
This means that any subsequent saves of the game overwrite the existing savestate of the game. 
Finally, when the user solves a board without the use of helper tools in the app, 
the solved game is saved and overwrites any previous save of the game. 
Saved, solved games appear on the leaderboards. 
On the leaderboards users are ranked based primarily on the amount of solved sudokus
 and secondarily on the fastest solve time. 
Users and gamestates are stored into the app's cloud database in Firebase.
