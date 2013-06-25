package com.demo;

import java.io.File;

public class Content {
    
    String message;
    File image;
    File audio;
    File file;
    
    boolean send;
    
    public void clear(){
        message = null;
        image = null;
        audio = null;
        file = null;
    }
}
