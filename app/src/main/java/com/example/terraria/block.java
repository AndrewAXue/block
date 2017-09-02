package com.example.terraria;

import android.graphics.Color;

/**
 * Created by Andrew on 8/31/2017.
 */

class block{
    private int block_id;
    private int back_col = Color.WHITE;
    private int fore_col = Color.BLACK;
    private int max_hp;
    private int cur_hp;
    private int recover_rate;

    block(int temp_block_id, int back_col_temp,int fore_col_temp,int max_hp_temp,int temp_recover_rate){
        block_id = temp_block_id;
        back_col = back_col_temp;
        fore_col = fore_col_temp;
        max_hp = max_hp_temp;
        cur_hp = max_hp_temp;
        recover_rate = temp_recover_rate;
    }

    block(block copy){
        block_id = copy.get_id();
        back_col = copy.get_back_col();
        fore_col = copy.get_fore_col();
        max_hp = copy.get_max_hp();
        cur_hp = copy.get_cur_hp();
        recover_rate = copy.get_recover_rate();
    }

    public void take_damage(int damage){
        cur_hp-=damage;
        if (cur_hp<0){
            block_id = 0;
            back_col = Color.TRANSPARENT;
            fore_col = Color.TRANSPARENT;
            max_hp = 1000;
            cur_hp = 1000;
            recover_rate = 1000;
        }
    }

    public void recover(){
        cur_hp+=recover_rate;
        if (cur_hp>max_hp) cur_hp=max_hp;
    }

    public int get_id() { return block_id; }

    public int get_cur_hp(){
        return cur_hp;
    }

    public int get_max_hp(){ return max_hp; }

    public int get_recover_rate(){ return recover_rate; }

    public int get_fore_col(){
        return fore_col;
    }

    public int get_back_col(){
        return back_col;
    }
}
