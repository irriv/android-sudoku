package fi.virri.game.sudoku.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.List;

public class SudokuBoardView extends View {
    private final Paint thickLinePaint = new Paint(); // Paint to differentiate sub-grids
    private final Paint thinLinePaint = new Paint(); // Paint to differentiate cells
    private final Paint selectedCellPaint = new Paint(); // Paint for selected cell
    private final Paint conflictingCellPaint = new Paint(); // Paint for conflicting cells of selected cell
    private final Paint startingCellPaint = new Paint(); // Paint for starting cells
    private final Paint emptyCellPaint = new Paint(); // Paint for empty cells (only needed to display images in saves correctly)
    private final Paint textPaint = new Paint(); // Paint for digits
    private final Paint noteTextPaint = new Paint(); // Paint for notes

    private final int sqrtSize = 3; // Sub-grid row/column amount
    private final int size = 9; // Board row/column amount

    private float cellSizePixels = 0f; // Size of a cell in pixels (derived when board is drawn)
    private float noteSizePixels = 0f; // Size of a note in pixels (derived when board is drawn)
    private int selectedRow = 0;
    private int selectedCol = 0;
    private boolean isBoardSolved = false;

    private SudokuBoardView.OnTouchListener listener;

    private List<Cell> cells;

    public SudokuBoardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        thickLinePaint.setStyle(Paint.Style.STROKE);
        thickLinePaint.setColor(Color.BLACK);
        thickLinePaint.setStrokeWidth(4f);

        thinLinePaint.setStyle(Paint.Style.STROKE);
        thinLinePaint.setColor(Color.BLACK);
        thinLinePaint.setStrokeWidth(2f);

        selectedCellPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        selectedCellPaint.setColor(Color.parseColor("#64e764"));

        conflictingCellPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        conflictingCellPaint.setColor(Color.parseColor("#d4f8d4"));

        startingCellPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        startingCellPaint.setColor(Color.parseColor("#d4ced4"));

        emptyCellPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        emptyCellPaint.setColor(Color.parseColor("#ffffff"));

        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.BLACK);

        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.BLACK);
    }

    // Draw the board - invalidate() calls this
    protected void onDraw(Canvas canvas) {
        updateMeasurements(getWidth());
        fillCells(canvas);
        drawLines(canvas);
        drawText(canvas);
    }

    // Update measurements of board components
    private void updateMeasurements(int width) {
        cellSizePixels = (float) width/(float) size;
        noteSizePixels = cellSizePixels / (float) sqrtSize; // Notes form their own sub-grid in a cell
        this.getLayoutParams().height = (int) (size*cellSizePixels); // Adjust height of the board
        textPaint.setTextSize(cellSizePixels/1.5f);
        noteTextPaint.setTextSize(cellSizePixels/(float) sqrtSize);
    }

    // Fill all cells with correct paints
    private void fillCells(Canvas canvas) {
        if(cells == null){
            return;
        }
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            int row = cell.row;
            int col = cell.col;
            if(isBoardSolved){ // Board solved
                fillCell(canvas, row, col, selectedCellPaint);
            }
            else if(cell.isStartingCell){ // Starting cell
                fillCell(canvas, row, col, startingCellPaint);
            }
            else if(row == selectedRow && col == selectedCol){ // Selected cell
                fillCell(canvas, row, col, selectedCellPaint);
            }
            else if(row == selectedRow || col == selectedCol){ // Conflicting cell in row/column
                fillCell(canvas, row, col, conflictingCellPaint);
            }
            else if(row/sqrtSize == selectedRow/sqrtSize && col/sqrtSize == selectedCol/sqrtSize){ // Conflicting cell in sub-grid
                if(selectedRow != -1 && selectedCol != -1){ // Selection exists
                    fillCell(canvas, row, col, conflictingCellPaint);
                }
                else{
                    fillCell(canvas, row, col, emptyCellPaint);
                }
            }
            else{
                fillCell(canvas, row, col, emptyCellPaint); // Empty cells - player can modify the digit/notes
            }
        }
    }

    // Fill a given cell with a given paint
    private void fillCell(@NonNull Canvas canvas, int row, int col, Paint paint){
        canvas.drawRect(col*cellSizePixels, row*cellSizePixels,
                (col+1)*cellSizePixels, (row+1)*cellSizePixels, paint);
    }

    // Draw lines of the board
    private void drawLines(@NonNull Canvas canvas){
        canvas.drawRect(0F, 0F, (float) getWidth(), (float) this.getLayoutParams().height, thickLinePaint); // Outline of board
        for(int i=1; i<size; i++){
            Paint toUse;
            if(i%sqrtSize == 0){ // Sub-grid differentiating line
                toUse = thickLinePaint;
            }
            else{ // Cell differentiating line
                toUse = thinLinePaint;
            }
            float pos = i*cellSizePixels;
            canvas.drawLine(pos, 0f, pos, (float) this.getLayoutParams().height, toUse); // Vertical line
            canvas.drawLine(0f, pos, (float) getWidth(), pos, toUse); // Horizontal line
        }
    }

    // Draw digits and notes
    private void drawText(Canvas canvas){
        if(cells == null){
            return;
        }
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            int value = cell.value;
            if(value == 0){ // Empty cell -> draw notes
                for (Integer note : cell.notes) {
                    int rowInCell = (note-1)/sqrtSize;
                    int colInCell = (note-1)%sqrtSize;
                    String valueStr = note.toString();

                    Rect textBounds = new Rect();
                    noteTextPaint.getTextBounds(valueStr, 0, valueStr.length(), textBounds);
                    float textWidth = noteTextPaint.measureText(valueStr);
                    float textHeight = textBounds.height();

                    canvas.drawText(valueStr,
                            (cell.col * cellSizePixels) + (colInCell * noteSizePixels) + noteSizePixels / 2 - textWidth / 2f,
                            (cell.row * cellSizePixels) + (rowInCell * noteSizePixels) + noteSizePixels / 2 + textHeight / 2f,
                            noteTextPaint);
                }
            }
            else{ // Non-empty cell -> draw values
                String valueStr = String.valueOf(value);
                Rect textBounds = new Rect();
                textPaint.getTextBounds(valueStr, 0, valueStr.length(), textBounds);
                float textWidth = textPaint.measureText(valueStr);
                float textHeight = textBounds.height();

                canvas.drawText(valueStr, (cell.col*cellSizePixels) + cellSizePixels/2 - textWidth/2,
                        (cell.row*cellSizePixels) + cellSizePixels/2 + textHeight/2, textPaint);
            }
        }
    }

    // Touch event handler
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(@NonNull MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            int possibleSelectedRow = (int) (event.getY() / cellSizePixels);
            int possibleSelectedCol = (int) (event.getX() / cellSizePixels);
            listener.onCellTouched(possibleSelectedRow, possibleSelectedCol);
            return true;
        }
        return false;
    }

    // Selected cell changed -> Draw board
    public void updateSelectedCellUI(int row, int col){
        selectedRow = row;
        selectedCol = col;
        invalidate();
    }

    // Something in cells changed -> Draw board
    public void updateCells(List<Cell> cells){
        this.cells = cells;
        invalidate();
    }

    // Set board solved state
    public void setSolvedState(boolean isBoardSolved){
        if(this.isBoardSolved == isBoardSolved){ // Win state not changed
            return;
        }
        this.isBoardSolved = isBoardSolved; // Win state changed
        invalidate();
    }

    // Register touch event listener
    public void registerListener(SudokuBoardView.OnTouchListener listener){
        this.listener = listener;
    }

    // Touch event listener interface - implemented by GameActivity - updates selected cell
    public interface OnTouchListener {
        void onCellTouched(int row, int col);
    }
}
