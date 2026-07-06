package com.example.bablueza.g2p;

/**
 * Created by Bablueza on 19/8/2559.
 */
public class g2pJni {
    static
    {
        System.loadLibrary("ttsvaja");

    }
    public native int initG2P(String initFile,String path,String user);
    public native String getPhoneme(String text);
    public native void deallocateG2P();
}

