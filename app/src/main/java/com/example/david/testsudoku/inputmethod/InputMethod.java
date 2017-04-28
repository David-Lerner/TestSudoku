/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.example.david.testsudoku.inputmethod;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.david.completesudoku.SudokuGame;
import com.example.david.testsudoku.R;
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
			View switchModeView = mInputMethodView.findViewById(R.id.switch_input_mode);
			Button switchModeButton = (Button) switchModeView;
			switchModeButton.setText(getAbbrName());
			switchModeButton.getBackground().setColorFilter(new LightingColorFilter(Color.parseColor("#00695c"), 0));
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

	/**
	 * Gets abbreviated name of input method, which will be displayed on input method switch button.
	 *
	 * @return
	 */
	public abstract String getAbbrName();

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

	protected abstract View createControlPanelView();

	protected void onControlPanelCreated(View controlPanel) {

	}

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
