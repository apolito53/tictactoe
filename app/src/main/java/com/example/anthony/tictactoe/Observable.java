package com.example.anthony.tictactoe;

public interface Observable {
    static void notifyListeners() {}

    static void registerObserver(Observer o) {}
}
