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
    int x_mining_block;
    int y_mining_block;
    ArrayList<block> damaged = new ArrayList<block>();

    // First ind is X, second ind is Y
    // 600 width, 100 height
    block block_char[][];

    int xdiff;

    boolean jumping = false;

    xblock_stat[] xblock_stats = new xblock_stat[10];
    //yblock_stat[] yblock_stats = new yblock_stat[10];

    int block_size = 30;

    Context context;

    class xblock_stat{
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

        player = new Player(context,300,60,block_size*2,block_size);
        player.setxpixel(height/2-player.getwidth()/2);
        player.setypixel(width/2-player.getheight()/2);
        Log.d("debug","ypixel "+player.getypixel());
        /*
        Random ballspawn = new Random();
        ball = new Ball(ballspawn.nextInt(width-200)+100,player.getY()-300);
        */
        block empty = new block(0,Color.TRANSPARENT,Color.TRANSPARENT,1000,1000);
        block dirt = new block(1,Color.GREEN,Color.rgb(153,76,0),100,2);
        block stone = new block(2,Color.BLACK,Color.GRAY,100,2);
        
        block unpassable =  new block(-1, Color.BLACK,Color.BLACK,1000,1000);

        block_char = new block[600][300];

        for (int i=0;i<30;i++){
            for (int k=0;k<block_char.length;k++){
                block_char[k][i] = new block(stone);
            }
        }

        for (int i=30;i<60;i++){
            for (int k=0;k<block_char.length;k++){
                block_char[k][i] = new block(dirt);
            }
        }

        for (int i=60;i<200;i++){
            for (int k=0;k<block_char.length;k++){
                block_char[k][i] = new block(empty);
            }
        }

        for (int i=200;i<300;i++){
            for (int k=0;k<block_char.length;k++){
                block_char[k][i] = new block(unpassable);
            }
        }



        for (int i=500;i<600;i++){
            for (int k=0;k<block_char[0].length;k++){
                block_char[i][k] = new block(unpassable);
                block_char[i-500][k] = new block(unpassable);
            }

        }

        
        for (int i=0;i<10;i++){
            xblock_stats[i] = new xblock_stat();
            int dist_to_right = player.getxpixel()-i*(block_size/10);
            int dist_to_left = dist_to_right;
            dist_to_right = height-dist_to_right;

            int left_blocks = dist_to_left/block_size;
            dist_to_left-=left_blocks*block_size;
            xblock_stats[i].first_block_pixel = dist_to_left-block_size;
            left_blocks++;


            int right_blocks = dist_to_right/block_size;
            dist_to_right-=right_blocks*block_size;
            if (dist_to_right!=0){
                right_blocks++;
            }
            xblock_stats[i].blocks_on_screen = right_blocks+left_blocks;
            xblock_stats[i].left_blocks = left_blocks;
            xblock_stats[i].right_blocks = right_blocks;
        }

        for (int i=0;i<10;i++){
            xblock_stat temp = xblock_stats[i];
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
        double pyta3 = Math.sqrt(Math.pow(xpos-(width-100),2)+Math.pow(ypos-(height/2),2));

        if (e.getAction()==MotionEvent.ACTION_DOWN||e.getAction()==MotionEvent.ACTION_MOVE){
            xdiff = xpos;
            //Log.d("debug","clicked at "+xpos+" "+ypos);
            //Log.d("debug","xblock is "+xpixel_to_indice(ypos)+" yblock is "+ypixel_to_ind(xpos));
            if (pyta1<=35){
                //Log.d("debug","left");
                player.setdx(-player.speed);
                if (block_char[xpixel_to_indice(player.getxpixel()-(player.getdx()*block_size/10))][70].get_id()!=0){
                    //player.dir=0;
                }
            }
            else if (pyta2<=35){
                //Log.d("debug","right");
                player.setdx(player.speed);
                if (block_char[xpixel_to_indice(player.getxpixel()+player.getwidth()+player.getdx()*block_size/10)][70].get_id()!=0){
                  //  player.dir=0;
                }
            }
            else if (pyta3<=35){
                Log.d("debug","jumped");
                jumping = true;
            }
            else{
                mining = true;
                x_mining_block = xpixel_to_indice(ypos);
                y_mining_block = ypixel_to_ind(xpos)+2;
            }
        }
        else if (e.getAction()==MotionEvent.ACTION_UP){
            player.setdx(0);
            xdiff-=xpos;
            if (xdiff>=200){
                Log.d("debug","x pos "+player.getX()+" tenths "+player.getxtenths());
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
        return true;
    }

    //Game Logic
    @Override
    public void run() {
        while (playing) {
            control();
            draw();
            player_update_x();
            player_update_y();
            if (mining){
                block mined_block = block_char[x_mining_block][y_mining_block];
                mined_block.take_damage(player.get_damage());
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

    public int min(int a,int b){
        if (a<b)return a;
        return b;
    }

    //Method to update y_coordinate of character
    public void player_update_y(){
        if (jumping){
            if (player.getdy()==0)player.setdy(10);
            jumping=false;
        }
        int cur_dy = player.getdy();
        //Log.d("debug","current dy "+cur_dy);
        if (cur_dy==0){
            if (player.getytenths()!=0||(block_char[player.getX()][player.getY()-1].get_id()==0&&(player.getxtenths()==0||
                block_char[player.getX()+1][player.getY()-1].get_id()==0))){
                cur_dy-=1;
            }
            player.setdy(cur_dy);
        }
        else if (cur_dy<0&&cur_dy+player.getytenths()<=-1){
            int ypixel_after_move = player.getypixel()+min(cur_dy*block_size/10-1,-1);
            if (block_char[player.getX()][ypixel_to_ind(ypixel_after_move)].get_id()==0&&(player.getxtenths()==0 ||
                    block_char[player.getX()+1][ypixel_to_ind(ypixel_after_move)].get_id()==0)){
                Log.d("debug","should drop "+player.getX()+" "+ypixel_to_ind(ypixel_after_move)+" "+ypixel_after_move+" id "+block_char[player.getX()][ypixel_to_ind(ypixel_after_move)].get_id());
                cur_dy-=1;
            }
            else{
                Log.d("debug","hit bottom");
                player.set_y_tenths(0);
                cur_dy=0;
            }
            player.setdy(cur_dy);
        }
        cur_dy = player.getdy();
        if (cur_dy>0){
            cur_dy-=1;
            int ypixel_after_move = player.getypixel()+2*block_size+cur_dy*block_size/10;
            if (block_char[player.getX()][ypixel_to_ind(ypixel_after_move)].get_id()!=0||(player.getxtenths()!=0 &&
                    block_char[player.getX()+1][ypixel_to_ind(ypixel_after_move)].get_id()!=0)){
                Log.d("debug","hit head");
                cur_dy=0;
            }
            player.setdy(cur_dy);
        }
        int ytenths = player.getytenths();
        ytenths+=player.getdy();
        if (ytenths<=-1){
            ytenths+=10;
            player.setY(player.getY()-1);
        }
        if (ytenths>=10){
            ytenths-=10;
            player.setY(player.getY()+1);
        }
        player.set_y_tenths(ytenths);
    }

    //Method to update x_coordinate of character
    public void player_update_x(){
        int cur_dx = player.getdx();
        if (cur_dx<0&&-cur_dx>player.getxtenths()){
            if (block_char[player.getX()-1][player.getY()].get_id()!=0||
                    block_char[player.getX()-1][player.getY()+1].get_id()!=0){
                Log.d("debug","hit left");
                player.set_x_tenths(0);
                player.setdx(0);
            }
            else if (player.getytenths()!=0&&block_char[player.getX()-1][player.getY()+2].get_id()!=0){
                Log.d("debug","hit left");
                player.set_x_tenths(0);
                player.setdx(0);
            }
        }
        if (cur_dx>0&&(cur_dx+player.getxtenths()>=10||player.getxtenths()==0)){
            if (block_char[player.getX()+1][player.getY()].get_id()!=0||
                    block_char[player.getX()+1][player.getY()+1].get_id()!=0){
                player.set_x_tenths(0);
                player.setdx(0);
            }
            else if (player.getxtenths()!=0&&(block_char[player.getX()+2][player.getY()].get_id()!=0||
                    block_char[player.getX()+2][player.getY()+1].get_id()!=0)){
                Log.d("debug","hit right");
                player.setX(player.getX()+1);
                player.set_x_tenths(0);
                player.setdx(0);
            }
            else if (player.getytenths()!=0&&block_char[player.getX()+1][player.getY()+2].get_id()!=0){
                Log.d("debug","hit right");
                player.set_x_tenths(0);
                player.setdx(0);
            }

        }

        int xtenths = player.getxtenths();
        xtenths+=player.getdx();
        if (xtenths<=-1){
            xtenths+=10;
            player.setX(player.getX()-1);
        }
        if (xtenths>=10){
            xtenths-=10;
            player.setX(player.getX()+1);
        }
        player.set_x_tenths(xtenths);
    }

    public int xpixel_to_indice(int xpixel){
        int xtenths = player.getxtenths();
        int player_first_pix = player.getxpixel()-xtenths*block_size/10;
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
    
    public int ypixel_to_ind(int ypixel){
        //Log.d("debug","called on "+ypixel);
        int ytenths = player.getytenths();
        int player_first_pix = player.getypixel()-ytenths*block_size/10;
        if (ypixel==player_first_pix){
            return player.getY();
        }
        else if (ypixel<player_first_pix){
            int temp = player_first_pix-ypixel;
            return player.getY()-temp/30-1;
        }
        else if (ypixel>player_first_pix){
            player_first_pix+=block_size;
            if (player_first_pix>ypixel){
                return player.getY();
            }
            int temp = ypixel-player_first_pix;
            return temp/block_size+player.getY()+1;
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


            //Drawing player
            paint.setColor(Color.BLUE);
            canvas.drawRect(player.getypixel(),player.getxpixel(),player.getypixel()+player.getheight(),player.getxpixel()+player.getwidth(),paint);
            paint.setColor(Color.GREEN);
            canvas.drawRect(player.getypixel()+2,player.getxpixel()+2,player.getypixel()+player.getheight()-2,player.getxpixel()+player.getwidth()-2,paint);



            int xtenths = player.getxtenths();
            int ytenths = player.getytenths();
            xblock_stat xstat = xblock_stats[xtenths];

            int y_draw_pixel = player.getypixel()-ytenths*block_size/10;
            int y_ind = player.getY();
            while(true) {
                if (y_draw_pixel+block_size<0)break;
                int x_draw_pixel = player.getxpixel() - xtenths * block_size / 10 - block_size;
                for (int i = 0; i < xstat.left_blocks; i++) {
                    block cur_block = block_char[player.getX() - 1 - i][y_ind];
                    paint.setColor(cur_block.get_back_col());
                    canvas.drawRect(y_draw_pixel, x_draw_pixel, y_draw_pixel + block_size, x_draw_pixel + block_size, paint);
                    paint.setColor(cur_block.get_fore_col());
                    canvas.drawRect(y_draw_pixel + 2, x_draw_pixel + 2, y_draw_pixel + block_size - 2, x_draw_pixel + block_size - 2, paint);
                    //canvas.drawRect(left,top,right,bottom)
                    paint.setColor(Color.BLACK);
                    for (int k = 0; k < 4; k++) {
                        if (cur_block.get_cur_hp() < cur_block.get_max_hp() * (4 - k) / 4) {
                            for (int z = 0; z < damage_states[k].length; z++) {
                                canvas.drawRect(
                                        y_draw_pixel + block_size / 2 + damage_states[k][z].left,
                                        x_draw_pixel + block_size / 2 + damage_states[k][z].top,
                                        y_draw_pixel + block_size / 2 + damage_states[k][z].right,
                                        x_draw_pixel + block_size / 2 + damage_states[k][z].bottom, paint);
                            }
                        }
                    }
                    x_draw_pixel -= block_size;
                }


                x_draw_pixel = player.getxpixel() - xtenths * block_size / 10;
                for (int i = 0; i < xstat.right_blocks; i++) {
                    block cur_block = block_char[player.getX() + i][y_ind];
                    paint.setColor(cur_block.get_back_col());
                    canvas.drawRect(y_draw_pixel, x_draw_pixel, y_draw_pixel + block_size, x_draw_pixel + block_size, paint);
                    paint.setColor(cur_block.get_fore_col());
                    canvas.drawRect(y_draw_pixel + 2, x_draw_pixel + 2, y_draw_pixel + block_size - 2, x_draw_pixel + block_size - 2, paint);
                    //canvas.drawRect(left,top,right,bottom)
                    paint.setColor(Color.BLACK);
                    for (int k = 0; k < 4; k++) {
                        if (cur_block.get_cur_hp() < cur_block.get_max_hp() * (4 - k) / 4) {
                            for (int z = 0; z < damage_states[k].length; z++) {
                                canvas.drawRect(
                                        y_draw_pixel + block_size / 2 + damage_states[k][z].left,
                                        x_draw_pixel + block_size / 2 + damage_states[k][z].top,
                                        y_draw_pixel + block_size / 2 + damage_states[k][z].right,
                                        x_draw_pixel + block_size / 2 + damage_states[k][z].bottom, paint);
                            }
                        }
                    }
                    x_draw_pixel += block_size;
                }

                y_draw_pixel-=block_size;
                y_ind--;
            }

            y_draw_pixel = player.getypixel()-ytenths*block_size/10+block_size;
            y_ind = player.getY()+1;

            while(true) {
                if (y_draw_pixel+block_size>width)break;
                int x_draw_pixel = player.getxpixel() - xtenths * block_size / 10 - block_size;
                for (int i = 0; i < xstat.left_blocks; i++) {
                    block cur_block = block_char[player.getX() - 1 - i][y_ind];
                    paint.setColor(cur_block.get_back_col());
                    canvas.drawRect(y_draw_pixel, x_draw_pixel, y_draw_pixel + block_size, x_draw_pixel + block_size, paint);
                    paint.setColor(cur_block.get_fore_col());
                    canvas.drawRect(y_draw_pixel + 2, x_draw_pixel + 2, y_draw_pixel + block_size - 2, x_draw_pixel + block_size - 2, paint);
                    //canvas.drawRect(left,top,right,bottom)
                    paint.setColor(Color.BLACK);
                    for (int k = 0; k < 4; k++) {
                        if (cur_block.get_cur_hp() < cur_block.get_max_hp() * (4 - k) / 4) {
                            for (int z = 0; z < damage_states[k].length; z++) {
                                canvas.drawRect(
                                        y_draw_pixel + block_size / 2 + damage_states[k][z].left,
                                        x_draw_pixel + block_size / 2 + damage_states[k][z].top,
                                        y_draw_pixel + block_size / 2 + damage_states[k][z].right,
                                        x_draw_pixel + block_size / 2 + damage_states[k][z].bottom, paint);
                            }
                        }
                    }
                    x_draw_pixel -= block_size;
                }


                x_draw_pixel = player.getxpixel() - xtenths * block_size / 10;
                for (int i = 0; i < xstat.right_blocks; i++) {
                    block cur_block = block_char[player.getX() + i][y_ind];
                    paint.setColor(cur_block.get_back_col());
                    canvas.drawRect(y_draw_pixel, x_draw_pixel, y_draw_pixel + block_size, x_draw_pixel + block_size, paint);
                    paint.setColor(cur_block.get_fore_col());
                    canvas.drawRect(y_draw_pixel + 2, x_draw_pixel + 2, y_draw_pixel + block_size - 2, x_draw_pixel + block_size - 2, paint);
                    //canvas.drawRect(left,top,right,bottom)
                    paint.setColor(Color.BLACK);
                    for (int k = 0; k < 4; k++) {
                        if (cur_block.get_cur_hp() < cur_block.get_max_hp() * (4 - k) / 4) {
                            for (int z = 0; z < damage_states[k].length; z++) {
                                canvas.drawRect(
                                        y_draw_pixel + block_size / 2 + damage_states[k][z].left,
                                        x_draw_pixel + block_size / 2 + damage_states[k][z].top,
                                        y_draw_pixel + block_size / 2 + damage_states[k][z].right,
                                        x_draw_pixel + block_size / 2 + damage_states[k][z].bottom, paint);
                            }
                        }
                    }
                    x_draw_pixel += block_size;
                }

                y_draw_pixel+=block_size;
                y_ind++;
            }

            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(4);
            paint.setStyle(Paint.Style.STROKE);
            //Drawing buttons for left and right movement
            canvas.drawCircle(width-100,height/2-100,35,paint);
            canvas.drawCircle(width-100,height/2+100,35,paint);
            //Drawing button for jumping
            canvas.drawCircle(width-100,height/2,35,paint);

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

            canvas.drawCircle(width-100,height/2,1,paint);
            canvas.drawLine(width-100+10,height/2,width-100-10,height/2+10,paint);
            canvas.drawLine(width-100+10,height/2,width-100-10,height/2-10,paint);
            canvas.drawLine(width-100-10,height/2+10,width-100-10,height/2-10,paint);


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