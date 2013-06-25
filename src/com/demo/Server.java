package com.demo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Server {
    
    public static User login(String name ,String password){
        User user = new User();
        user.name = "test";
        user.imei = "12343141324";
        return user;
    }
    
    public static boolean register(String name ,String password ,String imei){
        return false;
    }
    
    
    public static String[] getHost(User user){
        String[] address = new String[2];
        address[0] = "ip";
        address[1] = "port";
        return address;
    }
    
    public static void send(String message) throws IOException {
        send(CONTENT_TYPE_MESSAGE ,message.getBytes());
    }
    
    public static void send(File file) throws IOException {
        if(!file.exists()){
            throw new IOException("file " + file.getAbsolutePath() + " doesn't exist.");
        }
        String fileName = file.getName();
        String contentType = null;
        if(fileName.endsWith(".jpg")){
            contentType = CONTENT_TYPE_IMAGE;
        }else if(fileName.endsWith(".mp4")){
            contentType = CONTENT_TYPE_AUDIO;
        }else{
            contentType = CONTENT_TYPE_BINARY_FILE;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(50 * 1024);
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        int read = -1;
        while((read = is.read()) != -1){
            bos.write(read);
        }
        is.close();
        send(contentType ,bos.toByteArray());
        bos.close();
    }
    
    private static String CONTENT_TYPE_IMAGE = "image";
    private static String CONTENT_TYPE_AUDIO = "audio";
    private static String CONTENT_TYPE_BINARY_FILE = "binary_file";
    private static String CONTENT_TYPE_MESSAGE = "message";
    /**
     * 发送
     * @param type
     * @param content
     * @throws IOException
     */
    private static void send(String type ,byte[] content) throws IOException{
        
    }

}
