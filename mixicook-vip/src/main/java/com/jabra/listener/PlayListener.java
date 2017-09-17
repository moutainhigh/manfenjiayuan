package com.jabra.listener;

public abstract interface PlayListener {
    public abstract void onComplete();

    public abstract void onError();
}
