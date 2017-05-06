package com.example.david.testsudoku;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.Toast;

import com.david.completesudoku.Sudoku;
import com.david.completesudoku.SudokuGame;
import com.david.completesudoku.SudokuGenerator;
import com.example.david.testsudoku.inputmethod.IMControlPanel;
import com.example.david.testsudoku.inputmethod.IMControlPanelStatePersister;
import com.example.david.testsudoku.inputmethod.IMNumpad;
import com.example.david.testsudoku.inputmethod.IMSingleNumber;
import com.example.david.testsudoku.inputmethod.IMPopup;

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

    private ViewGroup mRootLayout;
    private TextView mTimeLabel;

    private IMControlPanel mIMControlPanel;
    private IMControlPanelStatePersister mIMControlPanelStatePersister;
    private IMPopup mIMPopup;
    private IMSingleNumber mIMSingleNumber;
    private IMNumpad mIMNumpad;

    //time
    private Handler timerHandler;
    Runnable timerRunnable;
    private boolean mShowTime = true;

    private boolean mFullScreen;
    private boolean mFillInNotesEnabled = false;

    private HintsQueue mHintsQueue;

    private SudokuBoardView mSudokuBoard;
    private SudokuGenerator sudokuGenerator;
    private SudokuGame sudokuGame;
    private CellCollection cells;

    //for persisting
    private DataResult dataResult;

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

        int[][] model = new int[9][9] ;

        // Create the initial situation

        model[0][0] = 9 ;
        model[0][4] = 2 ;
        model[0][6] = 7 ;
        model[0][7] = 5 ;

        model[1][0] = 6 ;
        model[1][4] = 5 ;
        model[1][7] = 4 ;

        model[2][1] = 2 ;
        model[2][3] = 4 ;
        model[2][7] = 1 ;

        model[3][0] = 2 ;
        model[3][2] = 8 ;

        model[4][1] = 7 ;
        model[4][3] = 5 ;
        model[4][5] = 9 ;
        model[4][7] = 6 ;

        model[5][6] = 4 ;
        model[5][8] = 1 ;

        model[6][1] = 1 ;
        model[6][5] = 5 ;
        model[6][7] = 8 ;

        model[7][1] = 9 ;
        model[7][4] = 7 ;
        model[7][8] = 4 ;

        model[8][1] = 8 ;
        model[8][2] = 2 ;
        model[8][4] = 4 ;
        model[8][8] = 6 ;

        sudokuGenerator = new SudokuGenerator();

        //persist data on activity restarts
        dataResult = DataResult.getInstance();
        sudokuGame = dataResult.getSudokuGame();
        if (sudokuGame == null) {
            //change to return to previous screen
            sudokuGame = new SudokuGame(new Sudoku(model));
            dataResult.setSudokuGame(sudokuGame);
        }

        mSudokuBoard.setSudokuGame(sudokuGame);

        CellTile[][] cellTiles = new CellTile[sudokuGame.getLength()][sudokuGame.getLength()];
        for (int i = 0; i < sudokuGame.getLength(); i++) {
            for (int j = 0; j < sudokuGame.getLength(); j++) {
                cellTiles[i][j] = new CellTile(i, j);
            }
        }
        cells = new CellCollection(cellTiles);
        mSudokuBoard.setCells(cells);
        mSudokuBoard.setTarget(dataResult.getTarget());
        cells.addOnChangeListener(new CellCollection.OnChangeListener() {
            @Override
            public void onChange() {
                invalidateOptionsMenu();
            }
        });

        //input
        mIMControlPanel = (IMControlPanel) findViewById(R.id.input_methods);
        mIMControlPanel.initialize(mSudokuBoard, mHintsQueue);

        mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);

        mIMPopup = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_POPUP);
        mIMSingleNumber = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
        mIMNumpad = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
        mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);

        Log.d(TAG, "calling begin()");
        sudokuGame.begin();

        /*if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
            mSudokuGame.start();
        } else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();
        }

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
            mSudokuBoard.setReadOnly(true);
        }*/

         timerRunnable = new Runnable() {

            @Override
            public void run() {

                mTimeLabel.setText(sudokuGame.getElapsedFormatted());

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(sudokuGame.getName());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // we will save game to the database as we might not be able to get back
        //mDatabase.updateSudoku(mSudokuGame);

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

        mFillInNotesEnabled = gameSettings.getBoolean("fill_in_notes_enabled", false);

        mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean("highlight_wrong_values", true));
        mSudokuBoard.setHighlightTouchedCell(gameSettings.getBoolean("highlight_touched_cell", true));

        mShowTime = gameSettings.getBoolean("show_time", true);

        mTimeLabel.setVisibility(mShowTime ? View.VISIBLE : View.GONE);

        mIMPopup.setEnabled(gameSettings.getBoolean("im_popup", true));
        mIMSingleNumber.setEnabled(gameSettings.getBoolean("im_single_number", true));
        mIMNumpad.setEnabled(gameSettings.getBoolean("im_numpad", true));

        mIMPopup.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMPopup.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        mIMSingleNumber.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMSingleNumber.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        mIMNumpad.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMNumpad.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));

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
                mSudokuBoard.getCells().updateCells();
                return true;
            case MENU_ITEM_FILL_IN_NOTES:
                sudokuGame.showPossibilities();
                mSudokuBoard.getCells().updateCells();
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
                mSudokuBoard.getCells().updateCells();
                return true;
            case MENU_ITEM_REDO:
                sudokuGame.redo();
                mSudokuBoard.getCells().updateCells();
                return true;
            case MENU_ITEM_SHOW_ERROR:
                cell = mSudokuBoard.getSelectedCell();
                if (cell != null) {
                    sudokuGame.showError(cell.getRow(), cell.getCol());
                    mSudokuBoard.getCells().updateCells();
                } else {
                    Toast toast = Toast.makeText(this, R.string.no_cell, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return true;
            case MENU_ITEM_SHOW_ALL_ERRORS:
                sudokuGame.showAllErrors();
                mSudokuBoard.getCells().updateCells();
                return true;
            case MENU_ITEM_SHOW_VALUE:
                cell = mSudokuBoard.getSelectedCell();
                if (cell != null) {
                    sudokuGame.showAnswer(cell.getRow(), cell.getCol());
                    mSudokuBoard.getCells().updateCells();
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
                                mSudokuBoard.getCells().updateCells();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_WELL_DONE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_info)
                        .setTitle(R.string.complete)
                        .setMessage(String.format(getString(R.string.congrats), sudokuGame.getElapsedFormatted(), sudokuGame.getScore()))
                        .setPositiveButton(android.R.string.ok, null)
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
                                mSudokuBoard.getCells().updateCells();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_SAVE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_save)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.save_dialog)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast toast = Toast.makeText(GameActivity.this, "Save not implemented", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
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
    }

}
