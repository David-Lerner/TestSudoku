
package com.example.david.testsudoku.inputmethod;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import com.example.david.testsudoku.R;
import com.example.david.testsudoku.CellTile;
import com.david.completesudoku.SudokuGame.OnChangeListener;
import com.example.david.testsudoku.HintsQueue;
import com.example.david.testsudoku.SudokuBoardView;
import com.example.david.testsudoku.inputmethod.IMControlPanelStatePersister.StateBundle;

/**
 * This class represents following type of number input workflow: Number buttons are displayed
 * in the sidebar, user selects one number and then fill values by tapping the cells.
 *
 */
public class IMSingleNumber extends InputMethod {

	private static final int MODE_EDIT_VALUE = 0;
	private static final int MODE_EDIT_NOTE = 1;

	private boolean mHighlightCompletedValues = true;
	private boolean mShowNumberTotals = false;

	private int mSelectedNumber = 1;
	private int mEditMode = MODE_EDIT_VALUE;

	private Handler mGuiHandler;
	private Map<Integer, Button> mNumberButtons;
	private ImageButton mSwitchNumNoteButton;

	private long time;
	boolean firstTouch;
	private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

	public static final String SETTINGS_ENABLE_NAME = "im_single_number";

	public IMSingleNumber() {
		super();

		mGuiHandler = new Handler();
	}

	public boolean getHighlightCompletedValues() {
		return mHighlightCompletedValues;
	}

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

	@Override
	protected void initialize(Context context, IMControlPanel controlPanel,
							  SudokuBoardView board, HintsQueue hintsQueue) {
		super.initialize(context, controlPanel, board, hintsQueue);

		sudokuGame.addOnChangeListener(mOnCellsChangeListener);

		time = System.currentTimeMillis();
		firstTouch = false;
	}

	@Override
	public int getNameResID() {
		return R.string.single_number;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_single_number_hint;
	}

	@Override
	public String getSettingsEnableName() {
		return SETTINGS_ENABLE_NAME;
	}

	@Override
	protected View createControlPanelView() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View controlPanel = inflater.inflate(R.layout.im_single_number, null);

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
			b.setOnClickListener(mNumberButtonClicked);
            b.setOnTouchListener(mNumberButtonTouched);
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

    private View.OnTouchListener mNumberButtonTouched = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mSelectedNumber = (Integer) view.getTag();
            update();
            return true;
        }
    };

	private OnClickListener mNumberButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mSelectedNumber = (Integer) v.getTag();

			update();
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
				mSwitchNumNoteButton.setImageResource(R.drawable.ic_notes_white);
				mSwitchNumNoteButton.getBackground().setColorFilter(Color.GRAY,PorterDuff.Mode.MULTIPLY);
				break;
			case MODE_EDIT_VALUE:
				mSwitchNumNoteButton.setImageResource(R.drawable.ic_notes);
				mSwitchNumNoteButton.getBackground().setColorFilter(null);
				break;
		}

		// this is just ugly workaround
		mGuiHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (Button b : mNumberButtons.values()) {
					if (b.getTag().equals(mSelectedNumber)) {
                        TextViewCompat.setTextAppearance(b, android.R.style.TextAppearance_Large);
                        /* Use focus instead color */
						/*LightingColorFilter selBkgColorFilter = new LightingColorFilter(
								mContext.getResources().getColor(R.color.im_number_button_selected_background), 0);
						b.getBackground().setColorFilter(selBkgColorFilter);*/
                        b.requestFocus();
					} else {
                        TextViewCompat.setTextAppearance(b, android.R.style.TextAppearance_Widget_Button);
						b.getBackground().setColorFilter(null);
					}
				}

				Map<Integer, Integer> valuesUseCount = null;
				if (mHighlightCompletedValues || mShowNumberTotals) {
					//below: get number of times each number shows up in puzzle; > 9 = not possible
					//valuesUseCount = mBoard.getCells().getValuesUseCount();
					valuesUseCount = new HashMap<>();
					for (int value = 1; value <= sudokuGame.getLength(); value++) {
						valuesUseCount.put(value, sudokuGame.getValueCount(value));
					}

				}

				if (mHighlightCompletedValues) {
					//int completedTextColor = mContext.getResources().getColor(R.color.im_number_button_completed_text);
					for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
						boolean highlightValue = entry.getValue() >= sudokuGame.getLength();
						if (highlightValue) {
							Button b = mNumberButtons.get(entry.getKey());
                            // Only set background color
                            b.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
							b.setTextColor(Color.WHITE);
						}
					}
				}

				if (mShowNumberTotals) {
					for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
						Button b = mNumberButtons.get(entry.getKey());
						if (!b.getTag().equals(mSelectedNumber))
							b.setText(entry.getKey() + " (" + entry.getValue() + ")");
						else
							b.setText("" + entry.getKey());
					}
				}
			}
		}, 100);
	}

	@Override
	protected void onActivated() {
		update();
	}

	@Override
	protected void onCellTapped(CellTile cell) {
		int selNumber = mSelectedNumber;

		if (cell != null) {
			//set cell value/possibilities
			if (firstTouch && (System.currentTimeMillis() - time) <= DOUBLE_CLICK_TIME_DELTA) {
				switch (mEditMode) {
					case MODE_EDIT_NOTE:
						sudokuGame.setHighlightedPossibilityAction(selNumber);
						break;
					case MODE_EDIT_VALUE:
						sudokuGame.setHighlightedValueAction(selNumber);
						break;
				}
				firstTouch = false;

			} else {
				firstTouch = true;
				time = System.currentTimeMillis();
				switch (mEditMode) {
					case MODE_EDIT_NOTE:
						sudokuGame.setPossibleAction(cell.getRow(), cell.getCol(), selNumber);
						break;
					case MODE_EDIT_VALUE:
						sudokuGame.setValueAction(cell.getRow(), cell.getCol(), selNumber);
						break;
				}
			}
		}

	}

	@Override
	protected void onSaveState(StateBundle outState) {
		outState.putInt("selectedNumber", mSelectedNumber);
		outState.putInt("editMode", mEditMode);
	}

	@Override
	protected void onRestoreState(StateBundle savedState) {
		mSelectedNumber = savedState.getInt("selectedNumber", 1);
		mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
		if (isInputMethodViewCreated()) {
			update();
		}
	}

}
