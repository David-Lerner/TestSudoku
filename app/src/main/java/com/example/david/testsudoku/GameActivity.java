package com.example.david.testsudoku;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.david.completesudoku.SudokuGame;
import com.david.completesudoku.SudokuModel;
import com.example.david.testsudoku.inputmethod.IMControlPanel;
import com.example.david.testsudoku.inputmethod.IMControlPanelStatePersister;
import com.example.david.testsudoku.inputmethod.InputMethod;

import java.util.List;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "IMControlPanel";

    public static final int MENU_ITEM_UNDO = Menu.FIRST;
    public static final int MENU_ITEM_REDO = Menu.FIRST + 1;
    public static final int MENU_ITEM_ACTION_MENU = Menu.FIRST + 2;
    public static final int MENU_ITEM_CLEAR_ALL_NOTES = Menu.FIRST + 3;
    public static final int MENU_ITEM_FILL_IN_NOTES = Menu.FIRST + 4;
    public static final int MENU_ITEM_SHOW_ERROR = Menu.FIRST + 5;
    public static final int MENU_ITEM_SHOW_ALL_ERRORS = Menu.FIRST + 6;
    public static final int MENU_ITEM_SHOW_VALUE = Menu.FIRST + 7;
    public static final int MENU_ITEM_SOLVE = Menu.FIRST + 8;
    public static final int MENU_ITEM_RESTART = Menu.FIRST + 9;
    public static final int MENU_ITEM_HELP = Menu.FIRST + 10;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 11;
    public static final int MENU_ITEM_SAVE = Menu.FIRST + 12;
    public static final int MENU_ITEM_EXIT = Menu.FIRST + 13;

    private static final int DIALOG_RESTART = 1;
    private static final int DIALOG_WELL_DONE = 2;
    private static final int DIALOG_SOLVE = 3;
    private static final int DIALOG_SAVE = 4;
    private static final int DIALOG_EXIT = 5;

    private static final int REQUEST_SETTINGS = 1;

    public static final int SAVE_SUCCESS = 0;
    public static final int SAVE_FAILURE = 1;
    public static final int SAVE_AND_QUIT = 2;

    private ViewGroup mRootLayout;

    //input
    private IMControlPanel mIMControlPanel;
    private IMControlPanelStatePersister mIMControlPanelStatePersister;
    private List<InputMethod> inputMethods;

    //time
    private TextView mTimeLabel;
    private Handler timerHandler;
    Runnable timerRunnable;
    private boolean mShowTime = true;

    private HintsQueue mHintsQueue;

    private SudokuBoardView mSudokuBoard;
    private SudokuGame sudokuGame;
    private boolean showScore;

    private Handler saveHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
        mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);
        mTimeLabel = (TextView) findViewById(R.id.time_label);

        mHintsQueue = new HintsQueue(this);

        timerHandler = new Handler();

        mHintsQueue.showOneTimeHint("welcome", R.string.welcome, R.string.first_run_hint);

        //persist data on activity restarts
        sudokuGame = DataResult.getInstance().getSudokuGame();
        if (sudokuGame == null) {
            //change to return to previous screen
            Toast toast = Toast.makeText(this, R.string.no_cell, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            finish();
        }
        sudokuGame.clearOnChangeListeners();

        sudokuGame.addOnChangeListener(new SudokuGame.OnChangeListener() {
            @Override
            public void onChange() {
                invalidateOptionsMenu();
                getSupportActionBar().setTitle(sudokuGame.getName());
            }
        });

        mSudokuBoard.setSudokuGame(sudokuGame);

        CellTile[][] cellTiles = new CellTile[sudokuGame.getLength()][sudokuGame.getLength()];
        for (int i = 0; i < sudokuGame.getLength(); i++) {
            for (int j = 0; j < sudokuGame.getLength(); j++) {
                cellTiles[i][j] = new CellTile(i, j);
            }
        }

        mSudokuBoard.setTarget(DataResult.getInstance().getTarget());

        //input
        mIMControlPanel = (IMControlPanel) findViewById(R.id.input_methods);
        mIMControlPanel.initialize(mSudokuBoard, mHintsQueue);
        mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);
        inputMethods = mIMControlPanel.getInputMethods();

        mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);

        Log.d(TAG, "calling begin()");
        sudokuGame.begin();

        showScore = false;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                mTimeLabel.setText(sudokuGame.getElapsedFormatted());
                if (sudokuGame.getStatus().equals(SudokuGame.COMPLETED) && !showScore) {
                    showScore = true;
                    mSudokuBoard.setReadOnly(true);
                    showDialog(DIALOG_WELL_DONE);
                }
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(sudokuGame.getName());

        saveHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if (msg.what == SAVE_SUCCESS){
                    Toast toast = Toast.makeText(GameActivity.this, String.format(getString(R.string.save_success),
                            sudokuGame.getName()), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else if (msg.what == SAVE_FAILURE) {
                    Toast toast = Toast.makeText(GameActivity.this, String.format(getString(R.string.save_failure),
                            sudokuGame.getName()), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else if (msg.what == SAVE_AND_QUIT) {
                    Toast toast = Toast.makeText(GameActivity.this, String.format(getString(R.string.save_success),
                            sudokuGame.getName()), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    finish();
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();

        DataResult.getInstance().setTarget(mSudokuBoard.getTarget());
        sudokuGame.stop();
        mIMControlPanel.pause();
        mIMControlPanelStatePersister.saveState(mIMControlPanel);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // read game settings
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int screenPadding = gameSettings.getInt("screen_border_size", 0);
        mRootLayout.setPadding(screenPadding, screenPadding, screenPadding, screenPadding);

        mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean("highlight_wrong_values", true));
        mSudokuBoard.setHighlightTouchedCell(gameSettings.getBoolean("highlight_touched_cell", true));
        mSudokuBoard.setTarget(DataResult.getInstance().getTarget());

        mShowTime = gameSettings.getBoolean("show_time", true);

        mTimeLabel.setVisibility(mShowTime ? View.VISIBLE : View.GONE);

        for (InputMethod method : inputMethods) {
            method.setEnabled(gameSettings.getBoolean(method.getSettingsEnableName(), true));
            method.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
            method.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        }

        mIMControlPanel.activateFirstInputMethod(); // make sure that some input method is activated
        mIMControlPanelStatePersister.restoreState(mIMControlPanel);

        sudokuGame.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_ITEM_UNDO, 0, R.string.undo).setIcon(R.drawable.ic_undo);
        menu.add(0, MENU_ITEM_REDO, 1, R.string.redo).setIcon(R.drawable.ic_redo);
        SubMenu actionMenu = menu.addSubMenu (2, MENU_ITEM_ACTION_MENU, 2, R.string.actions);
        actionMenu.add(0, MENU_ITEM_CLEAR_ALL_NOTES, 2, R.string.clear_all_notes).setIcon(R.drawable.ic_delete);
        actionMenu.add(0, MENU_ITEM_FILL_IN_NOTES, 3, R.string.fill_in_notes).setIcon(R.drawable.ic_notes);
        actionMenu.add(0, MENU_ITEM_SHOW_ERROR, 4, R.string.show_cell_error).setIcon(R.drawable.ic_clear);
        actionMenu.add(0, MENU_ITEM_SHOW_ALL_ERRORS, 5, R.string.show_all_errors).setIcon(R.drawable.ic_grid_off);
        actionMenu.add(0, MENU_ITEM_SHOW_VALUE, 6, R.string.show_cell_value).setIcon(R.drawable.ic_check);
        actionMenu.add(0, MENU_ITEM_SOLVE, 7, R.string.solve).setIcon(R.drawable.ic_grid);
        actionMenu.add(0, MENU_ITEM_RESTART, 8, R.string.restart).setIcon(R.drawable.ic_restore);
        menu.add(0, MENU_ITEM_HELP, 9, R.string.help).setIcon(R.drawable.ic_help);
        menu.add(0, MENU_ITEM_SETTINGS, 10, R.string.settings).setIcon(R.drawable.ic_settings);
        menu.add(0, MENU_ITEM_SAVE, 11, R.string.save).setIcon(R.drawable.ic_save);
        menu.add(0, MENU_ITEM_EXIT, 12, R.string.exit).setIcon(R.drawable.ic_back);

        menu.findItem(MENU_ITEM_UNDO).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.findItem(MENU_ITEM_REDO).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (sudokuGame.getStatus().equals(SudokuGame.IN_PROGRESS)) {
            if (sudokuGame.hasUndo()) {
                menu.findItem(MENU_ITEM_UNDO).setEnabled(true);
                menu.findItem(MENU_ITEM_UNDO).getIcon().setAlpha(255);
            } else {
                menu.findItem(MENU_ITEM_UNDO).setEnabled(false);
                menu.findItem(MENU_ITEM_UNDO).getIcon().setAlpha(64);
            }
            if (sudokuGame.hasRedo()) {
                menu.findItem(MENU_ITEM_REDO).setEnabled(true);
                menu.findItem(MENU_ITEM_REDO).getIcon().setAlpha(255);
            } else {
                menu.findItem(MENU_ITEM_REDO).setEnabled(false);
                menu.findItem(MENU_ITEM_REDO).getIcon().setAlpha(64);
            }
            menu.findItem(MENU_ITEM_ACTION_MENU).setEnabled(true);
        } else {
            menu.findItem(MENU_ITEM_UNDO).setEnabled(false);
            menu.findItem(MENU_ITEM_UNDO).getIcon().setAlpha(100);
            menu.findItem(MENU_ITEM_REDO).setEnabled(false);
            menu.findItem(MENU_ITEM_REDO).getIcon().setAlpha(100);
            menu.findItem(MENU_ITEM_ACTION_MENU).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CellTile cell;
        switch (item.getItemId()) {
            case MENU_ITEM_RESTART:
                showDialog(DIALOG_RESTART);
                return true;
            case MENU_ITEM_CLEAR_ALL_NOTES:
                sudokuGame.removePossibilities();
                return true;
            case MENU_ITEM_FILL_IN_NOTES:
                sudokuGame.showPossibilities();
                return true;
            case MENU_ITEM_HELP:
                mHintsQueue.showHint(R.string.help, R.string.help_text);
                return true;
            case MENU_ITEM_SETTINGS:
                Intent i = new Intent();
                i.setClass(this, GameSettingsActivity.class);
                startActivityForResult(i, REQUEST_SETTINGS);
                return true;
            case MENU_ITEM_UNDO:
                sudokuGame.undo();
                return true;
            case MENU_ITEM_REDO:
                sudokuGame.redo();
                return true;
            case MENU_ITEM_ACTION_MENU:
                mHintsQueue.showOneTimeHint("actions", R.string.actions, R.string.actions_hint);
                return true;
            case MENU_ITEM_SHOW_ERROR:
                cell = mSudokuBoard.getSelectedCell();
                if (cell != null) {
                    sudokuGame.showError(cell.getRow(), cell.getCol());
                } else {
                    Toast toast = Toast.makeText(this, R.string.no_cell, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return true;
            case MENU_ITEM_SHOW_ALL_ERRORS:
                sudokuGame.showAllErrors();
                return true;
            case MENU_ITEM_SHOW_VALUE:
                cell = mSudokuBoard.getSelectedCell();
                if (cell != null) {
                    sudokuGame.showAnswer(cell.getRow(), cell.getCol());
                } else {
                    Toast toast = Toast.makeText(this, R.string.no_cell, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return true;
            case MENU_ITEM_SOLVE:
                showDialog(DIALOG_SOLVE);
                return true;
            case MENU_ITEM_SAVE:
                showDialog(DIALOG_SAVE);
                return true;
            case MENU_ITEM_EXIT:
                showDialog(DIALOG_EXIT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SETTINGS:
                sudokuGame.clearShownErrors();
                restartActivity();
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_RESTART:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_restore)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.restart_confirm)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sudokuGame.reset();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_WELL_DONE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_info)
                        .setTitle(R.string.complete)
                        .setCancelable(false)
                        .setMessage(String.format(getString(R.string.congrats), sudokuGame.getElapsedFormatted(), sudokuGame.getScore()))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                SudokuModel sudokuModel = DataResult.getInstance().getSudokuModel();
                                try {
                                    sudokuModel.saveGame(sudokuGame, saveHandler);
                                } catch (Exception e) {
                                    Log.d(TAG, e.getMessage());
                                    saveHandler.sendEmptyMessage(SAVE_FAILURE);
                                }
                            }
                        })
                        .create();
            case DIALOG_SOLVE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_grid)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.solve_confirm)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sudokuGame.showAllAnswers();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_SAVE:
                final EditText editText = new EditText(getApplicationContext());
                editText.setText(sudokuGame.getName());
                editText.setTextColor(Color.BLACK);
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_save)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.save_dialog)
                        .setView(editText)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String name = editText.getText().toString();
                                sudokuGame.setName(name);
                                SudokuModel sudokuModel = DataResult.getInstance().getSudokuModel();
                                try {
                                    sudokuModel.saveGame(sudokuGame, saveHandler);
                                } catch (Exception e) {
                                    Log.d(TAG, e.getMessage());
                                    saveHandler.sendEmptyMessage(SAVE_FAILURE);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
                return alertDialog;
            case DIALOG_EXIT:
                return new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.back_confirm)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .create();

        }
        return null;
    }

    /**
     * Restarts whole activity.
     */
    private void restartActivity() {
        startActivity(getIntent());
        finish();
    }

    @Override
    public void onBackPressed() {
        showDialog(DIALOG_EXIT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DataResult.getInstance().setTarget(mSudokuBoard.getTarget());
        DataResult.getInstance().setSudokuGame(sudokuGame);
    }

    public interface Callback {
        public void call(String message);
    }

}
