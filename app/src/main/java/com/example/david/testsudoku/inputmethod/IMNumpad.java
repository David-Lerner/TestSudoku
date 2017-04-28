
package com.example.david.testsudoku.inputmethod;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import com.example.david.testsudoku.R;
import com.example.david.testsudoku.CellTile;
import com.example.david.testsudoku.CellCollection;
import com.example.david.testsudoku.CellCollection.OnChangeListener;
import com.example.david.testsudoku.HintsQueue;
import com.example.david.testsudoku.SudokuBoardView;
import com.example.david.testsudoku.inputmethod.IMControlPanelStatePersister.StateBundle;

public class IMNumpad extends InputMethod {

    private static final String TAG = "IMNumpad";

	private boolean moveCellSelectionOnPress = true;
	private boolean mHighlightCompletedValues = true;
	private boolean mShowNumberTotals = false;

	private static final int MODE_EDIT_VALUE = 0;
	private static final int MODE_EDIT_NOTE = 1;

	private CellTile mSelectedCell;
	private ImageButton mSwitchNumNoteButton;

	private int mEditMode = MODE_EDIT_VALUE;

	private Map<Integer, Button> mNumberButtons;

	public boolean isMoveCellSelectionOnPress() {
		return moveCellSelectionOnPress;
	}

	public void setMoveCellSelectionOnPress(boolean moveCellSelectionOnPress) {
		this.moveCellSelectionOnPress = moveCellSelectionOnPress;
	}

	public boolean getHighlightCompletedValues() {
		return mHighlightCompletedValues;
	}

	/**
	 * If set to true, buttons for numbers, which occur in {@link CellCollection}
	 * more than 9-times, will be highlighted.
	 *
	 * @param highlightCompletedValues
	 */
	public void setHighlightCompletedValues(boolean highlightCompletedValues) {
		mHighlightCompletedValues = highlightCompletedValues;
	}

	public boolean getShowNumberTotals() {
		return mShowNumberTotals;
	}

	public void setShowNumberTotals(boolean showNumberTotals) {
		mShowNumberTotals = showNumberTotals;
	}

	@Override
	protected void initialize(Context context, IMControlPanel controlPanel,
							  SudokuBoardView board, HintsQueue hintsQueue) {
		super.initialize(context, controlPanel, board, hintsQueue);

		board.getCells().addOnChangeListener(mOnCellsChangeListener);
        Log.d(TAG, "initialize()");
	}

	@Override
	protected View createControlPanelView() {
        Log.d(TAG, "createControlPanelView()");
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View controlPanel = inflater.inflate(R.layout.im_numpad, null);

		mNumberButtons = new HashMap<Integer, Button>();
		mNumberButtons.put(1, (Button) controlPanel.findViewById(R.id.button_1));
		mNumberButtons.put(2, (Button) controlPanel.findViewById(R.id.button_2));
		mNumberButtons.put(3, (Button) controlPanel.findViewById(R.id.button_3));
		mNumberButtons.put(4, (Button) controlPanel.findViewById(R.id.button_4));
		mNumberButtons.put(5, (Button) controlPanel.findViewById(R.id.button_5));
		mNumberButtons.put(6, (Button) controlPanel.findViewById(R.id.button_6));
		mNumberButtons.put(7, (Button) controlPanel.findViewById(R.id.button_7));
		mNumberButtons.put(8, (Button) controlPanel.findViewById(R.id.button_8));
		mNumberButtons.put(9, (Button) controlPanel.findViewById(R.id.button_9));
		mNumberButtons.put(0, (Button) controlPanel.findViewById(R.id.button_clear));

		for (Integer num : mNumberButtons.keySet()) {
			Button b = mNumberButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(mNumberButtonClick);
		}

		mSwitchNumNoteButton = (ImageButton) controlPanel.findViewById(R.id.switch_num_note);
		mSwitchNumNoteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE : MODE_EDIT_VALUE;
				update();
			}

		});

		return controlPanel;

	}

	@Override
	public int getNameResID() {
		return R.string.numpad;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_numpad_hint;
	}

	@Override
	public String getAbbrName() {
		return mContext.getString(R.string.numpad_abbr);
	}

	@Override
	protected void onActivated() {
        Log.d(TAG, "onActivated()");
		update();

		mSelectedCell = mBoard.getSelectedCell();
	}

	@Override
	protected void onCellSelected(CellTile cell) {
		mSelectedCell = cell;
	}

	private OnClickListener mNumberButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int selNumber = (Integer) v.getTag();
			CellTile selCell = mSelectedCell;

			if (selCell != null) {
				//set cell value/possibilities
				switch (mEditMode) {
					case MODE_EDIT_NOTE:
					    Log.d(TAG, "i: "+ selCell.getRow()+" j: "+selCell.getCol()+" n: "+selNumber);
					    sudokuGame.setPossibleAction(selCell.getRow(), selCell.getCol(), selNumber);
                        mBoard.getCells().updateCells();
						break;
					case MODE_EDIT_VALUE:
					    sudokuGame.setValueAction(selCell.getRow(), selCell.getCol(), selNumber);
                        mBoard.getCells().updateCells();
						break;
				}
			}
		}

	};

	private OnChangeListener mOnCellsChangeListener = new OnChangeListener() {

		@Override
		public void onChange() {
			if (mActive) {
				update();
			}
		}
	};


	private void update() {
		switch (mEditMode) {
			case MODE_EDIT_NOTE:
				mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_white);
				break;
			case MODE_EDIT_VALUE:
				mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_grey);
				break;
		}

		//excluding unpossible values -implement later
		Map<Integer, Integer> valuesUseCount = null;
		if (mHighlightCompletedValues || mShowNumberTotals) {
            //below: get number of times each number shows up in puzzle; > 9 = not possible
            //valuesUseCount = mBoard.getCells().getValuesUseCount();
            valuesUseCount = new HashMap<>();
            for (int value = 1; value <= sudokuGame.getLength(); value++) {
                valuesUseCount.put(value, 0);
            }

        }

		if (mHighlightCompletedValues) {
			for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
				boolean highlightValue = entry.getValue() >= sudokuGame.getLength();
				Button b = mNumberButtons.get(entry.getKey());
				if (highlightValue) {
                    b.getBackground().setColorFilter(0xFF1B5E20, PorterDuff.Mode.MULTIPLY);
				} else {
                    b.getBackground().setColorFilter(null);
				}
			}
		}

		if (mShowNumberTotals) {
			for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
				Button b = mNumberButtons.get(entry.getKey());
				b.setText(entry.getKey() + " (" + entry.getValue() + ")");
			}
		}
	}

	@Override
	protected void onSaveState(StateBundle outState) {
		outState.putInt("editMode", mEditMode);
	}

	@Override
	protected void onRestoreState(StateBundle savedState) {
		mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
		if (isInputMethodViewCreated()) {
			update();
		}
	}
}
