package com.example.david.testsudoku;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.david.completesudoku.Sudoku;
import com.david.completesudoku.SudokuGame;
import com.david.completesudoku.SudokuGenerator;
import com.david.completesudoku.Selectable;
import com.example.david.testsudoku.inputmethod.IMControlPanel;
import com.example.david.testsudoku.inputmethod.IMControlPanelStatePersister;
import com.example.david.testsudoku.inputmethod.IMNumpad;

public class Game extends AppCompatActivity {

    public static final String EXTRA_SUDOKU_ID = "sudoku_id";

    public static final int MENU_ITEM_RESTART = Menu.FIRST;
    public static final int MENU_ITEM_CLEAR_ALL_NOTES = Menu.FIRST + 1;
    public static final int MENU_ITEM_FILL_IN_NOTES = Menu.FIRST + 2;
    public static final int MENU_ITEM_UNDO = Menu.FIRST + 3;
    public static final int MENU_ITEM_HELP = Menu.FIRST + 4;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 5;

    public static final int MENU_ITEM_SET_CHECKPOINT = Menu.FIRST + 6;
    public static final int MENU_ITEM_UNDO_TO_CHECKPOINT = Menu.FIRST + 7;

    private static final int DIALOG_RESTART = 1;
    private static final int DIALOG_WELL_DONE = 2;
    private static final int DIALOG_CLEAR_NOTES = 3;
    private static final int DIALOG_UNDO_TO_CHECKPOINT = 4;

    private static final int REQUEST_SETTINGS = 1;

    private Handler mGuiHandler;

    private ViewGroup mRootLayout;
    private TextView mTimeLabel;

    private IMControlPanel mIMControlPanel;
    private IMControlPanelStatePersister mIMControlPanelStatePersister;
    //private IMPopup mIMPopup;
    //private IMSingleNumber mIMSingleNumber;
    private IMNumpad mIMNumpad;

    //time
    private boolean mShowTime = true;
    /*private GameTimer mGameTimer;
    private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();*/

    private boolean mFullScreen;
    private boolean mFillInNotesEnabled = false;

    private HintsQueue mHintsQueue;

    private SudokuBoardView mSudokuBoard;
    private SudokuGenerator sudokuGenerator;
    private SudokuGame sudokuGame;
    private CellCollection cells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // go fullscreen for devices with QVGA screen (only way I found
        // how to fit UI on the screen)
        Display display = getWindowManager().getDefaultDisplay();
        if ((display.getWidth() == 240 || display.getWidth() == 320)
                && (display.getHeight() == 240 || display.getHeight() == 320)) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mFullScreen = true;
        }

        // theme must be set before setContentView
        //AndroidUtils.setThemeFromPreferences(this);

        setContentView(R.layout.activity_game);

        mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
        mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);
        mTimeLabel = (TextView) findViewById(R.id.time_label);

        mHintsQueue = new HintsQueue(this);
        //mGameTimer = new GameTimer();

        mGuiHandler = new Handler();

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

        sudokuGame = new SudokuGame(new Sudoku(model));
        mSudokuBoard.setSudokuGame(sudokuGame);
        CellTile[][] cellTiles = new CellTile[sudokuGame.getLength()][sudokuGame.getLength()];
        for (int i = 0; i < sudokuGame.getLength(); i++) {
            for (int j = 0; j < sudokuGame.getLength(); j++) {
                cellTiles[i][j] = new CellTile(i, j);
            }
        }
        cells = new CellCollection(cellTiles);
        mSudokuBoard.setCells(cells);

        //input
        mIMControlPanel = (IMControlPanel) findViewById(R.id.input_methods);
        mIMControlPanel.initialize(mSudokuBoard, mHintsQueue);

        mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);

        //mIMPopup = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_POPUP);
        //mIMSingleNumber = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
        mIMNumpad = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
        mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);

        //resetLabels();
        sudokuGame.begin();

        /*if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
            mSudokuGame.start();
        } else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();
        }

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
            mSudokuBoard.setReadOnly(true);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();

        // we will save game to the database as we might not be able to get back
        //mDatabase.updateSudoku(mSudokuGame);

        //mGameTimer.stop();
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
        /*if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();

            if (mShowTime) {
                mGameTimer.start();
            }
        }*/
        mTimeLabel.setVisibility(mFullScreen && mShowTime ? View.VISIBLE : View.GONE);

        //mIMPopup.setEnabled(gameSettings.getBoolean("im_popup", true));
        //mIMSingleNumber.setEnabled(gameSettings.getBoolean("im_single_number", true));
        mIMNumpad.setEnabled(gameSettings.getBoolean("im_numpad", true));
        mIMNumpad.setMoveCellSelectionOnPress(gameSettings.getBoolean("im_numpad_move_right", false));
        //mIMPopup.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        //mIMPopup.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        //mIMSingleNumber.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        //mIMSingleNumber.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        mIMNumpad.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMNumpad.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));

        mIMControlPanel.activateFirstInputMethod(); // make sure that some input method is activated
        mIMControlPanelStatePersister.restoreState(mIMControlPanel);

        //updateTime();
    }
}
