package com.vsbot.api;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.vsbot.hooks.Client;
import com.vsbot.input.Mouse;
import com.vsbot.wrappers.RSItem;

public class Inventory {


    Methods methods;

    public Client getHook() {
        return methods.getHook();
    }

    Mouse mouse;

    public String getSelectedItem() {
        return getHook().getSelectedItemName();
    }

    public Inventory(Methods m) {
        this.methods = m;
        this.mouse = methods.mouse;
    }


    public void openTab(int tab) {
        switch (tab) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
        }
    }


    /**
     * @param name of the item
     * @return whether inventory contains the item with the given name
     */

    public boolean containsItem(String name) {
        return getCount(name) > 0;
    }


    private int[] getInventoryCache() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (com.vsbot.hooks.GameInterface[] inface : methods.getHook().getInterfaceCache()) {
            if (inface != null) {
                for (com.vsbot.hooks.GameInterface iface2 : inface) {
                    if (iface2 != null) {

                        if (iface2.getId() == 4521985
                                && !methods.banking.isOpen()) {
                            if (iface2.getInv() != null) {
                                for (int a = 0; a < iface2.getInv().length; a++) {
                                    list.add(iface2.getInv()[a]);
                                }
                            }
                        } else if (methods.banking.isOpen()
                                && iface2.getId() == 7471105) {
                            if (iface2.getInv() != null) {
                                for (int a = 0; a < iface2.getInv().length; a++) {
                                    list.add(iface2.getInv()[a]);
                                }
                            }
                        }
                    }
                }
            }
        }
        return convertIntegers(list);
    }

    private int[] convertIntegers(List<Integer> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }


    /**
     * @param name of the item wanted
     * @return instance of rsitem with the given name
     */
    public RSItem getItem(String name) {
        for (RSItem one : getItems()) {
            if (one != null && one.getId() != -1 && one.getName() != null) {
                if (one.getName().equals(name)) {
                    return one;
                }
            }
        }
        return null;
    }

    /**
     * @param id of the item wanted
     * @return instance of rsitem with the given id
     */

    public RSItem getItem(int id) {
        for (RSItem one : getItems()) {
            if (one != null && one.getId() != -1 && one.getId() == id) {
                return one;
            }
        }
        return null;
    }


    public void interactSlotBank(int slot, String action) {
        if (methods.menu.isOpen()) {
            mouse.moveMouse(new Point(10, 10));
            methods.sleep(1000);
        }

        Point p = getInventoryItemPoint(slot);
        mouse.moveMouse(p);
        methods.sleep(1000);
        mouse.clickMouse(p, false);
        methods.sleep(1000);
        methods.menu.interact(action, false);
    }

    public void interactSlot(int slot, String action) {
        if (methods.menu.isOpen()) {
            mouse.moveMouse(new Point(10, 10));
            methods.sleep(1000);
        }

        Point p = getInventoryItemPoint(slot);
        mouse.moveMouse(p);
        methods.sleep(150);
        String actions[] = methods.menu.getValidMenuActions();
        for (int i = 0; i < actions.length; i++) {
            actions[i] = actions[i].toLowerCase();
        }
        action = action.toLowerCase();
        if (actions[0] != null && actions[0].contains(action)) {
            mouse.clickMouse(p, true);
            return;
        }
        mouse.clickMouse(p, false);
        methods.sleep(1000);
        methods.menu.interact(action, true);
    }


    /**
     * does the given action to the item with given id
     *
     * @param id     of the item
     * @param action the action we want to do
     */

    public void interact(int id, String action) {
        if (getFirstSlotWith(id) != -1) {
            interactSlot(getFirstSlotWith(id), action);
        }
    }

    /**
     * Drops all except the given id
     *
     * @param except the ids not to be dropped
     */

    public void dropAllBut(int except) {
        for (int i = 1; i < 27; i++) {
            int slotItem = getSlotItem(i);
            if (slotItem != except && slotItem != -1) {
                interactSlot(i, "Drop");
            }
        }

    }

    /**
     * Drops everything in inventory
     */

    public void dropAll() {
        for (int i = 1; i < 27; i++) {
            int slotItem = -1;
            slotItem = getSlotItem(i);
            if (slotItem != -1) {
                interactSlot(i, "Drop");
                methods.sleep(nextInt(500, 850));
            }

        }
    }

    public int getSlotItem(int slot) {
        int invCache[] = getInventoryCache();
        return invCache[slot];
    }

    public int getFirstSlotWith(int id) {
        for (int i = 0; i < 28; i++) {
            if (getSlotItem(i) == id) {
                return i + 1;
            }
        }
        return -1;
    }

    public void clickItem(int idArray[]) {
        for (int i = 1; i < 29; i++) {
            int slotID = getSlotItem(i);
            int ai[];
            int k = (ai = idArray).length;
            for (int j = 0; j < k; j++) {
                int id = ai[j];
                if (id == slotID) {
                    clickSlot(i);
                    return;
                }
            }

        }

    }

    public void clickItem(int id) {
        mouse.clickMouse(getInventoryItemPoint(getFirstSlotWith(id)), false);
    }

    public void clickSlot(int slot) {
        mouse.clickMouse(getInventoryItemPoint(slot), false);
    }


    public RSItem[] getItems() {
        final List<RSItem> out = new ArrayList<RSItem>();
        for (int i = 0; i < getInventoryCache().length; i++) {
            RSItem in = new RSItem(getInventoryCache()[i] - 1, i + 1, methods);
            out.add(in);
        }
        return out.toArray(new RSItem[out.size()]);
    }

    public static Point getInventoryItemPoint(int slot) {
        Random r = new Random();
        int col = --slot % 4;
        int row = slot / 4;
        int x = 580 + col * 42 - 10 + r.nextInt(6);
        int y = 219 + row * 35 + r.nextInt(6);
        return new Point(x, y);
    }

    public int getCount(int id) {
        int i = 0;
        int invCache[] = getInventoryCache();
        int ai[];
        int k = (ai = invCache).length;
        for (int j = 0; j < k; j++) {
            int x = ai[j];
            if (x != 0 && x - 1 == id)
                i++;
        }

        return i;
    }

    public int getCount(String name) {
        int i = 0;
        for (RSItem item : getItems()) {
            if (item.getName().equalsIgnoreCase(name)) {
                i++;
            }
        }

        return i;
    }

    public int getCount() {
        int i = 0;
        int invCache[] = getInventoryCache();
        int ai[];
        int k = (ai = invCache).length;
        for (int j = 0; j < k; j++) {
            int x = ai[j];
            if (x != 0)
                i++;
        }

        return i;
    }

    public boolean isFull() {
        return getCount() == 28;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public void sleep(int min, int max) {
        methods.sleep(nextInt(min, max));
    }

    public int nextInt(int min, int max) {
        return (int) (Math.random() * (double) (max - min)) + min;
    }
}
