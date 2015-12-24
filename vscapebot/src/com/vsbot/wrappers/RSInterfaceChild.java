package com.vsbot.wrappers;

import java.awt.*;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;
import com.vsbot.hooks.GameInterface;
import com.vsbot.input.Mouse;

public class RSInterfaceChild {

    private GameInterface[] parent;

    private Mouse m;

    private GameInterface accessor;

    private int parentId;
    private Methods methods;
    private Client hook;

    public RSInterfaceChild(final GameInterface[] theParent, GameInterface access, int parentId, Methods m) {
        this.methods = m;
        this.hook = methods.getHook();
        this.m = methods.mouse;
        parent = theParent;
        accessor = access;
        this.parentId = parentId;
    }

    public int getScreenX() {
        return accessor.getXPos() + getAccessor().getXOffset();
    }

    public int getScreenY() {
        return accessor.getY() + getAccessor().getYOffset();
    }

    public GameInterface getAccessor() {
        return accessor;
    }


    public RSInterfaceChild getParent() {

        return new RSInterfaceChild(parent, parent[accessor.getParentId()], parentId, methods);
    }

    public GameInterface getHook() {
        return accessor;
    }

    public int getId() {
        if (accessor != null) {
            return accessor.getId();
        }
        return -1;
    }

    public int getAbsoluteX() {
        // Get internal Interface
        GameInterface inter = accessor;
        if (inter == null) {
            return -1;
        }

        // Define x
        int x = 0;

        return x + 1;
    }


    public Rectangle getArea() {
        int x = 0;
        int y = 0;
        x += getAccessor().getMasterX();
        y += getAccessor().getMasterY();
        x += getParent().getScreenX();
        y += getParent().getScreenY();
        x += getParent().getAccessor().getMasterX();
        y += getParent().getAccessor().getMasterY();
        for (GameInterface a : accessor.getChildren()) {
            x += a.getXPos() + a.getYOffset() + a.getMasterY();
            y += a.getY() + a.getYOffset() + a.getMasterX();
        }
        x += getScreenX();
        y += getScreenY();
        return new Rectangle(x, y, accessor.getWidth(), accessor.getHeight());
    }

    public String getText() {
        if (accessor.getText() != null) {
            return accessor.getText();
        }
        return "";
    }

    public int getType() {
        return accessor.getType();
    }

    public String[] getActions() {
        return accessor.getActions();
    }

    public boolean doClick(boolean leftClick) {

        Rectangle rect = getArea();
        if (rect.x == -1 || rect.y == -1 || rect.width == -1 || rect.height == -1) {
            return false;
        }

        int x = accessor.getX() + 5;
        System.out.println("x:" + x);
        Point p = new Point(x, accessor.getY()); ///getArea().x+getRandomPoint().x, getArea().y+getRandomPoint().y);
        System.out.println("x: " + x + " y: " + accessor.getY() + " id:" + accessor.getId());
        System.out.println(p.toString());
        m.moveMouse(p);
        try {
            Thread.sleep(400);
        } catch (Exception e) {

        }
        m.clickMouse(p, true);
        ///
        return true;
    }

    public Point getRandomPoint() {
        int randomX = nextInt(0, getArea().width);
        int randomY = nextInt(0, getArea().height);

        return new Point(randomX, randomY);

    }


    double getRandomInteger(int min, int max) {
        return min + Math.floor(Math.random() * (max + 1 - min));
    }

    public static int nextInt(int min, int max) {
        return (int) (Math.random() * (double) (max - min)) + min;
    }


}
