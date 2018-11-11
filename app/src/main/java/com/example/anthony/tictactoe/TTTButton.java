package com.example.anthony.tictactoe;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class TTTButton extends android.support.v7.widget.AppCompatImageButton implements Observer {
    int index = 0;
    String status = "";
    String name;
    int s;
    public TTTButton(Context context) {
        super(context);
    }

    public TTTButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TTTButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void update(int s) {
        status = "1";
        this.s = s;
        setImageResource(s);

        System.out.println("Index: " + index);
        System.out.println("Symbol: " + s);
    }

    public String getText() {
        return String.valueOf(s);
    }

    public String getStatus() {
        return status;
    }

    public void setName(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

}
