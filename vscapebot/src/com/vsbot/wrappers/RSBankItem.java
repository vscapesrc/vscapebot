package com.vsbot.wrappers;

import java.awt.*;
import java.util.Random;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;
import com.vsbot.hooks.ItemDef;

public class RSBankItem {
    int id;
    int slot;
    private ItemDef itemDef;
    public RSInterfaceChild child;
    Random r = new Random();

    public Methods methods;

    public Client getHook() {
        return methods.getHook();
    }

    public RSBankItem(int id, int slot, RSInterfaceChild component, Methods m) {
        methods = m;
        id--;
        this.id = id;
        this.slot = slot;
        if (id > -1) {
            this.itemDef = getHook().getForId(id);
        }
        this.child = component;
    }

    private int getScreenX() {
        return ((child.getScreenX() + child.getAccessor().getXOffset()) + slot + 1 * (32 + child
                .getAccessor().getInvSpritePadX()));
    }

    private int getScreenY() {
        System.out.println(child.getAccessor().getInvSpritePadX());
        System.out.println(child.getAccessor().getInvSpritePadY());
        return ((child.getScreenY() + child.getAccessor().getYOffset()) + slot + 1 * (32 + child
                .getAccessor().getInvSpritePadY()));
    }

    public Point getScreenLocation() {
        return new Point(getScreenX() + r.nextInt(8), getScreenY());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        if (itemDef != null && itemDef.getName() != null) {
            return itemDef.getName();
        }
        return "null";
    }

    public int getSlot() {
        return slot;
    }

    public void interact(String action) {
        if (methods.menu.isOpen()) {
            methods.mouse.moveMouse(new Point(10, 10));
        }

        try {
            Point p = getPoint();
            methods.mouse.moveMouse(p);
            Thread.sleep(100);

            methods.mouse.clickMouse(p, false);

            Thread.sleep(1000);
        } catch (Exception e) {

        }

        methods.menu.interact(action, false);
    }

    public Point getPoint() {
        int theSlot = slot;
        Random r = new Random();
        int col = theSlot-- % 8;
        int row = theSlot / 8;
        int x = 73 + col * 49;
        int y = 63 + (row * 39);

        return new Point(x + 10, y);
    }

}
