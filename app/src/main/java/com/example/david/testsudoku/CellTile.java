package com.example.david.testsudoku;

import com.david.completesudoku.Selectable;

/**
 * Created by David on 4/27/2017.
 */

public class CellTile implements Selectable {

    private int col, row;
    private boolean selected;

    public CellTile(int row, int col) {
        this.row = row;
        this.col = col;
        this.selected = false;
    }

    @Override
    public void setSelected(boolean selected) {
        //todo
    }

    @Override
    public void resolve(Selectable s) {
        //todo
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public boolean isSelected() {
        return selected;
    }
}
