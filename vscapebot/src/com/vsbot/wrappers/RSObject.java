package com.vsbot.wrappers;

import java.awt.*;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;
import com.vsbot.hooks.GameObject;
import com.vsbot.hooks.ObjectDef;
import com.vsbot.input.Mouse;


public class RSObject {
    private int id;
    private RSTile location;
    private com.vsbot.hooks.GameObject accessor;
    Mouse m;
    private ObjectDef objDef = null;
    private Methods methods;
    private Client hook;


    public RSObject(int theId, RSTile loc, GameObject theAccessor, Methods m) {
        this.methods = m;
        this.hook = methods.getHook();
        this.m = methods.mouse;
        this.id = theId;
        this.location = loc;
        this.accessor = theAccessor;
        this.objDef = hook.getForIdObject(id);
    }

    public int getId() {
        return accessor.getId();
    }

    public RSTile getLocation() {
        return location;
    }

    public String getName() {
        return objDef.getName();
    }

    public Point getScreenLocation() {
        Point p = methods.calc.worldToScreen(accessor.getWorldX(), accessor.getWorldY(), accessor.getHeight() * 2);
        if (getName().contains("swing")) {
            p = methods.calc.worldToScreen(accessor.getWorldX(), accessor.getWorldY(), accessor.getHeight() / 2);
            p.y -= 20;
        }
        return new Point(p);
        /*Point screenPoint = methods.calc.worldToScreen(((double) (getLocation()
                  .getX() - BSLoader.getClient().getBaseX()) + 0.5D) * 128D,
                  ((double) (getLocation().getY() - BSLoader.getClient()
                          .getBaseY()) + 0.5D) * 128D, 0);
          return screenPoint;*/
        ///return methods.calc.tileToScreen(getLocation());
        ////return methods.calc.worldToScreen(accessor.getWorldX(), accessor.getWorldY(), 0);
    }

    public boolean isOnScreen() {
        return methods.calc.pointOnGameScreen(getScreenLocation());
    }


    public void interact(String action) {
        Point p = getScreenLocation();
        ////    System.out.println(accessor.getWorldZ());
        if (methods.calc.pointOnGameScreen(p)) {
            if (methods.menu.isOpen()) {
                m.moveMouse(100, 100);
                ////////	System.out.println("menu is open");
            }
            /// Mouse.moveMouseRandomly(450);
            m.moveMouse(p);
            methods.sleep(600);
            String actions[] = methods.menu.getValidMenuActions();
            /////System.out.println(actions[0]);
            for (int i = 0; i < actions.length; i++) {
                actions[i] = actions[i].toLowerCase();
            }
            action = action.toLowerCase();
            if (actions[0] != null && actions[0].contains(action)) {
                //////	System.out.println(actions[0]);
                m.clickMouse(p, true);
                return;
            }
            m.clickMouse(p, false);
            try {
                Thread.sleep(300);
            } catch (Exception e) {

            }
            ////7BSLoader.getMethods().sleep(700);
            methods.menu.interact(action);
        } else {
            Point mm = methods.calc.tileToMinimap(getLocation());
            if (mm.x != -1 && mm.y != -1)
                m.clickMouse(methods.calc.tileToMinimap(getLocation()), true);
        }
    }


}
