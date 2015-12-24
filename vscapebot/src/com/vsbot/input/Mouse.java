// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Mouse.java

package com.vsbot.input;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Random;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;

public class Mouse {
    private class DoublePoint {

        public double x;
        public double y;

        public DoublePoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public class Line {

        public final int x1;
        public final int y1;
        public final int x2;
        public final int y2;

        public Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    private Methods methods;

    public Client getHook() {
        return methods.getHook();
    }

    public Mouse(Methods m) {
        this.methods = m;
    }

    public boolean isPressed() {
        return methods.getLoader().pressed;
    }

    private void splineMouse() {
        Line line1 = (Line) SPLINE_LINES.get(0);
        int lastX = line1.x1;
        int lastY = line1.y1;
        if (line1.x1 < 0 || line1.y1 < 0 || line1.x1 > 766 || line1.y1 > 504) {
            lastX = -1;
            lastY = -1;
        }
        for (int i = 0; i < SPLINE_LINES.size(); i++) {
            Line line = (Line) SPLINE_LINES.get(i);
            int x = line.x2;
            int y = line.y2;
            if (lastX != x || lastY != y && x > -1 && y > -1 && x < 766 && y < 504) {
                moveMouseInternal(lastX, lastY, x, y);
                lastX = x;
                lastY = y;
            } else if (lastX != x || lastY != y)
                try {
                    int secs = 0;
                    double nanos = 0.050000000000000003D - (double) secs;
                    int nanosReal = (int) (nanos * 1000D);
                    Thread.sleep(1, 10); //secs, nanosReal
                } catch (InterruptedException interruptedexception) {
                }
        }

    }

    private double createSmartControlPoint(int n, double spacing, boolean yValue, int distance) {
        int length = (int) spacing;
        double d;
        if (yValue)
            d = nextDouble(0.0D, length - n) * random.nextDouble() * (double) nextInt(0, 2);
        else
            d = nextDouble(0.0D, length - n) * random.nextDouble() * (double) nextInt(distance / 100, distance / 100 + nextInt(2, 4));
        return d;
    }

    private void createSpline(Point start, Point end) {
        int distance = (int) Point2D.distance(start.x, start.y, end.x, end.y);
        n = distance / 100 + random.nextInt(3) + 4;
        if (distance < 100)
            n = 3;
        points = new DoublePoint[n];
        points[0] = new DoublePoint(start.x, start.y);
        points[n - 1] = new DoublePoint(end.x, end.y);
        int midPoints = n - 2;
        DoublePoint lastPos = new DoublePoint(points[0].x, points[0].y);
        for (int i = 1; i < n - 1; i++) {
            double X = lastPos.x;
            double Y = lastPos.y;
            double spacing = distance / (midPoints + 2);
            int randomNum = random.nextInt(2);
            if (randomNum == 0)
                X += createSmartControlPoint(i, spacing, false, distance);
            else
                X -= createSmartControlPoint(i, spacing, false, distance);
            randomNum = random.nextInt(2);
            if (randomNum == 0)
                Y += createSmartControlPoint(i, spacing, true, distance);
            else
                Y -= createSmartControlPoint(i, spacing, true, distance);
            points[i] = new DoublePoint(X, Y);
            lastPos.x = X;
            lastPos.y = Y;
        }

        generateSpline();
    }

    private void generateSpline() {
        OUTLINE_LINES.clear();
        OUTLINE_POINTS.clear();
        SPLINE_LINES.clear();
        double step = 0.00130718954248366D;
        double t = 0.00130718954248366D;
        DoublePoint points2[] = new DoublePoint[n];
        int Xold = (int) points[0].x;
        int Yold = (int) points[0].y;
        for (int i = 0; i < n; i++) {
            int X = (int) points[i].x;
            int Y = (int) points[i].y;
            OUTLINE_POINTS.add(new Point(X, Y));
        }

        if (n > 2) {
            int Xo = Xold;
            int Yo = Yold;
            for (int i = 1; i < n; i++) {
                int X = (int) points[i].x;
                int Y = (int) points[i].y;
                OUTLINE_LINES.add(new Line(Xo, Yo, X, Y));
                Xo = X;
                Yo = Y;
            }

        }
        for (int k = 1; k < 765; k++) {
            System.arraycopy(points, 0, points2, 0, n);
            for (int j = n - 1; j > 0; j--) {
                for (int i = 0; i < j; i++) {
                    points2[i].x = (1.0D - t) * points2[i].x + t * points2[i + 1].x;
                    points2[i].y = (1.0D - t) * points2[i].y + t * points2[i + 1].y;
                }

            }

            int X = (int) points2[0].x;
            int Y = (int) points2[0].y;
            SPLINE_LINES.add(new Line(Xold, Yold, X, Y));
            Xold = X;
            Yold = Y;
            t += 0.00130718954248366D;
        }

    }

    public void setMousePos(int x, int y) {
        if (x < 0)
            x = -1;
        if (y < 0)
            y = -1;
        if (x > 765)
            x = -1;
        if (y > 504)
            y = -1;
        try {
            methods.getLoader().processEvent(new MouseEvent(FAKE_SOURCE, 503, System.currentTimeMillis(), 0, x, y, 0, false, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveMouseInternal(int curX, int curY, int x, int y) {
        for (double distance = Point2D.distance(curX, curY, x, y); distance > 0.0D; distance = Point2D.distance(curX, curY, x, y)) {
            if (curX < 0 || curY < 0) {
                curX = x;
                curY = y;
            }
            if (Math.round(curX) < Math.round(x))
                curX++;
            else if (Math.round(curX) > Math.round(x))
                curX--;
            if (Math.round(curY) < Math.round(y))
                curY++;
            else if (Math.round(curY) > Math.round(y))
                curY--;
            setMousePos(curX, curY);
            try {
                int secs = 0;
                double nanos = 0.050000000000000003D - (double) secs;
                int nanosReal = (int) (nanos * 1000D);
                Thread.sleep(1, 10); //secs, nanosReal
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void moveMouse(int x, int y, int randomX, int randomY) {
        int thisX = getLocation().x;
        int thisY = getLocation().y;
        if (thisX < 0 || thisY < 0)
            switch (nextInt(1, 5)) {
                case 1: // '\001'
                    thisX = 1;
                    thisY = nextInt(0, 500);
                    setMousePos(thisX, thisY);
                    break;

                case 2: // '\002'
                    thisX = nextInt(0, 765);
                    thisY = 501;
                    setMousePos(thisX, thisY);
                    break;

                case 3: // '\003'
                    thisX = 766;
                    thisY = nextInt(0, 500);
                    setMousePos(thisX, thisY);
                    break;

                case 4: // '\004'
                    thisX = nextInt(0, 765);
                    thisY = 1;
                    setMousePos(thisX, thisY);
                    break;
            }
        if (thisX == x && thisY == y)
            return;
        if (Point2D.distanceSq(thisX, thisY, x, y) < 10D) {
            SPLINE_LINES.clear();
            SPLINE_LINES.add(new Line(thisX, thisY, nextInt(x, x + randomX), nextInt(y, y + randomY)));
        } else {
            createSpline(new Point(thisX, thisY), new Point(nextInt(x, x + randomX), nextInt(y, y + randomY)));
        }
        splineMouse();
    }

    public void moveMouse(int x, int y) {
        moveMouse(x, y, 0, 0);
    }

    public void moveMouse(Point pos) {
        moveMouse(pos.x, pos.y, 0, 0);
    }

    public void pressMouse(int x, int y, boolean button) {
        if (x < 0 || y < 0 || x > 756 || y > 503)
            return;
        try {
            methods.getLoader().processEvent(new MouseEvent(FAKE_SOURCE, 501, System.currentTimeMillis(), 0, x, y, 1, false, button ? 1 : 3));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseMouse(int x, int y, boolean button) {
        if (x < 0 || y < 0 || x > 756 || y > 503)
            return;
        try {
            methods.getLoader().processEvent(new MouseEvent(FAKE_SOURCE, 502, System.currentTimeMillis(), 0, x, y, 1, false, button ? 1 : 3));
            methods.getLoader().processEvent(new MouseEvent(FAKE_SOURCE, 500, System.currentTimeMillis(), 0, x, y, 1, false, button ? 1 : 3));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clickMouse(Point p, int randomX, int randomY, boolean button) {
        moveMouse(p, randomX, randomY);
        sleep(nextInt(50, 150));
        clickMouse(button);
    }

    public void clickMouse(Point p, boolean button) {
        clickMouse(p, 0, 0, button);
    }

    public void moveMouse(Point p, int randomX, int randomY) {
        moveMouse(p.x, p.y, randomX, randomY);
    }

    public void clickMouse(int x, int y, boolean leftClick) {
        pressMouse(x, y, leftClick);
        sleep(nextInt(0, 70));
        releaseMouse(x, y, leftClick);
    }

    public void clickMouse(boolean button) {
        clickMouse(getLocation().x, getLocation().y, button);
    }

    public void clickMouse2(Point p, boolean button) {
        clickMouse(p.x, p.y, button);
    }

    public void dragMouse(Point destination, int randomX, int randomY) {
        int thisX = getLocation().x;
        int thisY = getLocation().y;
        pressMouse(thisX, thisY, true);
        sleep(10, 50);
        moveMouse(destination, randomX, randomY);
        thisX = getLocation().x;
        thisY = getLocation().y;
        sleep(10, 50);
        clickMouse(thisX, thisY, true);
    }

    public void dragMouse(Point destination) {
        dragMouse(destination, 0, 0);
    }

    public boolean moveMouseRandomly(int maxDistance) {
        if (maxDistance == 0)
            return false;
        maxDistance = nextInt(1, maxDistance);
        Point p = new Point(getRandomMouseX(maxDistance), getRandomMouseY(maxDistance));
        if (p.x < 1 || p.y < 1)
            p.x = p.y = 1;
        moveMouse(p.x, p.y);
        if (nextInt(0, 2) == 0)
            return false;
        else
            return moveMouseRandomly(maxDistance / 2);
    }

    public int getRandomMouseX(int maxDistance) {
        Point p = getLocation();
        if (nextInt(0, 2) == 0)
            return p.x - nextInt(0, p.x >= maxDistance ? maxDistance : p.x);
        else
            return p.x + nextInt(1, 762 - p.x >= maxDistance ? maxDistance : 762 - p.x);
    }

    public int getRandomMouseY(int maxDistance) {
        Point p = getLocation();
        if (nextInt(0, 2) == 0)
            return p.y - nextInt(0, p.y >= maxDistance ? maxDistance : p.y);
        else
            return p.y + nextInt(1, 500 - p.y >= maxDistance ? maxDistance : 500 - p.y);
    }

    public void moveOffScreen() {
        switch (nextInt(0, 4)) {
            case 0: // '\0'
                moveMouse(nextInt(-10, 775), nextInt(-100, -10));
                break;

            case 1: // '\001'
                moveMouse(nextInt(-10, 775), 503 + nextInt(10, 100));
                break;

            case 2: // '\002'
                moveMouse(nextInt(-100, -10), nextInt(-10, 513));
                break;

            case 3: // '\003'
                moveMouse(nextInt(10, 100) + 765, nextInt(-10, 513));
                break;
        }
    }

    public boolean isPresent() {
        Point pos = getLocation();
        return pos.x != -1 && pos.y != -1;
    }

    public Point getLocation() {
        try {
            Point appletPos = methods.getLoader().mousePos;
            return appletPos != null ? appletPos : new Point(-1, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Point(-1, -1);
    }


    public double nextDouble(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public int nextInt(int min, int max) {
        return (int) (Math.random() * (double) (max - min)) + min;
    }

    public void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sleep(int min, int max) {
        try {
            Thread.sleep(nextInt(min, max));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int n = 4;
    private DoublePoint points[];
    private final Random random = new Random();
    private final java.util.List SPLINE_LINES = new LinkedList();
    private final java.util.List OUTLINE_LINES = new LinkedList();
    private final java.util.List OUTLINE_POINTS = new LinkedList();
    private final double FIXED_MOUSE_SPEED = 0.050000000000000003D;
    public final static Container FAKE_SOURCE = new Container();
    public boolean input = true;

}
