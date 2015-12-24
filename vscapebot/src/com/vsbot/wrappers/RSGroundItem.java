package com.vsbot.wrappers;

import java.awt.*;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;
import com.vsbot.hooks.ItemDef;
import com.vsbot.input.Mouse;

public class RSGroundItem {
    private int id;
    private RSTile location;
    private ItemDef itemDef;
    Methods methods;
    Client hook;
    Mouse m;

    public RSGroundItem(int theId, RSTile loc, Methods m) {
        this.id = theId;
        this.location = loc;
        this.methods = m;
        this.hook = methods.getHook();
        this.m = methods.mouse;
        this.itemDef = hook.getForId(id);
    }

    public int getId() {
        return id;
    }

    public RSTile getLocation() {
        return location;
    }

    public String getName() {
        return itemDef.getName();
    }


    public Point getScreenLocation() {
        Point screenPoint = methods.calc.worldToScreen(((double) (getLocation()
                .getX() - hook.getBaseX()) + 0.5D) * 128D,
                ((double) (getLocation().getY() - hook
                        .getBaseY()) + 0.5D) * 128D, 0);
        //Point screenPoint = methods.calc.tileToScreen(getLocation());

        return screenPoint;
    }

    public void interact(String action) {
        Point p = getScreenLocation();
        System.out.println(p);
        if (methods.calc.pointOnGameScreen(p)) {
            methods.camera.setPitch(false);
            if (methods.menu.isOpen()) {
                m.moveMouse(new Point(1, 1));
                System.out.println("menu is open");
            }
            m.moveMouse(p);
            methods.sleep(40);
            String actions[] = methods.menu.getValidMenuActions();
            System.out.println(actions[0]);
            for (int i = 0; i < actions.length; i++) {
                System.out.println(actions[i]);
                actions[i] = actions[i].toLowerCase();
            }
            action = action.toLowerCase();
            if (actions[0] != null && actions[0].contains(action) && actions[0].contains(getName())) {
                m.moveMouse(p);
                try {
                    Thread.sleep(300);
                } catch (Exception e) {

                }
                m.clickMouse(p, true);
                return;
            }
            m.moveMouse(p);
            m.clickMouse(p, false);
            try {
                Thread.sleep(700);
            } catch (Exception e) {
                e.printStackTrace();
            }
            methods.menu.interact(action + " " + getName());
        } else {
            Point mm = methods.calc.tileToMinimap(getLocation());
            if (mm.x != -1 && mm.y != -1)
                m.clickMouse(methods.calc.tileToMinimap(getLocation()), true);
        }
    }
}
