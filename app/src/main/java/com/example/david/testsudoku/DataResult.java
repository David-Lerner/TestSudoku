package com.example.david.testsudoku;

import com.david.completesudoku.Sudoku;
import com.david.completesudoku.SudokuGame;
import com.david.completesudoku.SudokuModel;

/**
 * Singleton pattern.
 *
 */
public class DataResult {

    private static DataResult instance;
    private SudokuGame sudokuGame = null;
    private SudokuModel sudokuModel = null;
    private int target = 0;

    protected DataResult() {

    }

    public static DataResult getInstance() {
        if (instance == null) {
            instance = new DataResult();
        }
        return instance;
    }

    public SudokuGame getSudokuGame() {
        return sudokuGame;
    }

    public void setSudokuGame(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public SudokuModel getSudokuModel() {
        return sudokuModel;
    }

    public void setSudokuModel(SudokuModel sudokuModel) {
        this.sudokuModel = sudokuModel;
    }
}