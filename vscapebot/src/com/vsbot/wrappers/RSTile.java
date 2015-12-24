package com.vsbot.wrappers;

public class RSTile {

    int x, y, plane;

    public RSTile(int xa, int ya) {
        this.x = xa;
        this.y = ya;
        this.plane = 0;
    }

    public RSTile(int xa, int ya, int plane) {
        this.x = xa;
        this.y = ya;
        this.plane = plane;
    }


    public String toString() {
        return new String("X: " + x + " Y: " + y);
    }

    public int getPlane() {
        return plane;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


}
