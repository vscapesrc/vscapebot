package com.vsbot.events;

import java.awt.*;

import com.vsbot.events.debugpaint.DebugPaint;
import com.vsbot.launcher.Loader;


public class PaintEvent {


    public void paintHack(Graphics g, int hashCode) {
        try {
            for (Loader l : com.vsbot.launcher.Frame.loaders) {
                if (l != null && l.getHook() != null && l.getHook().getGraphics() != null && l.getHook().getGraphics().hashCode() == hashCode) {
                    if (l.getRunningScript() != null && l.getRunningScript().isRunning()) {
                        l.getRunningScript().paint(g);
                    }
                    for (DebugPaint paint : l.getDebugPaints()) {
                        if (paint.isEnabled()) {

                            paint.paint(g);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
