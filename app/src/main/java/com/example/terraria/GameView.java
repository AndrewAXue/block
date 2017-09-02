package com.example.terraria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
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

    boolean mining = false;
    int mining_block;
    ArrayList<block> damaged = new ArrayList<block>();

    // First ind is X, second ind is Y
    block block_char[] = new block[100];

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

    class damaged_coord{
        int left,top,right,bottom;
        damaged_coord(int temp_left,int temp_top,int temp_right,int temp_bottom){
            left = temp_left;
            right = temp_right;
            top = temp_top;
            bottom = temp_bottom;
        }
    }

    damaged_coord damage_states[][] = {
    {
        new damaged_coord(-3,-3,0,0),
        new damaged_coord(0,0,3,3)
    },
    {
        new damaged_coord(-6,-3,-3,0),

        new damaged_coord(3,-3,6,0),

        new damaged_coord(3,3,6,6),
        new damaged_coord(3,6,6,9),

        new damaged_coord(-3,3,0,6)
    },
    {
        new damaged_coord(-6,-6,-3,-3),
        new damaged_coord(0,-6,3,-3),
        new damaged_coord(6,-6,9,-3),

        new damaged_coord(6,0,9,3),
        new damaged_coord(6,6,9,9),

        new damaged_coord(-6,6,-3,9),
        new damaged_coord(-9,6,-6,9),

        new damaged_coord(-9,0,-6,3)
    },
    {
        new damaged_coord(-12,-12,-9,-9),
        new damaged_coord(-9,-9,-6,-6),

        new damaged_coord(-3,-9,0,-6),
        new damaged_coord(3,-9,6,-6),
        new damaged_coord(9,-9,12,-6),

        new damaged_coord(9,-3,12,0),
        new damaged_coord(9,3,12,6),
        new damaged_coord(9,9,12,12),

        new damaged_coord(0,9,3,12),
        new damaged_coord(-6,9,-3,12),
        new damaged_coord(-12,9,-9,12),

        new damaged_coord(-12,3,-9,6),
        new damaged_coord(-12,-3,-9,0)
    }
    };

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
        block empty = new block(0,Color.TRANSPARENT,Color.TRANSPARENT,1000,1000);
        block dirt = new block(1,Color.GREEN,Color.rgb(153,76,0),100,2);
        block stone = new block(2,Color.BLACK,Color.GRAY,100,2);
        for (int i=0;i<50;i++){
            block_char[i] = new block(dirt);
        }
        for (int i=50;i<100;i++){
            block_char[i] = new block(stone);
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

        double pyta1 = Math.sqrt(Math.pow(xpos-(width-100),2)+Math.pow(ypos-(height/2-100),2));
        double pyta2 = Math.sqrt(Math.pow(xpos-(width-100),2)+Math.pow(ypos-(height/2+100),2));
        Log.d("debug",pyta1+" "+pyta2);

        if (e.getAction()==MotionEvent.ACTION_DOWN){
            xdiff = xpos;
            if (pyta1<=35){
                Log.d("debug","left");
                player.setdx(-1);
                if (block_char[pixel_to_indice(player.xpixel-(+player.getSpeed()*block_size/10))].get_id()!=0){
                //    player.dir=0;
                }
            }
            else if (pyta2<=35){
                Log.d("debug","right");
                player.setdx(1);
                if (block_char[pixel_to_indice(player.xpixel+player.getwidth()+player.getSpeed()*block_size/10)].get_id()!=0){
                  //  player.dir=0;
                }
            }
            else{
                mining = true;
                mining_block = pixel_to_indice(ypos);
            }
        }
        else if (e.getAction()==MotionEvent.ACTION_UP){
            player.setdx(0);
            xdiff-=xpos;
            if (xdiff>=200){
                Log.d("debug","x pos "+player.getX()+" tenths "+player.gettenths());
            }
            //Log.d("debug","block is "+pixel_to_indice(ypos));
            mining = false;
            /*
            if (xdiff<=-200){
                player.dir = 1;
                player.update();
                player.dir=0;
            }
            */
        }
        else if (e.getAction()==MotionEvent.ACTION_MOVE){
            mining_block = pixel_to_indice(ypos);
        }
        return true;
    }

    @Override
    public void run() {
        while (playing) {
            control();
            draw();
            update();
            if (mining){
                block mined_block = block_char[mining_block];
                mined_block.take_damage(4);
                if(!damaged.contains(mined_block)){
                    damaged.add(mined_block);
                }
                //Log.d("debug",mining_block+" take damage to "+block_char[mining_block].get_cur_hp());
            }
            for (int i=0;i<damaged.size();i++){
                block current_block = damaged.get(i);
                current_block.recover();
                if (current_block.get_cur_hp()==current_block.get_max_hp()){
                    damaged.remove(i);
                    i--;
                }
            }

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
            int temp = player_first_pix-xpixel;
            return player.getX()-temp/30-1;
        }
        else if (xpixel>player_first_pix){
            player_first_pix+=block_size;
            if (player_first_pix>xpixel){
                return player.getX();
            }
            int temp = xpixel-player_first_pix;
            return temp/block_size+player.getX()+1;
        }
        return -1;
    }

    private void draw() {
        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {
            Paint.Style default_style = paint.getStyle();
            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            //drawing a background color for canvas
            canvas.drawColor(Color.RED);

            final float testTextSize = 24f;
            paint.setTextSize(testTextSize);
            paint.setTypeface(Typeface.create("Arial",Typeface.BOLD));

            paint.setColor(Color.BLUE);
            canvas.drawRect(100+block_size,player.xpixel,100+block_size+player.getheight(),player.xpixel+player.getwidth(),paint);
            paint.setColor(Color.GREEN);
            canvas.drawRect(100+block_size+2,player.xpixel+2,100+block_size+player.getheight()-2,player.xpixel+player.getwidth()-2,paint);


            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(width-100,height/2-100,35,paint);
            canvas.drawCircle(width-100,height/2+100,35,paint);

           // canvas.drawLine(startX,startY,stopX,stopY,paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(width-100,height/2-100,1,paint);
            canvas.drawLine(width-100,height/2-100-10,width-100+10,height/2-100+10,paint);
            canvas.drawLine(width-100,height/2-100-10,width-100-10,height/2-100+10,paint);
            canvas.drawLine(width-100-10,height/2-100+10,width-100+10,height/2-100+10,paint);

            canvas.drawCircle(width-100,height/2+100,1,paint);
            canvas.drawLine(width-100,height/2+100+10,width-100+10,height/2+100-10,paint);
            canvas.drawLine(width-100,height/2+100+10,width-100-10,height/2+100-10,paint);
            canvas.drawLine(width-100-10,height/2+100-10,width-100+10,height/2+100-10,paint);


            paint.setStyle(default_style);


            int tenths = player.gettenths();
            block_stat stat = block_stats[tenths];

            int draw_pixel = player.xpixel-tenths*block_size/10-block_size;

            for (int i=0;i<stat.left_blocks;i++){
                block block = block_char[player.getX()-1-i];
                paint.setColor(block.get_back_col());
                canvas.drawRect(100,draw_pixel,100+block_size,draw_pixel+block_size,paint);
                paint.setColor(block.get_fore_col());
                canvas.drawRect(100+2,draw_pixel+2,100+block_size-2,draw_pixel+block_size-2,paint);
                //canvas.drawRect(left,top,right,bottom)
                paint.setColor(Color.BLACK);
                for (int k=0;k<4;k++){
                    if (block.get_cur_hp()<block.get_max_hp()*(4-k)/4){
                        for (int z=0;z<damage_states[k].length;z++){
                            canvas.drawRect(
                                100+block_size/2+damage_states[k][z].left,
                                draw_pixel+block_size/2+damage_states[k][z].top,
                                100+block_size/2+damage_states[k][z].right,
                                draw_pixel+block_size/2+damage_states[k][z].bottom,paint);
                        }
                    }
                }
                draw_pixel-=block_size;
            }


            draw_pixel = player.xpixel-tenths*block_size/10;
            for (int i=0;i<stat.right_blocks;i++){
                block block = block_char[player.getX()+i];
                paint.setColor(block.get_back_col());
                canvas.drawRect(100,draw_pixel,100+block_size,draw_pixel+block_size,paint);
                paint.setColor(block.get_fore_col());
                canvas.drawRect(100+2,draw_pixel+2,100+block_size-2,draw_pixel+block_size-2,paint);
                //canvas.drawRect(left,top,right,bottom)
                paint.setColor(Color.BLACK);
                for (int k=0;k<4;k++){
                    if (block.get_cur_hp()<block.get_max_hp()*(4-k)/4){
                        for (int z=0;z<damage_states[k].length;z++){
                            canvas.drawRect(
                                    100+block_size/2+damage_states[k][z].left,
                                    draw_pixel+block_size/2+damage_states[k][z].top,
                                    100+block_size/2+damage_states[k][z].right,
                                    draw_pixel+block_size/2+damage_states[k][z].bottom,paint);
                        }
                    }
                }
                draw_pixel+=block_size;
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