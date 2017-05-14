package com.example.david.testsudoku;

import com.david.completesudoku.SudokuGame;
import com.david.completesudoku.SudokuModel;
import android.os.Handler;

import java.util.Random;

public class TestModel implements SudokuModel {
    @Override
    public void saveGame (final SudokuGame sudokuGame, Object param) {
        final Handler saveHandler = (Handler) param;
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    Thread.sleep(1000);
                    if (new Random(System.currentTimeMillis()).nextBoolean()) {
                        throw new Exception();
                    }
                    if (sudokuGame.getStatus().equals(SudokuGame.COMPLETED)) {
                        saveHandler.sendEmptyMessage(GameActivity.SAVE_AND_QUIT);
                    } else {
                        saveHandler.sendEmptyMessage(GameActivity.SAVE_SUCCESS);
                    }
                } catch (Exception e) {
                    saveHandler.sendEmptyMessage(GameActivity.SAVE_FAILURE);
                }
            }
        }).start();
    }

    @Override
    public SudokuGame loadGame(Object param) {
        return null;
    }
}
