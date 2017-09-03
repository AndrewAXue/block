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

    //coordinates
    private int x;
    private int y;
    private int height = 60;
    private int width = 20;
    //motion speed of the character
    private int dx = 0;
    private int dy = 0;
    private int xtenths = 0;
    private int ytenths = 0;

    private int damage = 10;

    private int xpixel;
    private int ypixel;

    //constructor
    public Player(Context context,int xtemp,int ytemp,int tempheight,int tempwidth) {
        x = xtemp;
        y = ytemp;
        height = tempheight;
        width = tempwidth;

        //Getting bitmap from drawable resource
        //bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
    }

    /*
    These are getters you can generate it automatically
    right click on editor -> generate -> getters
    */
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setX(int tempx) { x=tempx; }

    public int getX() {
        return x;
    }

    public void set_x_tenths(int temp_x_tenths) { xtenths=temp_x_tenths; }

    public int get_x_tenths() {
        return xtenths;
    }

    public void setdx(int tempdx){ dx = tempdx; }

    public int getdx() { return dx; }

    public void setdy(int tempdy){ dy = tempdy; }

    public int getdy() { return dy; }

    public void setY(int tempy) { y=tempy; }

    public int getY() {
        return y;
    }

    public void set_y_tenths(int temp_y_tenths) { ytenths=temp_y_tenths; }

    public int get_y_tenths() {
        return ytenths;
    }

    public int getheight() { return height; }

    public int getwidth() { return width; }

    public int getxtenths() { return xtenths; }

    public int getytenths() { return ytenths; }

    public void setxpixel(int tempxpixel) { xpixel = tempxpixel; }

    public int getxpixel() { return xpixel; }

    public void setypixel(int tempypixel) { ypixel = tempypixel; }

    public int getypixel() { return ypixel; }

    public void setdamage(int temp_damage) { damage = temp_damage; }

    public int get_damage() { return damage; }
}
