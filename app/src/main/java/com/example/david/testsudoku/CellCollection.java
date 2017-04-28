package com.example.david.testsudoku;

import java.util.ArrayList;
import java.util.List;

public class CellCollection {

    private CellTile[][] cellTiles;

    private boolean mOnChangeEnabled = true;

    private final List<OnChangeListener> mChangeListeners = new ArrayList<>();

    public CellCollection(CellTile[][] cellTiles) {
        this.cellTiles = cellTiles;
    }

    public CellTile getCellTile(int row, int column) {
        return cellTiles[row][column];
    }

    public void updateCells() {
        onChange();
    }

    public void addOnChangeListener(OnChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener is null.");
        }
        synchronized (mChangeListeners) {
            if (mChangeListeners.contains(listener)) {
                throw new IllegalStateException("Listener " + listener + "is already registered.");
            }
            mChangeListeners.add(listener);
        }
    }

    public void removeOnChangeListener(OnChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener is null.");
        }
        synchronized (mChangeListeners) {
            if (!mChangeListeners.contains(listener)) {
                throw new IllegalStateException("Listener " + listener + " was not registered.");
            }
            mChangeListeners.remove(listener);
        }
    }

    /**
     * Returns whether change notification is enabled.
     *
     * If true, change notifications are distributed to the listeners
     * registered by {@link #addOnChangeListener(OnChangeListener)}.
     *
     * @return
     */
//	public boolean isOnChangeEnabled() {
//		return mOnChangeEnabled;
//	}
//
//	/**
//	 * Enables or disables change notifications, that are distributed to the listeners
//	 * registered by {@link #addOnChangeListener(OnChangeListener)}.
//	 *
//	 * @param onChangeEnabled
//	 */
//	public void setOnChangeEnabled(boolean onChangeEnabled) {
//		mOnChangeEnabled = onChangeEnabled;
//	}

    /**
     * Notify all registered listeners that something has changed.
     */
    protected void onChange() {
        if (mOnChangeEnabled) {
            synchronized (mChangeListeners) {
                for (OnChangeListener l : mChangeListeners) {
                    l.onChange();
                }
            }
        }
    }

    public interface OnChangeListener {
        /**
         * Called when anything in the collection changes (cell's value, note, etc.)
         */
        void onChange();
    }
}
