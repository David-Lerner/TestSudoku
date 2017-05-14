package com.example.david.testsudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.david.completesudoku.SudokuGame;
import com.david.completesudoku.SudokuGame.OnChangeListener;

/**
 * Sudoku board widget.
 *
 */
public class SudokuBoardView extends View {

    public static final int DEFAULT_BOARD_SIZE = 100;

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

    /**
     * "Color not set" value. (In relation to {@link Color}, it is in fact black color with
     * alpha channel set to 0 => that means it is completely transparent).
     */
    private static final int NO_COLOR = 0;

    private SudokuGame sudokuGame;
    private CellTile[][] cells;

    private float mCellWidth;
    private float mCellHeight;

    private CellTile mTouchedCell;
    //maybe synchronize access
    private CellTile mSelectedCell;

    private boolean mReadonly = false;
    private boolean mHighlightWrongVals = true;
    private boolean mHighlightTouchedCell = true;
    private boolean mAutoHideTouchedCellHint = true;

    private OnCellTappedListener mOnCellTappedListener;
    private OnCellSelectedListener mOnCellSelectedListener;

    private Paint mLinePaint;
    private Paint mSectorLinePaint;
    private Paint mCellValuePaint;
    private Paint mCellValueReadonlyPaint;
    private Paint mCellNotePaint;
    private int mNumberLeft;
    private int mNumberTop;
    private float mNoteTop;
    private int mSectorLineWidth;
    private Paint mBackgroundColorSecondary;
    private Paint mBackgroundColorReadOnly;
    private Paint mBackgroundColorTouched;
    private Paint mBackgroundColorSelected;
    private Paint highlightedPaint;

    private Paint mCellValueInvalidPaint;

    private long time;
    boolean firstTouch;

    public SudokuBoardView(Context context) {
        this(context, null);
    }

    public SudokuBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setFocusable(true);
        setFocusableInTouchMode(true);

        mLinePaint = new Paint();
        mSectorLinePaint = new Paint();
        mCellValuePaint = new Paint();
        mCellValueReadonlyPaint = new Paint();
        mCellValueInvalidPaint = new Paint();
        mCellNotePaint = new Paint();
        mBackgroundColorSecondary = new Paint();
        mBackgroundColorReadOnly = new Paint();
        mBackgroundColorTouched = new Paint();
        mBackgroundColorSelected = new Paint();
        highlightedPaint = new Paint();

        mCellValuePaint.setAntiAlias(true);
        mCellValueReadonlyPaint.setAntiAlias(true);
        mCellValueInvalidPaint.setAntiAlias(true);
        mCellNotePaint.setAntiAlias(true);
        mCellValueInvalidPaint.setColor(Color.RED);

        setLineColor(Color.DKGRAY);
        setSectorLineColor(Color.BLACK);
        setTextColor(Color.BLACK);
        setTextColorReadOnly(Color.BLUE);
        setTextColorNote(Color.BLACK);
        setBackgroundColor(Color.LTGRAY);
        setBackgroundColorSecondary(Color.WHITE);
        setBackgroundColorReadOnly(NO_COLOR);
        setBackgroundColorTouched(Color.rgb(50, 50, 255));
        setBackgroundColorSelected(Color.CYAN);
        setHighlightedPaint(Color.YELLOW);

        time = System.currentTimeMillis();
        firstTouch = false;
    }

    public int getLineColor() {
        return mLinePaint.getColor();
    }

    public void setLineColor(int color) {
        mLinePaint.setColor(color);
    }

    public int getSectorLineColor() {
        return mSectorLinePaint.getColor();
    }

    public void setSectorLineColor(int color) {
        mSectorLinePaint.setColor(color);
    }

    public int getTextColor() {
        return mCellValuePaint.getColor();
    }

    public void setTextColor(int color) {
        mCellValuePaint.setColor(color);
    }

    public int getTextColorReadOnly() {
        return mCellValueReadonlyPaint.getColor();
    }

    public void setTextColorReadOnly(int color) {
        mCellValueReadonlyPaint.setColor(color);
    }

    public int getTextColorNote() {
        return mCellNotePaint.getColor();
    }

    public void setTextColorNote(int color) {
        mCellNotePaint.setColor(color);
    }

    public int getBackgroundColorSecondary() {
        return mBackgroundColorSecondary.getColor();
    }

    public void setBackgroundColorSecondary(int color) {
        mBackgroundColorSecondary.setColor(color);
    }

    public int getBackgroundColorReadOnly() {
        return mBackgroundColorReadOnly.getColor();
    }

    public void setBackgroundColorReadOnly(int color) {
        mBackgroundColorReadOnly.setColor(color);
    }

    public int getBackgroundColorTouched() {
        return mBackgroundColorTouched.getColor();
    }

    public void setBackgroundColorTouched(int color) {
        mBackgroundColorTouched.setColor(color);
        mBackgroundColorTouched.setAlpha(100);
    }

    public int getBackgroundColorSelected() {
        return mBackgroundColorSelected.getColor();
    }

    public void setBackgroundColorSelected(int color) {
        mBackgroundColorSelected.setColor(color);
        mBackgroundColorSelected.setAlpha(100);
    }

    public Paint getHighlightedPaint() {
        return highlightedPaint;
    }

    public void setHighlightedPaint(int color) {
        highlightedPaint.setColor(color);
        highlightedPaint.setAlpha(100);
    }

    public void setSudokuGame(SudokuGame game) {
        sudokuGame = game;

        if (sudokuGame != null) {
            cells = new CellTile[sudokuGame.getLength()][sudokuGame.getLength()];
            for (int i = 0; i < sudokuGame.getLength(); i++) {
                for (int j = 0; j < sudokuGame.getLength(); j++) {
                    cells[i][j] = new CellTile(i, j);
                }
            }

            if (!mReadonly) {
                mSelectedCell = cells[0][0]; // first cell will be selected by default
                onCellSelected(mSelectedCell);
            }

            sudokuGame.addOnChangeListener(new OnChangeListener() {
                @Override
                public void onChange() {
                    postInvalidate();
                }
            });
        }

        postInvalidate();
    }

    public SudokuGame getSudokuGame() {
        return sudokuGame;
    }

    public void setTarget(int target) {
        if (cells != null && !mReadonly) {
            mSelectedCell = cells[target/sudokuGame.getLength()][target%sudokuGame.getLength()];
        }
    }

    public int getTarget() {
        return mSelectedCell.getRow()*sudokuGame.getLength()+mSelectedCell.getCol();
    }

    public CellTile getSelectedCell() {
        return mSelectedCell;
    }

    public void setReadOnly(boolean readonly) {
        mReadonly = readonly;
        postInvalidate();
    }

    public boolean isReadOnly() {
        return mReadonly;
    }

    public void setHighlightWrongVals(boolean highlightWrongVals) {
        mHighlightWrongVals = highlightWrongVals;
        postInvalidate();
    }

    public boolean getHighlightWrongVals() {
        return mHighlightWrongVals;
    }

    public void setHighlightTouchedCell(boolean highlightTouchedCell) {
        mHighlightTouchedCell = highlightTouchedCell;
    }

    public boolean getHighlightTouchedCell() {
        return mHighlightTouchedCell;
    }

    public void setAutoHideTouchedCellHint(boolean autoHideTouchedCellHint) {
        mAutoHideTouchedCellHint = autoHideTouchedCellHint;
    }

    public boolean getAutoHideTouchedCellHint() {
        return mAutoHideTouchedCellHint;
    }

    /**
     * Registers callback which will be invoked when user taps the cell.
     *
     * @param l
     */
    public void setOnCellTappedListener(OnCellTappedListener l) {
        mOnCellTappedListener = l;
    }

    protected void onCellTapped(CellTile cell) {
        if (mOnCellTappedListener != null) {
            mOnCellTappedListener.onCellTapped(cell);
        }
    }

    /**
     * Registers callback which will be invoked when cell is selected. Cell selection
     * can change without user interaction.
     *
     * @param l
     */
    public void setOnCellSelectedListener(OnCellSelectedListener l) {
        mOnCellSelectedListener = l;
    }

    public void hideTouchedCellHint() {
        mTouchedCell = null;
        postInvalidate();
    }


    protected void onCellSelected(CellTile cell) {
        if (mOnCellSelectedListener != null) {
            mOnCellSelectedListener.onCellSelected(cell);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


//        Log.d(TAG, "widthMode=" + getMeasureSpecModeString(widthMode));
//        Log.d(TAG, "widthSize=" + widthSize);
//        Log.d(TAG, "heightMode=" + getMeasureSpecModeString(heightMode));
//        Log.d(TAG, "heightSize=" + heightSize);

        int width = -1, height = -1;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = DEFAULT_BOARD_SIZE;
            if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
                width = widthSize;
            }
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = DEFAULT_BOARD_SIZE;
            if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
                height = heightSize;
            }
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            width = height;
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            height = width;
        }

        if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
            width = widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
            height = heightSize;
        }

        mCellWidth = (width - getPaddingLeft() - getPaddingRight()) / 9.0f;
        mCellHeight = (height - getPaddingTop() - getPaddingBottom()) / 9.0f;

        setMeasuredDimension(width, height);

        float cellTextSize = mCellHeight * 0.75f;
        mCellValuePaint.setTextSize(cellTextSize);
        mCellValueReadonlyPaint.setTextSize(cellTextSize);
        mCellValueInvalidPaint.setTextSize(cellTextSize);
        mCellNotePaint.setTextSize(mCellHeight / 3.0f);
        // compute offsets in each cell to center the rendered number
        mNumberLeft = (int) ((mCellWidth - mCellValuePaint.measureText("9")) / 2);
        mNumberTop = (int) ((mCellHeight - mCellValuePaint.getTextSize()) / 2);

        // add some offset because in some resolutions notes are cut-off in the top
        mNoteTop = mCellHeight / 50.0f;

        computeSectorLineWidth(width, height);
    }

    private void computeSectorLineWidth(int widthInPx, int heightInPx) {
        int sizeInPx = widthInPx < heightInPx ? widthInPx : heightInPx;
        float dipScale = getContext().getResources().getDisplayMetrics().density;
        float sizeInDip = sizeInPx / dipScale;

        float sectorLineWidthInDip = 2.0f;

        if (sizeInDip > 150) {
            sectorLineWidthInDip = 3.0f;
        }

        mSectorLineWidth = (int) (sectorLineWidthInDip * dipScale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // some notes:
        // Drawable has its own draw() method that takes your Canvas as an arguement

        int width = getWidth() - getPaddingRight();
        int height = getHeight() - getPaddingBottom();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        // draw secondary background
        if (mBackgroundColorSecondary.getColor() != NO_COLOR) {
            canvas.drawRect(3 * mCellWidth, 0, 6 * mCellWidth, 3 * mCellWidth, mBackgroundColorSecondary);
            canvas.drawRect(0, 3 * mCellWidth, 3 * mCellWidth, 6 * mCellWidth, mBackgroundColorSecondary);
            canvas.drawRect(6 * mCellWidth, 3 * mCellWidth, 9 * mCellWidth, 6 * mCellWidth, mBackgroundColorSecondary);
            canvas.drawRect(3 * mCellWidth, 6 * mCellWidth, 6 * mCellWidth, 9 * mCellWidth, mBackgroundColorSecondary);
        }

        // draw cells
        int cellLeft, cellTop;
        if (sudokuGame != null) {

            boolean hasBackgroundColorReadOnly = mBackgroundColorReadOnly.getColor() != NO_COLOR;

            float numberAscent = mCellValuePaint.ascent();
            float noteAscent = mCellNotePaint.ascent();
            float noteWidth = mCellWidth / 3f;
            for (int row = 0; row < sudokuGame.getLength(); row++) {
                for (int col = 0; col < sudokuGame.getLength(); col++) {
                    CellTile cell = cells[row][col];

                    cellLeft = Math.round((col * mCellWidth) + paddingLeft);
                    cellTop = Math.round((row * mCellHeight) + paddingTop);

                    // draw read-only field background
                    if (sudokuGame.isGiven(cell.getRow(), cell.getCol()) && hasBackgroundColorReadOnly) {
                        if (mBackgroundColorReadOnly.getColor() != NO_COLOR) {
                            canvas.drawRect(
                                    cellLeft, cellTop,
                                    cellLeft + mCellWidth, cellTop + mCellHeight,
                                    mBackgroundColorReadOnly);
                        }
                    }

                    //draw highlighted background
                    if (sudokuGame.isHighlighted(cell.getRow(),cell.getCol())) {
                        if (highlightedPaint.getColor() != NO_COLOR) {
                            canvas.drawRect(
                                    cellLeft, cellTop,
                                    cellLeft + mCellWidth, cellTop + mCellHeight,
                                    highlightedPaint);
                        }
                    }

                    // draw cell Text
                    int value = sudokuGame.getValue(cell.getRow(), cell.getCol());
                    if (value != 0) {
                        Paint cellValuePaint = mCellValuePaint;

                        if (sudokuGame.isErrorShownInCell(cell.getRow(), cell.getCol()) ||
                                (mHighlightWrongVals && !sudokuGame.isValid(cell.getRow(), cell.getCol()))) {
                            cellValuePaint = mCellValueInvalidPaint;
                        }
                        if (sudokuGame.isGiven(cell.getRow(), cell.getCol())) {
                            cellValuePaint = mCellValueReadonlyPaint;
                        }
                        canvas.drawText(Integer.toString(value),
                                cellLeft + mNumberLeft,
                                cellTop + mNumberTop - numberAscent,
                                cellValuePaint);
                    } else {
                        if (sudokuGame.getPossibilityCount(cell.getRow(), cell.getCol()) > 0) {
                            for (int n = 1; n <= 9; n++) {
                                if (sudokuGame.containsPossibility(cell.getRow(), cell.getCol(), n)) {
                                    int number = n - 1;
                                    int c = number % 3;
                                    int r = number / 3;
                                    //canvas.drawText(Integer.toString(number), cellLeft + c*noteWidth + 2, cellTop + noteAscent + r*noteWidth - 1, mNotePaint);
                                    canvas.drawText(Integer.toString(n), cellLeft + c * noteWidth + 2, cellTop + mNoteTop - noteAscent + r * noteWidth - 1, mCellNotePaint);
                                }
                            }
                        }
                    }


                }
            }


            // highlight selected cell
            if (!mReadonly && mSelectedCell != null) {
                cellLeft = Math.round(mSelectedCell.getCol() * mCellWidth) + paddingLeft;
                cellTop = Math.round(mSelectedCell.getRow() * mCellHeight) + paddingTop;
                canvas.drawRect(
                        cellLeft, cellTop,
                        cellLeft + mCellWidth, cellTop + mCellHeight,
                        mBackgroundColorSelected);
            }

            // visually highlight cell under the finger (to cope with touch screen
            // imprecision)
            if (mHighlightTouchedCell && mTouchedCell != null) {
                cellLeft = Math.round(mTouchedCell.getCol() * mCellWidth) + paddingLeft;
                cellTop = Math.round(mTouchedCell.getRow() * mCellHeight) + paddingTop;
                canvas.drawRect(
                        cellLeft, paddingTop,
                        cellLeft + mCellWidth, height,
                        mBackgroundColorTouched);
                canvas.drawRect(
                        paddingLeft, cellTop,
                        width, cellTop + mCellHeight,
                        mBackgroundColorTouched);
            }

        }

        // draw vertical lines
        for (int c = 0; c <= 9; c++) {
            float x = (c * mCellWidth) + paddingLeft;
            canvas.drawLine(x, paddingTop, x, height, mLinePaint);
        }

        // draw horizontal lines
        for (int r = 0; r <= 9; r++) {
            float y = r * mCellHeight + paddingTop;
            canvas.drawLine(paddingLeft, y, width, y, mLinePaint);
        }

        int sectorLineWidth1 = mSectorLineWidth / 2;
        int sectorLineWidth2 = sectorLineWidth1 + (mSectorLineWidth % 2);

        // draw sector (thick) lines
        for (int c = 0; c <= 9; c = c + 3) {
            float x = (c * mCellWidth) + paddingLeft;
            canvas.drawRect(x - sectorLineWidth1, paddingTop, x + sectorLineWidth2, height, mSectorLinePaint);
        }

        for (int r = 0; r <= 9; r = r + 3) {
            float y = r * mCellHeight + paddingTop;
            canvas.drawRect(paddingLeft, y - sectorLineWidth1, width, y + sectorLineWidth2, mSectorLinePaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!mReadonly) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(event.getAction() == event.ACTION_DOWN){
                        if(firstTouch && (System.currentTimeMillis() - time) <= DOUBLE_CLICK_TIME_DELTA) {
                            Log.e("** DOUBLE TAP**"," second tap ");
                            if (mSelectedCell == getCellAtPoint(x, y)) {
                                sudokuGame.setHighlightedAction(mSelectedCell.getRow(), mSelectedCell.getCol());
                            }
                            firstTouch = false;

                        } else {
                            firstTouch = true;
                            time = System.currentTimeMillis();
                            Log.e("** SINGLE  TAP**"," First Tap time  "+time);
                        }
                    }
                case MotionEvent.ACTION_MOVE:
                    mTouchedCell = getCellAtPoint(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    mSelectedCell = getCellAtPoint(x, y);
                    invalidate(); // selected cell has changed, update board as soon as you can

                    if (mSelectedCell != null) {
                        onCellTapped(mSelectedCell);
                        onCellSelected(mSelectedCell);
                    }

                    if (mAutoHideTouchedCellHint) {
                        mTouchedCell = null;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mTouchedCell = null;
                    break;
            }
            postInvalidate();
        }

        return !mReadonly;
    }

    /**
     * Returns cell at given screen coordinates. Returns null if no cell is found.
     *
     * @param x
     * @param y
     * @return
     */
    private CellTile getCellAtPoint(int x, int y) {
        // take into account padding
        int lx = x - getPaddingLeft();
        int ly = y - getPaddingTop();

        int row = (int) (ly / mCellHeight);
        int col = (int) (lx / mCellWidth);

        if (col >= 0 && col < sudokuGame.getLength()
                && row >= 0 && row < sudokuGame.getLength()) {
            return cells[row][col];
        } else {
            return null;
        }
    }

    /**
     * Occurs when user tap the cell.
     *
     */
    public interface OnCellTappedListener {
        void onCellTapped(CellTile cell);
    }

    /**
     * Occurs when user selects the cell.
     *
     */
    public interface OnCellSelectedListener {
        void onCellSelected(CellTile cell);
    }

}

