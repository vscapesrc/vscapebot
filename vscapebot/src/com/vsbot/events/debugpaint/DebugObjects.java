package com.vsbot.events.debugpaint;

import java.awt.*;

import com.vsbot.api.Methods;
import com.vsbot.wrappers.RSObject;

public class DebugObjects extends DebugPaint {

    public DebugObjects(Methods m) {
        super(m);
    }

    @Override
    public void paint(Graphics g) {
        for (RSObject go : methods.objects.getAll()) {
            if (go != null) {
                if (methods.calc.pointOnGameScreen(go.getScreenLocation())) {
                    g.drawString("" + go.getId(), (int) go.getScreenLocation().getX(), (int) go.getScreenLocation().getY());
                }
            }
        }

    }

}
