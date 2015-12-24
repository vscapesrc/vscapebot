package com.vsbot.wrappers;

import java.awt.*;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;
import com.vsbot.hooks.Entity;
import com.vsbot.input.Mouse;

public abstract class RSEntity {


    protected abstract com.vsbot.hooks.Entity getAccessor();

    protected abstract Methods getMethods();

    public Mouse mouse() {
        return getMethods().mouse;
    }

    public void setMouse(Mouse mouse) {
    }

    public boolean isInCombat() {
        return getMethods().getHook().getTime() < getAccessor().getLoopCycle();
    }

    public int getLoopCycle() {
        return getAccessor().getLoopCycle();
    }

    public RSEntity getInteracting() {
        int interact = getAccessor().getInteracting();
        if (interact == -1) {
            return null;
        }
        if (interact < 32768) {
            return new RSNPC(interact);
        } else {
            interact -= 32768;
            if (interact == getMethods().getHook().getPlayerId()) {
                return getMethods().getMyPlayer();
            }
            return new RSPlayer(getMethods().getHook().getPlayers()[interact], getMethods());
        }
    }

    public int getCurrentHealth() {
        return getAccessor().getCurrentHealth();
    }

    public int getMaxHealth() {
        return getAccessor().getMaxHealth();
    }

    public boolean isOnScreen() {
        return getMethods().calc.pointOnGameScreen(getScreenLocation());
    }

    public void interact(String action) {
        Point p = getScreenLocation();
        if (isOnScreen()) {
            if (getMethods().menu.isOpen()) {
                getMethods().mouse.moveMouse(1, 1);
            }

            mouse().moveMouse(getScreenLocation());
            try {
                Thread.sleep(60);
            } catch (Exception e) {

            }
            String actions[] = getMethods().menu.getValidMenuActions();

            for (int i = 0; i < actions.length; i++) {
                actions[i] = actions[i].toLowerCase();
            }
            action = action.toLowerCase();
            if (actions[0] != null && actions[0].contains(action)) {
                try {
                    mouse().moveMouse(p);
                    Thread.sleep(30);
                    mouse().clickMouse(p, true);
                    Thread.sleep(30);
                    return;
                } catch (Exception e) {

                }
            }
            mouse().moveMouse(p);
            mouse().clickMouse(p, false);
            getMethods().sleep(300);
            getMethods().menu.interact(action);
        } else {
            Point mm = getMethods().calc.tileToMinimap(getLocation());
            if (mm.x != -1 && mm.y != -1)
                mouse().clickMouse(getMethods().calc.tileToMinimap(getLocation()), true);
        }
    }

    public Point getScreenLocation() {
        return getMethods().calc.worldToScreen(getAccessor().getX() - 5, getAccessor()
                .getY(), getAccessor().getHeight() / 2);
    }

    public String getName() {
        return "-1";
    }

    public int getHeight() {
        return getAccessor().getHeight();
    }


    public RSTile getLocation() {
        Entity c = getAccessor();
        if (c == null) {
            return new RSTile(-1, -1);
        }
        int x = getMethods().getHook().getBaseX() + (c.getX() >> 7);
        int y = getMethods().getHook().getBaseY() + (c.getY() >> 7);
        Client cl = getMethods().getHook();
        return new RSTile(x, y);
        // alternative:
        // return new RSTile(getAccessor().getSmallX()[0] +
        // getMethods().getHook().getBaseX(), getAccessor().getSmallY()[0] +
        // getMethods().getHook().getBaseY());
    }

    public int getX() {
        return getAccessor().getX();
    }

    public int getAnimation() {
        return getAccessor().getAnimation();
    }

    public int getY() {
        return getAccessor().getY();
    }

}
