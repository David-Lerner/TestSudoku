package com.example.david.testsudoku;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.david.completesudoku.Sudoku;
import com.david.completesudoku.SudokuGame;
import com.david.completesudoku.SudokuGenerator;
import com.david.completesudoku.Selectable;

public class Game extends AppCompatActivity {

    private SudokuBoardView mSudokuBoard;
    private SudokuGenerator sudokuGenerator;
    private SudokuGame sudokuGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);

        //D: sud etc.
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
        mSudokuBoard.setCellTiles(cellTiles);
        //resetLabels();
        sudokuGame.begin();
    }
}
