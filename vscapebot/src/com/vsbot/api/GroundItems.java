package com.vsbot.api;


import java.util.ArrayList;

import com.vsbot.hooks.Client;
import com.vsbot.hooks.GroundItem;
import com.vsbot.hooks.Node;
import com.vsbot.hooks.NodeList;
import com.vsbot.wrappers.RSGroundItem;
import com.vsbot.wrappers.RSTile;


public class GroundItems {

    private Methods methods;

    private Client getHook() {
        return methods.getHook();
    }

    public GroundItems(Methods m) {
        this.methods = m;
    }

    /**
     * @param id the id of ground item wanted
     * @return the nearest ground item with the given id
     */

    public RSGroundItem getNearest(int id) {
        RSGroundItem returnItem = null;
        int maxDist = 104;
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                RSGroundItem items[] = getGroundItemsAt(x, y);
                RSGroundItem agrounditem[];
                int j = (agrounditem = items).length;
                for (int i = 0; i < j; i++) {
                    RSGroundItem item = agrounditem[i];
                    System.out.println(item.getId());
                    if (item.getId() == id
                            && Calculations.distanceBetween(methods
                            .getMyPlayer().getLocation(), item
                            .getLocation()) < (double) maxDist) {
                        maxDist = (int) Calculations.distanceBetween(methods
                                .getMyPlayer().getLocation(), item
                                .getLocation());
                        returnItem = item;
                    }
                }

            }

        }

        return returnItem;
    }

    /**
     * @param id the name of ground item wanted
     * @return the nearest ground item with the given name
     */

    public RSGroundItem getNearest(String name) {
        RSGroundItem returner = null;
        int maxDist = 104;
        for (RSGroundItem it : getAll()) {
            if (it.getName().toLowerCase().equals(name.toLowerCase())
                    && Calculations.distanceBetween(methods.getMyPlayer()
                    .getLocation(), it.getLocation()) < (double) maxDist) {
                maxDist = (int) Calculations.distanceBetween(methods
                        .getMyPlayer().getLocation(), it.getLocation());
                returner = it;
            }

        }
        return returner;
    }

    /**
     * @return all the ground items
     */

    public RSGroundItem[] getAll() {
        ArrayList<RSGroundItem> temp = new ArrayList<RSGroundItem>();
        RSGroundItem returnItem = null;
        int maxDist = 104;
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                RSGroundItem items[] = getGroundItemsAt(x, y);
                RSGroundItem agrounditem[];
                if (items != null) {
                    int j = (agrounditem = items).length;
                    for (RSGroundItem gi : items) {
                        if (gi != null) {
                            temp.add(gi);
                        }
                    }

                }
            }

        }

        return temp.toArray(new RSGroundItem[temp.size()]);
    }

    private RSGroundItem[] getGroundItemsAt(int x, int y) {
        NodeList nl = getHook().getGroundArray()[getHook().getPlane()][x][y];
        if (nl == null) {
            return new RSGroundItem[0];
        }
        ArrayList<RSGroundItem> list = new ArrayList<RSGroundItem>();


        for (Node curNode = nl.getFirstHook(false); curNode != null; curNode = nl.getNextHook(173))
            if (curNode instanceof GroundItem) {
                GroundItem item = (GroundItem) curNode;
                list.add(new RSGroundItem(item.getId(), new RSTile(
                        (x + getHook().getBaseX()),
                        (y + getHook().getBaseY())), methods));
            }

        return (RSGroundItem[]) list.toArray(new RSGroundItem[list
                .size()]);
    }
}