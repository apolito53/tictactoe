package com.example.anthony.tictactoe;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class DataCell implements Observable {
    ArrayList<TTTButton> btns = new ArrayList<>();
    int symbol;

    public void registerObserver(TTTButton btn) {
        btns.add(btn);
    }

    public void notifyListners() {
        for(TTTButton btn : btns) {
            btn.update(symbol);
        }
    }
}
