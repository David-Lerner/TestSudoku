package com.example.david.testsudoku;

import com.david.completesudoku.Selectable;

/**
 * Created by David on 4/27/2017.
 */

public class CellTile implements Selectable {

    private int targetI, targetJ;
    private boolean selected;

    public CellTile(int targetI, int targetJ) {
        this.targetI = targetI;
        this.targetJ = targetJ;
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

    public int getTargetI() {
        return targetI;
    }

    public int getTargetJ() {
        return targetJ;
    }

    public boolean isSelected() {
        return selected;
    }
}
