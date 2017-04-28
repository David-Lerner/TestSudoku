package com.example.david.testsudoku;

import com.david.completesudoku.SudokuGame;

/**
 * Singleton pattern.
 *
 */
public class DataResult {

    private static DataResult instance;
    private SudokuGame sudokuGame = null;

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
}