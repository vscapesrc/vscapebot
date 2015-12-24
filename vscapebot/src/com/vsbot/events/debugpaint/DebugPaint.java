package com.vsbot.events.debugpaint;

import java.awt.*;

import com.vsbot.api.Methods;

public abstract class DebugPaint {

    Methods methods;

    public DebugPaint(Methods m) {
        this.methods = m;
    }

    public abstract void paint(Graphics g);

    private boolean isEnabled = false;

    public void setEnabled(boolean which) {
        isEnabled = which;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

}
