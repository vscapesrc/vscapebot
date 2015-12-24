package com.vsbot.api;

import java.awt.*;

import com.vsbot.hooks.Client;
import com.vsbot.wrappers.RSPlayer;
import com.vsbot.wrappers.RSTile;

public class Calculations {

    private Methods methods;

    public Client getClient() {
        return methods.getHook();
    }

    private static final int[] CURVESIN = new int[2048];
    private static final int[] CURVECOS = new int[2048];

    public static final int SIN_TABLE[];
    public static final int COS_TABLE[];
    public static final Rectangle WHOLE_SCREEN = new Rectangle(1, 1, 765, 503);
    public static final Rectangle GAME_SCREEN = new Rectangle(1, 1, 516, 337);

    static {
        for (int i = 0; i < 2048; i++) {
            CURVESIN[i] = (int) (65536.0 * Math.sin(i * 0.0030679615));
            CURVECOS[i] = (int) (65536.0 * Math.cos(i * 0.0030679615));
        }
    }

    public Calculations(Methods m) {
        this.methods = m;
    }


    /**
     * @param t the tile to be checked
     * @return whether the tile is on minimap
     */

    public boolean tileOnMap(RSTile t) {
        return distanceTo(t) < 18;
    }

    /**
     * @param t the tile to be checked for distance
     * @return the distance to given tile
     */

    public int distanceTo(RSTile t) {
        return (int) distanceBetween(methods.getMyPlayer().getLocation(), t);
    }

    static {
        SIN_TABLE = new int[2048];
        COS_TABLE = new int[2048];
        for (int i = 0; i < 2048; i++) {
            SIN_TABLE[i] = (int) (65536D * Math
                    .sin((double) i * 0.0030679614999999999D));
            COS_TABLE[i] = (int) (65536D * Math
                    .cos((double) i * 0.0030679614999999999D));
        }

    }


    private int getMinimapInt1() {
        return getClient().getMinimapInt1();
    }

    private int getMinimapInt2() {
        return getClient().getMinimapInt2();
    }

    private int getMinimapInt3() {
        return getClient().getMinimapInt3();
    }

    /**
     * @param t the tile to be calculated angle for
     * @return the angle
     */

    public int angleToTile(RSTile t) {
        RSTile me = methods.getMyPlayer().getLocation();
        int angle = (int) Math.toDegrees(Math.atan2(t.getY() - me.getY(),
                t.getX() - me.getX()));
        return angle >= 0 ? angle : 360 + angle;
    }

    /**
     * @param t the tile to be made into minimap coordinates
     * @return the minimap coordinates of the tile given
     */


    public Point tileToMinimap(RSTile t) {
        return worldToMinimap(t.getX(), t.getY());
    }

    /**
     * @param tile to be made into screen coordinates
     * @return
     */


    public Point tileToScreen(final RSTile tile) {
        return tileToScreen(tile, 0);
    }

    private Point tileToScreen(final RSTile tile, final int height) {
        return tileToScreen(tile, 0.5, 0.5, height);
    }


    private Point tileToScreen(final RSTile tile, final double dX,
                               final double dY, final int height) {
        return groundToScreen(
                (int) ((tile.getX() - getClient().getBaseX() + dX) * 512),
                (int) ((tile.getY() - getClient().getBaseY() + dY) * 512),
                height);
    }

    private Point groundToScreen(final int x, final int y, final int height) {
        if ((getClient().getGroundByteArray() == null)
                || (x < 512)
                || (y < 512) || (x > 52224) || (y > 52224)) {
            return new Point(-1, -1);
        }

        int z = tileHeight(x, y) + height;
        return worldToScreen(x, y, 0);
    }

    /**
     * @param x the x location of the tile
     * @param y the y location of the tile
     * @return the tile minimap screen location
     */

    public Point tileToMinimap(int x, int y) {
        return worldToMinimap((x * 4 + 2)
                - methods.getMyPlayer().getX() / 32, (y * 4 + 2)
                - methods.getMyPlayer().getY() / 32);
    }

    /**
     * @param p the point to be checked
     * @return if the point is on screen
     */

    public boolean pointOnScreen(Point p) {
        if (WHOLE_SCREEN.contains(p)) {
            return true;
        }
        return false;
    }

    /**
     * @param p the point to be checked
     * @return if the point is on game screen
     */

    public boolean pointOnGameScreen(Point p) {
        return GAME_SCREEN.contains(p);
    }


    /**
     * used by internal operations
     *
     * @param x world x(not tile)
     * @param y world y(not tile)
     * @return the world coordinates to minimap
     */

    public Point worldToMinimap(int x, int y) {
        int angle = getClient().getMinimapInt1() + getClient().getMinimapInt2() & 0x7ff;
        int m = x * x + y * y;
        if (m > 6400) {
            return new Point(-1, -1);
        } else {
            int n = (SIN_TABLE[angle] * 256) / (getClient().getMinimapInt3() + 256);
            int i1 = (COS_TABLE[angle] * 256) / (getClient().getMinimapInt3() + 256);
            int i2 = y * n + x * i1 >> 16;
            int i3 = y * i1 - x * n >> 16;
            int i4 = 107 + i2 + 3;
            int i5 = 88 - i3 - 5;
            return new Point(i4 + 515, i5);
        }
    }

    /**
     * @param tile to be turned into minimap coords
     * @return the minimap coordinates
     */

    public Point worldToMinimap(RSTile tile) {
        int x = tile.getX();
        int y = tile.getY();
        x -= getClient().getBaseX();
        y -= getClient().getBaseY();
        RSPlayer me = methods.getMyPlayer();
        int calculatedX = (x * 4 + 2) - me.getX() / 32;
        int calculatedY = (y * 4 + 2) - me.getY() / 32;
        int angle = getMinimapInt1() + getMinimapInt2() & 0x7ff;
        int i1 = CURVESIN[angle];
        int j1 = CURVECOS[angle];
        i1 = (i1 * 256) / (getMinimapInt3() + 256);
        j1 = (j1 * 256) / (getMinimapInt3() + 256);
        int k1 = calculatedY * i1 + calculatedX * j1 >> 16;
        int l1 = calculatedY * j1 - calculatedX * i1 >> 16;
        int screenx = 543 + ((94 + k1));
        int screeny = 6 + (83 - l1 - 4);
        return new Point(screenx, screeny);
    }

    /**
     * Calculates the distance between two given tiles
     *
     * @param curr the first tile
     * @param dest the second tile
     * @return the distance between the two tiles
     */

    public static double distanceBetween(RSTile curr, RSTile dest) {
        return Math.sqrt((curr.getX() - dest.getX())
                * (curr.getX() - dest.getX()) + (curr.getY() - dest.getY())
                * (curr.getY() - dest.getY()));
    }

    /**
     * Used by internal operations.
     *
     * @param X      world x
     * @param Y      world y
     * @param height height
     * @return the point
     */

    public Point worldToScreen(double X, double Y, int height) {
        Y += 30;
        X += 11;
        int ScreenX, ScreenY;
        if (X < 128 || Y < 128 || X > 13056 || Y > 13056) {
            ScreenX = -1;
            ScreenY = -1;
        } else {
            int tileCalculation = tileHeight((int) X, (int) Y) - height;
            X -= getClient().getXCameraPos();
            tileCalculation -= getClient().getZCameraPos();
            int curvexsin = CURVESIN[getClient().getXCameraCurve()];
            int curvexcos = CURVECOS[getClient().getXCameraCurve()];
            Y -= getClient().getYCameraPos();
            int curveysin = CURVESIN[getClient().getYCameraCurve()];
            int curveycos = CURVECOS[getClient().getYCameraCurve()];
            int calculation = curvexsin * (int) Y + ((int) X * curvexcos) >> 16;
            Y = -(curvexsin * (int) X) + (int) Y * curvexcos >> 16;
            X = calculation;
            calculation = curveycos * tileCalculation - curveysin * (int) Y >> 16;
            Y = curveysin * tileCalculation + ((int) Y * curveycos) >> 16;
            tileCalculation = calculation;
            if (Y >= 50) {
                ScreenX = ((int) X << 9) / (int) Y + 256;
                ScreenY = (tileCalculation << 9) / (int) Y + 167;
            } else {
                ScreenX = -1;
                ScreenY = -1;
            }
        }
        return new Point(ScreenX, ScreenY);
    }


    /**
     * @param x
     * @param y
     * @return
     */


    public int tileHeight(int x, int y) {
        int[][][] ground = getClient().getGroundIntArray();
        int zidx = getClient().getPlane();
        int x1 = x >> 7;
        int y1 = y >> 7;
        int x2 = x & 0x7f;
        int y2 = 0x7f & y;

        if (x1 < 0 || y1 < 0 || x1 > 103 || y1 > 103) {
            return 0;
        }

        if (zidx < 3
                && (2 & getClient().getGroundByteArray()[1][x1][y1]) == 2) {
            zidx++;
        }

        int i = ground[zidx][1 + x1][y1] * x2 + (128 + -x2)
                * ground[zidx][x1][y1] >> 7;
        int j = ground[zidx][1 + x1][1 + y1] * x2 + ground[zidx][x1][y1 + 1]
                * (128 - x2) >> 7;

        return j * y2 + (128 - y2) * i >> 7;
    }


}
