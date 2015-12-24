package com.vsbot.wrappers;

import java.awt.*;
import java.util.Random;

import com.vsbot.api.Methods;
import com.vsbot.api.Tabs;
import com.vsbot.hooks.Client;
import com.vsbot.hooks.ItemDef;

public class RSItem {

    private int id;
    private ItemDef itemDef;
    private int slot;
    private Client hook;
    private Methods methods;
    Tabs tabs;

    public RSItem(int theId, int theSlot, Methods m) {
        this.methods = m;
        this.hook = methods.getHook();
        this.tabs = methods.tabs;
        this.slot = theSlot + 1;
        this.id = theId;
        if (theId > -1) {
            this.itemDef = hook.getForId(id);
        }
    }


    public int getId() {
        return id;
    }

    public Point getScreenLocation() {
        Random r = new Random();
        int col = --slot % 4;
        int row = slot / 4;
        int x = 580 + col * 42 - 10;// + r.nextInt(6);
        int y = 219 + row * 35 + 10;// +// r.nextInt(6);
        return new Point(x, y);
    }

    public int getSlot() {
        return slot - 1;
    }

    public void interact(String action) {
        if (tabs.getTab() != tabs.INVENTORY) {
            tabs.switchTab(tabs.INVENTORY);
            try {
                Thread.sleep(10);
            } catch (Exception e) {

            }
        }
        if (tabs.getTab() != tabs.INVENTORY) {
            interact(action);
        }
        methods.inventory.interactSlot(getSlot(), action + " " + getName());
    }


    public void interactBank(String action) {
        methods.inventory.interactSlotBank(getSlot(), action);
    }

    /*public boolean isStackable() {
         return itemDef.getStackable();
     }

     public int getValue() {
         return itemDef.getValue();
     }

     public int getTeam() {
         return itemDef.getTeam();
     }

     public String[] getGroundActions() {
         return itemDef.getGroundActions();
     }*/

    public String getName() {
        if (itemDef != null && itemDef.getName() != null) {
            return itemDef.getName();
        }
        return "null";
    }

}
