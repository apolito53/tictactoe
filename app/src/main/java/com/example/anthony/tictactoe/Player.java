package com.example.anthony.tictactoe;

import android.graphics.drawable.Drawable;

public class Player {
    DataCell[] cells = new DataCell[9];
    String name;
    int symbol;

    public Player(String name, int symbol) {
        this.name = name;
        this.symbol = symbol;
        System.out.println(name);
    }

    public void markCell(int n) {
        cells[n].notifyListners();
    }

    public void register(TTTButton btn, int n) {
        cells[n] = new DataCell();
        cells[n].symbol = symbol;
        cells[n].registerObserver(btn);
        markCell(n);
    }
}