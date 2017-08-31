package com.example.terraria;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

public class Player {
    //Bitmap to get character from image
    private Bitmap bitmap;

    int dir = 0;

    //coordinates
    private int x;
    private int y;
    private int height = 70;
    private int width = 20;
    //motion speed of the character
    private int speed = 1;
    private int tenths = 0;

    int xpixel;

    //constructor
    public Player(Context context,int xtemp,int ytemp,int tempheight,int tempwidth) {
        x = xtemp;
        y = ytemp;
        height = tempheight;
        width = tempwidth;

        //Getting bitmap from drawable resource
        //bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
    }

    //Method to update coordinate of character
    public void update(){
        tenths+=speed*dir;
        if (tenths==-1){
            tenths=9;
            x-=1;
        }
        if (tenths==10){
            tenths=0;
            x+=1;
        }
    }

    /*
    * These are getters you can generate it autmaticallyl
    * right click on editor -> generate -> getters
    * */
    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSpeed() {
        return speed;
    }

    public int getheight() { return height; }

    public int getwidth() { return width; }

    public int gettenths() {return tenths; }
}
