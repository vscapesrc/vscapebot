package com.vsbot.events.debugpaint;

import java.awt.*;

import com.vsbot.api.Methods;
import com.vsbot.wrappers.RSItem;

public class DebugInventory extends DebugPaint {

    public DebugInventory(Methods m) {
        super(m);
    }

    @Override
    public void paint(Graphics g) {
        for (RSItem i : methods.inventory.getItems()) {
            if (i != null) {
                g.drawString("" + i.getId(), i.getScreenLocation().x, i.getScreenLocation().y);
            }
        }

    }

}
