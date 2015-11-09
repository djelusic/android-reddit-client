package com.rael.daniel.drc.fragments;

import android.support.design.widget.FloatingActionButton;

public interface IFragmentCallback {
    public FloatingActionButton getFAB();
    public FloatingActionButton getSubFAB(int position);
    public boolean isStateChanged();
    public void setStateChanged(boolean stateChanged);
}
