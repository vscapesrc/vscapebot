package com.vsbot.api;

import java.util.ArrayList;

import com.vsbot.hooks.Client;
import com.vsbot.hooks.GameObject;
import com.vsbot.hooks.Ground;
import com.vsbot.wrappers.RSObject;
import com.vsbot.wrappers.RSTile;


public class Objects {

    Methods methods = null;

    public Client getHook() {
        return methods.getHook();
    }

    public Objects(Methods m) {
        this.methods = m;
    }

    /**
     * Gets nearest object by id
     *
     * @param id Id of the object
     * @return The object
     */

    public RSObject getNearest(int id) {
        RSObject returnGameObject = null;
        int maxDist = 50;
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                RSObject items[] = getAtLocal(x, y);
                RSObject agrounditem[];
                int j = (agrounditem = items).length;
                for (int i = 0; i < j; i++) {
                    RSObject item = agrounditem[i];
                    if (item.getId() == id
                            && Calculations.distanceBetween(methods
                            .getMyPlayer().getLocation(), item
                            .getLocation()) < (double) maxDist) {
                        maxDist = (int) Calculations.distanceBetween(methods
                                .getMyPlayer().getLocation(), item
                                .getLocation());
                        returnGameObject = item;
                    }
                }

            }

        }

        return returnGameObject;
    }

    /**
     * Gets nearest object by name
     *
     * @param name Name of the object
     * @return The object
     */

    public RSObject getNearest(String name) {
        RSObject returnGameObject = null;
        try {

            int maxDist = 50;
            for (int x = 0; x < 104; x++) {
                for (int y = 0; y < 104; y++) {
                    RSObject items[] = getAtLocal(x, y);
                    RSObject agrounditem[];
                    int j = (agrounditem = items).length;
                    for (int i = 0; i < j; i++) {
                        RSObject item = agrounditem[i];
                        if (item.getName().equals(name)
                                && Calculations.distanceBetween(methods
                                .getMyPlayer().getLocation(), item
                                .getLocation()) < (double) maxDist) {
                            maxDist = (int) Calculations.distanceBetween(methods
                                    .getMyPlayer().getLocation(), item
                                    .getLocation());
                            returnGameObject = item;
                        }
                    }

                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnGameObject;
    }

    public RSObject getNearestByName(String name) {
        int curX = methods.getMyPlayer().getLocation().getX();
        int curY = methods.getMyPlayer().getLocation().getY();
        double dist = -1;
        RSObject cur = null;
        for (RSObject a : getAll()) {
            if (a != null && a.getName() != null) {
                if (a.getName().equalsIgnoreCase(name)) {
                    int x, y;

                    x = a.getLocation().getX();
                    y = a.getLocation().getY();
                    double distance = Math.sqrt((curX - x) * (curX - x)
                            + (curY - y) * (curY - y));
                    if (distance < dist || dist == -1) {
                        dist = distance;
                        cur = a;
                    }
                }
            }
        }

        return cur;
    }

    public RSObject[] getAll() {
        ArrayList<RSObject> temp = new ArrayList<RSObject>();
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                RSObject items[] = getAtLocal(x, y);
                for (RSObject gi : items) {
                    if (gi != null) {

                        temp.add(gi);
                    }
                }

            }

        }
        return temp.toArray(new RSObject[temp.size()]);
    }

    private RSObject[] getAtLocal(int x, int y) {
        ArrayList<RSObject> objects = new ArrayList<RSObject>();
        if (getHook() == null || getHook().getWorldController() == null || getHook().getWorldController().getGroundArray() == null) {
            return new RSObject[0];
        }

        Ground rsGround = getHook().getWorldController()
                .getGroundArray()[0][x][y];

        if (rsGround != null) {
            RSObject rsObj;

            x += getHook().getBaseX();
            y += getHook().getBaseY();

            for (GameObject obj : rsGround.getGround()) {
                if (obj != null && obj instanceof GameObject) {
                    if (obj.getId() != -1) {
                        objects.add(new RSObject(obj.getId(), new RSTile(x, y),
                                obj, methods));
                    }
                }
            }
        }
        return (RSObject[]) objects.toArray(new RSObject[objects.size()]);
    }
}