package com.example.anthony.tictactoe;

public class Singleton {

    private static Singleton single_instance = null;

    private int flag = 0;
    private String otherPlayerNumber;

    public void setFlag(int n) {
        flag = n;
    }

    public int getFlag() {
        return flag;
    }

    public void setOtherPlayerNumber(String in) {
        otherPlayerNumber = in;
    }

    public String getOtherPlayerNumber() {
        return otherPlayerNumber;
    }

    // private constructor restricted to this class itself
    private Singleton() {}

    // static method to create instance of Singleton class
    public static Singleton getInstance() {
        if (single_instance == null)
            single_instance = new Singleton();

        return single_instance;
    }
}
