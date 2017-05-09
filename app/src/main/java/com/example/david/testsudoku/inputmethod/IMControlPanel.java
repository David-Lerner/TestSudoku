
package com.example.david.testsudoku.inputmethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.david.testsudoku.R;
import com.example.david.testsudoku.HintsQueue;
import com.example.david.testsudoku.CellTile;
import com.example.david.testsudoku.SudokuBoardView;
import com.example.david.testsudoku.SudokuBoardView.OnCellSelectedListener;
import com.example.david.testsudoku.SudokuBoardView.OnCellTappedListener;

public class IMControlPanel extends LinearLayout {
    private static final String TAG = "IMControlPanel";

	private static final int TITLE = 10;
	private static final int INPUT_GROUP = 0;

    private Context mContext;
	private SudokuBoardView mBoard;
	private HintsQueue mHintsQueue;

	private List<InputMethod> mInputMethods = new ArrayList<InputMethod>();
	private int mActiveMethodIndex = -1;

	private int count = 0;

	public IMControlPanel(Context context) {
		super(context);
		mContext = context;
	}

	public IMControlPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void initialize(SudokuBoardView board, HintsQueue hintsQueue) {
		mBoard = board;
		mBoard.setOnCellTappedListener(mOnCellTapListener);
		mBoard.setOnCellSelectedListener(mOnCellSelected);

		mHintsQueue = hintsQueue;

		createInputMethods();
	}

	/**
	 * Activates first enabled input method. If such method does not exists, nothing
	 * happens.
	 */
	public void activateFirstInputMethod() {
        Log.d(TAG, "activateFirstInputMethod()");
		ensureInputMethods();
		if (mActiveMethodIndex == -1 || !mInputMethods.get(mActiveMethodIndex).isEnabled()) {
            Log.d(TAG, "mActiveMethodIndex == -1 || !mInputMethods.get(mActiveMethodIndex).isEnabled()");
			activateInputMethod(0);
		}

	}

	/**
	 * Activates given input method (see INPUT_METHOD_* constants). If the given method is
	 * not enabled, activates first available method after this method.
	 *
	 * @param methodID ID of method input to activate.
	 * @return
	 */
	public void activateInputMethod(int methodID) {
        Log.d(TAG, "activateInputMethod(methodID = "+methodID+")");
		if (methodID < -1 || methodID >= mInputMethods.size()) {
			throw new IllegalArgumentException(String.format("Invalid method id: %s.", methodID));
		}

		ensureInputMethods();

		if (mActiveMethodIndex != -1) {
			mInputMethods.get(mActiveMethodIndex).deactivate();
		}

		boolean idFound = false;
		int id = methodID;
		int numOfCycles = 0;

		if (id != -1) {
			while (!idFound && numOfCycles <= mInputMethods.size()) {
				if (mInputMethods.get(id).isEnabled()) {
					ensureControlPanel(id);
					idFound = true;
					break;
				}

				id++;
				if (id == mInputMethods.size()) {
					id = 0;
				}
				numOfCycles++;
			}
		}

		if (!idFound) {
			id = -1;
		}
		Log.d(TAG, "mInputMethods.size()="+mInputMethods.size());
		for (int i = 0; i < mInputMethods.size(); i++) {
			InputMethod im = mInputMethods.get(i);
			if (im.isInputMethodViewCreated()) {
                Log.d("sudoku:", "loop: "+i+" "+(i == id));
                im.getInputMethodView().setVisibility(i == id ? View.VISIBLE : View.GONE);
			}
		}

		mActiveMethodIndex = id;
		if (mActiveMethodIndex != -1) {
			InputMethod activeMethod = mInputMethods.get(mActiveMethodIndex);
			activeMethod.activate();

			if (mHintsQueue != null) {
				mHintsQueue.showOneTimeHint(activeMethod.getInputMethodName(), activeMethod.getNameResID(), activeMethod.getHelpResID());
			}
		}
	}

	private void selectInputMethod(View v) {
		ensureInputMethods();

		PopupMenu menu = new PopupMenu(mContext, v);
		menu.getMenu().add(TITLE, TITLE, 0, mContext.getString(R.string.input_methods));
		menu.getMenu().findItem(TITLE).setEnabled(false);
		for (int i = 0; i < mInputMethods.size(); i++) {
			InputMethod method = mInputMethods.get(i);
			if (method.isEnabled()) {
				menu.getMenu().add(INPUT_GROUP, i, i+1, mContext.getString(method.getNameResID()));
				menu.getMenu().findItem(i).setChecked(method.isActive());
			}
		}
		menu.getMenu().setGroupCheckable(INPUT_GROUP, true, true);
		menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				count++;
				if (count >= mInputMethods.size() && mHintsQueue != null) {
					mHintsQueue.showOneTimeHint("thatIsAll", R.string.that_is_all, R.string.im_disable_modes_hint);
				}
				activateInputMethod(item.getItemId());
				return true;
			}
		});
		menu.show();
	}

	public void activateNextInputMethod() {
		ensureInputMethods();

		int id = mActiveMethodIndex + 1;
		if (id >= mInputMethods.size()) {
			if (mHintsQueue != null) {
				mHintsQueue.showOneTimeHint("thatIsAll", R.string.that_is_all, R.string.im_disable_modes_hint);
			}
			id = 0;
		}
		activateInputMethod(id);
	}

	public List<InputMethod> getInputMethods() {
		return Collections.unmodifiableList(mInputMethods);
	}

	public int getActiveMethodIndex() {
		return mActiveMethodIndex;
	}

	public void showHelpForActiveMethod() {
		ensureInputMethods();

		if (mActiveMethodIndex != -1) {
			InputMethod activeMethod = mInputMethods.get(mActiveMethodIndex);
			activeMethod.activate();

			mHintsQueue.showHint(activeMethod.getNameResID(), activeMethod.getHelpResID());
		}
	}


	/**
	 * This should be called when activity is paused (so Input Methods can do some cleanup,
	 * for example properly dismiss dialogs because of WindowLeaked exception).
	 */
	public void pause() {
		for (InputMethod im : mInputMethods) {
			im.pause();
		}
	}

	/**
	 * Ensures that all input method objects are created.
	 */
	private void ensureInputMethods() {
		if (mInputMethods.size() == 0) {
			throw new IllegalStateException("Input methods are not created yet. Call initialize() first.");
		}

	}

	private void createInputMethods() {
		if (mInputMethods.size() == 0) {
			addInputMethod(new IMPopup());
			addInputMethod(new IMSingleNumber());
			addInputMethod(new IMNumpad());
		}
	}

	private void addInputMethod(InputMethod im) {
		im.initialize(mContext, this, mBoard, mHintsQueue);
		mInputMethods.add(im);
	}

	/**
	 * Ensures that control panel for given input method is created.
	 *
	 * @param methodID
	 */
	private void ensureControlPanel(int methodID) {
		InputMethod im = mInputMethods.get(methodID);
		if (!im.isInputMethodViewCreated()) {
			View controlPanel = im.getInputMethodView();
			Button switchModeButton = (Button) controlPanel.findViewById(R.id.switch_input_mode);
			switchModeButton.getBackground().setColorFilter(new LightingColorFilter(Color.parseColor("#008080"), 0));
			switchModeButton.setOnClickListener(mSwitchModeListener);
			this.addView(controlPanel, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
	}

	private OnCellTappedListener mOnCellTapListener = new OnCellTappedListener() {
		@Override
		public void onCellTapped(CellTile cell) {
			if (mActiveMethodIndex != -1 && mInputMethods != null) {
				mInputMethods.get(mActiveMethodIndex).onCellTapped(cell);
			}
		}
	};

	private OnCellSelectedListener mOnCellSelected = new OnCellSelectedListener() {
		@Override
		public void onCellSelected(CellTile cell) {
			if (mActiveMethodIndex != -1 && mInputMethods != null) {
				mInputMethods.get(mActiveMethodIndex).onCellSelected(cell);
			}
		}
	};

	private OnClickListener mSwitchModeListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			selectInputMethod(v);
		}
	};

}
