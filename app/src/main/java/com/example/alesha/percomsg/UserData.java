package com.example.alesha.percomsg;

import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by Alesha on 7/9/2018.
 */

public class UserData {

        private int size=0;
        Uri uri;
        private String first_name, last_name, bio;
        private ArrayList<String> hobbyArr;

        public UserData(){

        }



    public UserData(String fn, String ln, String b, ArrayList<String> arr){
        bio = b;
        first_name = fn;
        last_name = ln;
        hobbyArr = arr;
    }
    public String getFirst_name() {
            return first_name;
        }

    public String getBio() {return bio;}


    public ArrayList<String> getHobbyArr() {
        return hobbyArr;
    }
    public String getLast_name() {
            return last_name;
        }
 }






