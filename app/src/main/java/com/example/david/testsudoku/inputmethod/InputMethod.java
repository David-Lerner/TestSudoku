
package com.example.david.testsudoku.inputmethod;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.david.completesudoku.SudokuGame;
import com.example.david.testsudoku.CellTile;
import com.example.david.testsudoku.HintsQueue;
import com.example.david.testsudoku.SudokuBoardView;
import com.example.david.testsudoku.inputmethod.IMControlPanelStatePersister.StateBundle;

/**
 * Base class for several input methods used to edit Sudoku contents.
 *
 */
public abstract class InputMethod {

    private static final String TAG = "InputMethod";
    // inherited by subclasses
	protected Context mContext;
	protected IMControlPanel mControlPanel;
	protected SudokuBoardView mBoard;
    protected SudokuGame sudokuGame;
	protected HintsQueue mHintsQueue;

	private String mInputMethodName;
	protected View mInputMethodView;

	protected boolean mActive = false;
	private boolean mEnabled = true;

	public InputMethod() {

	}

	protected void initialize(Context context, IMControlPanel controlPanel, SudokuBoardView board, HintsQueue hintsQueue) {
		mContext = context;
		mControlPanel = controlPanel;
		mBoard = board;
        sudokuGame = board.getSudokuGame();
		mHintsQueue = hintsQueue;
		mInputMethodName = this.getClass().getSimpleName();
	}

	public boolean isInputMethodViewCreated() {
		return mInputMethodView != null;
	}

	public View getInputMethodView() {
        Log.d(TAG, "getInputMethodView()");
		if (mInputMethodView == null) {
            Log.d(TAG, "mInputMethodView == null");
			mInputMethodView = createControlPanelView();
			onControlPanelCreated(mInputMethodView);
		}

		return mInputMethodView;
	}

	/**
	 * This should be called when activity is paused (so InputMethod can do some cleanup,
	 * for example properly dismiss dialogs because of WindowLeaked exception).
	 */
	public void pause() {
		onPause();
	}

	protected void onPause() {

	}

	/**
	 * This should be unique name of input method.
	 *
	 * @return
	 */
	protected String getInputMethodName() {
		return mInputMethodName;
	}

	public abstract int getNameResID();

	public abstract int getHelpResID();

	public abstract String getSettingsEnableName();

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;

		if (!enabled) {
			mControlPanel.activateNextInputMethod();
		}
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void activate() {
		mActive = true;
		onActivated();
	}

	public void deactivate() {
		mActive = false;
		onDeactivated();
	}

	public boolean isActive() {
		return mActive;
	}

	protected abstract View createControlPanelView();

	protected void onControlPanelCreated(View controlPanel) {

	}

	public abstract void setHighlightCompletedValues(boolean highlightCompletedValues);
	public abstract void setShowNumberTotals(boolean numberTotals);

	protected void onActivated() {
	}

	protected void onDeactivated() {
	}

	/**
	 * Called when cell is selected. Please note that cell selection can
	 * change without direct user interaction.
	 *
	 * @param cell
	 */
	protected void onCellSelected(CellTile cell) {

	}

	/**
	 * Called when cell is tapped.
	 *
	 * @param cell
	 */
	protected void onCellTapped(CellTile cell) {

	}

	protected void onSaveState(StateBundle outState) {
	}

	protected void onRestoreState(StateBundle savedState) {
	}
}
