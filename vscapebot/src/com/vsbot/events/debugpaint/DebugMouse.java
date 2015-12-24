package com.vsbot.events.debugpaint;

import java.awt.*;

import com.vsbot.api.Methods;

public class DebugMouse extends DebugPaint {

    public DebugMouse(Methods m) {
        super(m);
    }

    @Override
    public void paint(Graphics g) {
        if (methods.getLoader() != null && methods.getLoader().mousePos != null) {
            int x = (int) methods.getLoader().mousePos.getX();
            int y = (int) methods.getLoader().mousePos.getY();
            g.setColor(Color.RED);
            g.translate(2, 2);
            g.drawLine((x - 8), (y - 8), x + 8, y + 8);
            g.drawLine((x - 8), (y + 8), x + 8, y - 8);
            g.drawString("Mouse X: " + x + " Y: " + y, 10, 20);
            g.translate(-8, -2);
        }

    }

}
