
package com.example.david.testsudoku.inputmethod;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import com.example.david.testsudoku.R;
import com.example.david.testsudoku.CellTile;
import com.example.david.testsudoku.inputmethod.IMPopupDialog.OnNoteEditListener;
import com.example.david.testsudoku.inputmethod.IMPopupDialog.OnNumberEditListener;


public class IMPopup extends InputMethod {

	private boolean mHighlightCompletedValues = true;
	private boolean mShowNumberTotals = false;

	private IMPopupDialog mEditCellDialog;
	private CellTile mSelectedCell;

	public static final String SETTINGS_ENABLE_NAME = "im_popup";

	public boolean getHighlightCompletedValues() {
		return mHighlightCompletedValues;
	}

	/**
	 * If set to true, buttons for numbers, which occur in {@link com.david.completesudoku.Sudoku}
	 * more than 9-times, will be highlighted.
	 *
	 * @param highlightCompletedValues
	 */
	@Override
	public void setHighlightCompletedValues(boolean highlightCompletedValues) {
		mHighlightCompletedValues = highlightCompletedValues;
	}

	public boolean getShowNumberTotals() {
		return mShowNumberTotals;
	}

	@Override
	public void setShowNumberTotals(boolean showNumberTotals) {
		mShowNumberTotals = showNumberTotals;
	}

	private void ensureEditCellDialog() {
		if (mEditCellDialog == null) {
			mEditCellDialog = new IMPopupDialog(mContext);
			mEditCellDialog.setOnNumberEditListener(mOnNumberEditListener);
			mEditCellDialog.setOnNoteEditListener(mOnNoteEditListener);
			mEditCellDialog.setOnDismissListener(mOnPopupDismissedListener);
		}

	}

	@Override
	protected void onActivated() {
		mBoard.setAutoHideTouchedCellHint(false);
	}

	@Override
	protected void onDeactivated() {
		mBoard.setAutoHideTouchedCellHint(true);
	}

	@Override
	public String getSettingsEnableName() {
		return SETTINGS_ENABLE_NAME;
	}

	@Override
	protected void onCellTapped(CellTile cell) {
		mSelectedCell = cell;
		if (!sudokuGame.isGiven(cell.getRow(),cell.getCol())) {
			ensureEditCellDialog();

			mEditCellDialog.resetButtons();
			mEditCellDialog.updateNumber(sudokuGame.getValue(cell.getRow(), cell.getCol()));
			mEditCellDialog.updateNote(sudokuGame.getPossibilities(cell.getRow(), cell.getCol()));

			Map<Integer, Integer> valuesUseCount = null;
			if (mHighlightCompletedValues || mShowNumberTotals) {
				//valuesUseCount = mGame.getCells().getValuesUseCount();
				valuesUseCount = new HashMap<>();
				for (int value = 1; value <= sudokuGame.getLength(); value++) {
					valuesUseCount.put(value, sudokuGame.getValueCount(value));
				}
			}

			if (mHighlightCompletedValues) {
				for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
					if (entry.getValue() >= sudokuGame.getLength()) {
						mEditCellDialog.highlightNumber(entry.getKey());
					}
				}
			}

			if (mShowNumberTotals) {
				for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
					mEditCellDialog.setValueCount(entry.getKey(), entry.getValue());
				}
			}
			mEditCellDialog.show();
		} else {
			mBoard.hideTouchedCellHint();
		}
	}

	@Override
	protected void onPause() {
		// release dialog resource (otherwise WindowLeaked exception is logged)
		if (mEditCellDialog != null) {
			mEditCellDialog.cancel();
		}
	}

	@Override
	public int getNameResID() {
		return R.string.popup;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_popup_hint;
	}

	@Override
	protected View createControlPanelView() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.im_popup, null);
	}

	/**
	 * Occurs when user selects number in EditCellDialog.
	 */
	private OnNumberEditListener mOnNumberEditListener = new OnNumberEditListener() {
		@Override
		public boolean onNumberEdit(int number) {
			if (number != -1 && mSelectedCell != null) {
				sudokuGame.setValueAction(mSelectedCell.getRow(), mSelectedCell.getCol(), number);
			}
			return true;
		}
	};

	/**
	 * Occurs when user edits note in EditCellDialog
	 */
	private OnNoteEditListener mOnNoteEditListener = new OnNoteEditListener() {
		@Override
		public boolean onNoteEdit(Integer[] numbers) {
			if (mSelectedCell != null) {
				boolean[] possibilities = new boolean[sudokuGame.getLength()];
				for (Integer n : numbers) {
					if (n > 0 && n <= sudokuGame.getLength())
						possibilities[n-1] = true;
				}
				sudokuGame.setPossibilitiesAction(mSelectedCell.getRow(), mSelectedCell.getCol(), possibilities);
			}
			return true;
		}
	};

	/**
	 * Occurs when popup dialog is closed.
	 */
	private OnDismissListener mOnPopupDismissedListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			mBoard.hideTouchedCellHint();
		}
	};

}
