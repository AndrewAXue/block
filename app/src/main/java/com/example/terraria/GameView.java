package com.example.terraria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by Andrew on 8/20/2017.
 */

public class GameView extends SurfaceView implements Runnable {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    SharedPreferences shared;

    volatile boolean playing;
    private Thread gameThread = null;

    //adding the player to this class
    private Player player;
    //private Ball ball;

    //These objects will be used for drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    int height;
    int width;


    block_prop block_char[] = new block_prop[100];

    int xdiff;

    block_stat[] block_stats = new block_stat[10];

    int block_size = 30;

    Context context;

    class block_stat{
        int blocks_on_screen;
        int first_block_pixel;
        int left_blocks;
        //right_blocks include block underneath, left does not
        int right_blocks;
    }

    class block_prop{
        int back_col = Color.WHITE;
        int fore_col = Color.BLACK;
        int max_hp;
        int cur_hp;

        block_prop(int back_col_temp,int fore_col_temp,int max_hp_temp){
            back_col = back_col_temp;
            fore_col = fore_col_temp;
            max_hp = max_hp_temp;
            cur_hp = max_hp;
        }
    }

    public GameView(Context tempcontext) {
        super(tempcontext);
        context = tempcontext;

        //initializing player object
        height = Resources.getSystem().getDisplayMetrics().heightPixels;
        width = Resources.getSystem().getDisplayMetrics().widthPixels;
        Log.d("debug","Width is "+width);
        Log.d("debug","Height is "+height);

        player = new Player(context,50,0,70,block_size);
        player.xpixel = height/2-player.getwidth()/2;
        /*
        Random ballspawn = new Random();
        ball = new Ball(ballspawn.nextInt(width-200)+100,player.getY()-300);
        */
        block_prop dirt = new block_prop(Color.GREEN,Color.BLACK,100);
        block_prop stone = new block_prop(Color.BLACK,Color.GRAY,100);
        for (int i=0;i<50;i++){
            block_char[i] = dirt;
        }
        for (int i=50;i<100;i++){
            block_char[i] = stone;
        }

        for (int i=0;i<10;i++){
            block_stats[i] = new block_stat();
            int dist_to_right = player.xpixel-i*(block_size/10);
            int dist_to_left = dist_to_right;
            dist_to_right = height-dist_to_right;

            int left_blocks = dist_to_left/block_size;
            dist_to_left-=left_blocks*block_size;
            block_stats[i].first_block_pixel = dist_to_left-block_size;
            left_blocks++;


            int right_blocks = dist_to_right/block_size;
            dist_to_right-=right_blocks*block_size;
            if (dist_to_right!=0){
                right_blocks++;
            }
            block_stats[i].blocks_on_screen = right_blocks+left_blocks;
            block_stats[i].left_blocks = left_blocks;
            block_stats[i].right_blocks = right_blocks;
        }

        for (int i=0;i<10;i++){
            block_stat temp = block_stats[i];
            Log.d("debug","first pixel "+temp.first_block_pixel+" right blocks "+temp.right_blocks+" left blocks "+temp.left_blocks+" numblock "+temp.blocks_on_screen);
        }

        //initializing drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();
    }

    public boolean onTouchEvent(MotionEvent e) {
        int xpos=(int) e.getX();
        int ypos=(int) e.getY();

        if (e.getAction()==MotionEvent.ACTION_DOWN){
            if (ypos<height/2){
                //player.dir = -1;
            }
            if (ypos>height/2){
                //player.dir = 1;
            }
            xdiff = xpos;
        }
        else if (e.getAction()==MotionEvent.ACTION_UP){
            player.dir = 0;
            xdiff-=xpos;
            if (xdiff>=200){
                Log.d("debug","x pos "+player.getX()+" tenths "+player.gettenths());
            }
            Log.d("debug","block is "+pixel_to_indice(ypos));
            /*
            if (xdiff<=-200){
                player.dir = 1;
                player.update();
                player.dir=0;
            }
            */
        }
        else if (e.getAction()==MotionEvent.ACTION_MOVE){}
        return true;
    }

    @Override
    public void run() {
        while (playing) {
            control();
            draw();
            update();
        }
    }

    private void update() {
        //updating player position
        player.update();
    }

    public int pixel_to_indice(int xpixel){
        int tenths = player.gettenths();
        int player_first_pix = player.xpixel-tenths*block_size/10;
        if (xpixel==player_first_pix){
            return player.getX();
        }
        else if (xpixel<player_first_pix){
            Log.d("debug","player "+player_first_pix+" xpixel "+xpixel);
            int temp = player_first_pix-xpixel;
            return player.getX()-temp/30;
        }
        else if (xpixel>player_first_pix){
            player_first_pix+=block_size;
            if (player_first_pix>xpixel){
                return player.getX();
            }
            int temp = xpixel-player_first_pix;
            return temp/30+player.getX()+1;
        }
        return -1;
    }

    private void draw() {
        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {
            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            //drawing a background color for canvas
            canvas.drawColor(Color.RED);


            final float testTextSize = 24f;
            paint.setTextSize(testTextSize);
            paint.setTypeface(Typeface.create("Arial",Typeface.BOLD));

            paint.setColor(Color.GREEN);
            canvas.drawRect(100+block_size,player.xpixel,100+block_size+player.getheight(),player.xpixel+player.getwidth(),paint);

            int tenths = player.gettenths();
            block_stat stat = block_stats[tenths];

            int draw_left_pixel = player.xpixel-tenths*block_size/10-block_size;
            for (int i=0;i<stat.left_blocks;i++){
                block_prop block = block_char[player.getX()-1-i];
                paint.setColor(block.back_col);
                canvas.drawRect(100,draw_left_pixel,100+block_size,draw_left_pixel+block_size,paint);
                paint.setColor(block.fore_col);
                canvas.drawRect(100+2,draw_left_pixel+2,100+block_size-2,draw_left_pixel+block_size-2,paint);
                draw_left_pixel-=block_size;
            }

            int draw_right_pixel = player.xpixel-tenths*block_size/10;
            for (int i=0;i<stat.right_blocks;i++){
                block_prop block = block_char[player.getX()+i];
                paint.setColor(block.back_col);
                canvas.drawRect(100,draw_right_pixel,100+block_size,draw_right_pixel+block_size,paint);
                paint.setColor(block.fore_col);
                canvas.drawRect(100+2,draw_right_pixel+2,100+block_size-2,draw_right_pixel+block_size-2,paint);
                draw_right_pixel+=block_size;
            }

            //canvas.drawRect(ball.getX(),ball.getY(),ball.getX()+ball.getleng(),ball.getY()+ball.getleng(),paint);
            //Unlocking the canvas
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}