
package com.example.david.testsudoku.inputmethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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

	public static final int INPUT_METHOD_NUMPAD = 0;
    public static final int INPUT_METHOD_SINGLE_NUMBER = 1;
    public static final int INPUT_METHOD_POPUP = 2;

    private Context mContext;
	private SudokuBoardView mBoard;
	private HintsQueue mHintsQueue;

	private List<InputMethod> mInputMethods = new ArrayList<InputMethod>();
	private int mActiveMethodIndex = -1;

	public IMControlPanel(Context context) {
		super(context);
		mContext = context;
	}

	public IMControlPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
        Log.d(TAG,"IMControlPanel() (constructor)");
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

	public void activateNextInputMethod() {
        Log.d(TAG, "activateNextInputMethod()");
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

	/**
	 * Returns input method object by its ID (see INPUT_METHOD_* constants).
	 *
	 * @param methodId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends InputMethod> T getInputMethod(int methodId) {
		ensureInputMethods();
        Log.d(TAG, "getInputMethod(int methodId="+methodId+"), returns mInputMethods.get(methodId)="+mInputMethods.get(methodId).getAbbrName());
		return (T) mInputMethods.get(methodId);
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
			addInputMethod(INPUT_METHOD_NUMPAD, new IMNumpad());
            addInputMethod(INPUT_METHOD_SINGLE_NUMBER, new IMSingleNumber());
            addInputMethod(INPUT_METHOD_POPUP, new IMPopup());
		}
	}

	private void addInputMethod(int methodIndex, InputMethod im) {
		im.initialize(mContext, this, mBoard, mHintsQueue);
		mInputMethods.add(methodIndex, im);
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
			switchModeButton.setOnClickListener(mSwitchModeListener);
			this.addView(controlPanel, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
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
			activateNextInputMethod();
		}
	};

}
