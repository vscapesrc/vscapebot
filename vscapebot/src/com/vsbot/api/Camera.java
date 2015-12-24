package com.vsbot.api;

import com.vsbot.hooks.Client;
import com.vsbot.wrappers.RSEntity;
import com.vsbot.wrappers.RSGroundItem;
import com.vsbot.wrappers.RSObject;
import com.vsbot.wrappers.RSTile;


public class Camera {

    public Methods accessor;

    private Client getClient() {
        return accessor.getHook();
    }

    public Camera(Methods m) {
        accessor = m;
    }

    /**
     * Turns the camera to given entity
     *
     * @param c the entity
     */

    public void turnTo(final RSGroundItem item){
        int angle = getTileAngle(item.getLocation());
        setCameraRotation(angle);
        setPitch(false);
    }

    public void turnTo(final RSEntity c) {
        int angle = getCharacterAngle(c);
        setCameraRotation(angle);
        setPitch(false);
    }

    private int getCharacterAngle(RSEntity n) {
        return getTileAngle(n.getLocation());
    }


    private int getTileAngle(RSTile t) {
        int a = (accessor.calc.angleToTile(t) - 90) % 360;
        return a < 0 ? a + 360 : a;
    }

    /**
     * Turns the camera to the given rsobject
     *
     * @param o the rsobject to be turned to
     */

    public void turnTo(final RSObject o) {
        int angle = getObjectAngle(o);
        setCameraRotation(angle);
    }

    private int getObjectAngle(RSObject o) {
        return getTileAngle(o.getLocation());
    }


    private int getCameraYaw() {
        return getClient().getXCameraCurve();
    }


    private int getCameraPitch() {
        return getClient().getYCameraCurve();
    }

    private int getCameraX() {
        return getClient().getXCameraPos();
    }

    private int getCameraY() {
        return getClient().getYCameraPos();
    }

    private int getCameraZ() {
        return getClient().getZCameraPos();
    }

    private int getCameraAngle() {
        double mapAngle = getCameraYaw();
        mapAngle /= 2040D;
        mapAngle *= 360D;
        return (int) mapAngle;
    }

    private void setCameraRotation(int degrees) {
        char left = '%';
        char right = '\'';
        char whichDir = left;
        int start = getCameraAngle();
        if (start < 180)
            start += 360;
        if (degrees < 180)
            degrees += 360;
        if (degrees > start) {
            if (degrees - 180 < start)
                whichDir = right;
        } else if (start > degrees && start - 180 >= degrees)
            whichDir = right;
        degrees %= 360;
        accessor.keyboard.pressKey(whichDir);
        int timeWaited = 0;
        while (getCameraAngle() > degrees + 5 || getCameraAngle() < degrees - 5) {
            sleep(10);
            if ((timeWaited += 10) > 500) {
                int time = timeWaited - 500;
                if (time == 0)
                    accessor.keyboard.pressKey(whichDir);
                else if (time % 40 == 0)
                    accessor.keyboard.pressKey(whichDir);
            }
        }
        accessor.keyboard.releaseKey(whichDir);
    }

    /**
     * Turns the camera to the given direction(n, e, s, w)
     *
     * @param direction the direction to be turned to
     */

    public void setCompass(char direction) {
        switch (direction) {
            case 110: // 'n'
                setCameraRotation(359);
                break;

            case 101: // 'e'
                setCameraRotation(89);
                break;

            case 115: // 's'
                setCameraRotation(179);
                break;

            case 119: // 'w'
                setCameraRotation(269);
                break;

            default:
                setCameraRotation(359);
                break;
        }
    }

    public boolean setPitch(boolean up) {
        try {
            char key = up ? '&' : '(';
            accessor.keyboard.pressKey(key);
            sleep(nextInt(1000, 1500));
            accessor.keyboard.releaseKey(key);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean setCameraAltitude(double altPercent) {
        int alt = (int) ((altPercent / 100D) * -1237D - 1226D);
        int curAlt = getCameraZ();
        int lastAlt = 0;
        if (curAlt == alt)
            return true;
        if (curAlt > alt) {
            accessor.keyboard.pressKey('&');
            for (long start = System.currentTimeMillis(); curAlt > alt && System.currentTimeMillis() - start < 30L; curAlt = getCameraZ()) {
                if (lastAlt != curAlt)
                    start = System.currentTimeMillis();
                lastAlt = curAlt;
                sleep(1);
            }

            accessor.keyboard.releaseKey('&');
            return true;
        }
        accessor.keyboard.pressKey('(');
        for (long start = System.currentTimeMillis(); curAlt < alt && System.currentTimeMillis() - start < 30L; curAlt = getCameraZ()) {
            if (lastAlt != curAlt)
                start = System.currentTimeMillis();
            lastAlt = curAlt;
            sleep(1);
        }

        accessor.keyboard.releaseKey('(');
        return true;
    }

    private static int nextInt(int min, int max) {
        return (int) (Math.random() * (double) (max - min)) + min;
    }

    private static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
