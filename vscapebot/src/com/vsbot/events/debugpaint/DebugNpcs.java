package com.vsbot.events.debugpaint;

import java.awt.*;

import com.vsbot.api.Methods;
import com.vsbot.wrappers.RSNPC;

public class DebugNpcs extends DebugPaint {

    public DebugNpcs(Methods m) {
        super(m);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.YELLOW);
        for (RSNPC npc : methods.getAllNpcs()) {
            if (npc != null && npc.getScreenLocation() != null && !npc.getScreenLocation().equals(new Point(-1, -1)) && npc.isOnScreen()) {
                Point p = npc.getScreenLocation();
                g.drawString(Integer.toString(npc.getId()), p.x, p.y);
            }
        }
        g.setColor(Color.WHITE);

    }

}
