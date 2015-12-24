package com.vsbot.api;

import scripts.Script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vsbot.hooks.Client;
import com.vsbot.hooks.Npc;
import com.vsbot.hooks.Player;
import com.vsbot.input.Keyboard;
import com.vsbot.input.Mouse;
import com.vsbot.launcher.Loader;
import com.vsbot.wrappers.RSInterface;
import com.vsbot.wrappers.RSNPC;
import com.vsbot.wrappers.RSPlayer;
import com.vsbot.wrappers.RSTile;

public class Methods {


    Client hook;

    public Client getHook() {
        return hook;
    }

    public Script runningScript;


    private Loader loader;

    public void setLoader(Loader l) {
        this.hook = l.getHook();
        this.loader = l;
    }


    public Loader getLoader() {
        return loader;
    }

    public Mouse mouse = new Mouse(this);
    public Calculations calc = new Calculations(this);
    public Objects objects = new Objects(this);
    public Inventory inventory = new Inventory(this);
    public Interfaces interfaces = new Interfaces(this);
    public Bank banking = new Bank(this);
    public Tabs tabs = new Tabs(this);
    public Menu menu = new Menu(this);
    public Keyboard keyboard = new Keyboard(this);
    public Camera camera = new Camera(this);
    private RSInterface[] mainCache = new RSInterface[0];
    public Skills skills = new Skills(this);
    public GroundItems grounditems = new GroundItems(this);
    private Map<Integer, RSInterface> sparseMap = new HashMap<Integer, RSInterface>();


    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
        }
    }

    public TilePath createTilePath(RSTile... tiles) {
        return new TilePath(this, tiles);
    }

    public int getMyPlayerIndex() {
        Player[] array = getHook().getPlayers();
        for (int i = 0; i < array.length; i++) {
            RSPlayer pl = new RSPlayer(array[i], this);
            if (pl.getName() != null
                    && pl.getName().equals(getMyPlayer().getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return Whether the player is loggeed in
     */

    public boolean isLoggedIn() {
        return getHook().isLoggedIn();
    }

    public RSPlayer getMyPlayer() {
        return new RSPlayer(getHook().getMyPlayer(), this);
    }

    public RSPlayer[] getAllPlayers() {
        ArrayList<RSPlayer> all = new ArrayList<RSPlayer>();
        for (Player p : getHook().getPlayers()) {
            RSPlayer pl = new RSPlayer(p, this);
            all.add(pl);
        }
        return all.toArray(new RSPlayer[all.size()]);
    }

    public synchronized RSInterface[] getAll() {
        final com.vsbot.hooks.GameInterface[][] inters = getHook()
                .getInterfaceCache();
        if (inters == null) {
            return new RSInterface[0];
        }
        final List<RSInterface> out = new ArrayList<RSInterface>();
        for (int i = 0; i < inters.length; i++) {
            if (inters[i] != null) {
                final RSInterface in = get(i);
                out.add(in);
            }
        }
        return out.toArray(new RSInterface[out.size()]);
    }

    public synchronized RSInterface get(final int index) {
        RSInterface inter;
        final int cacheLen = mainCache.length;
        if (index < cacheLen) {
            inter = mainCache[index];
            if (inter == null) {
                inter = new RSInterface(index, this);
                mainCache[index] = inter;
            }
        } else {
            inter = sparseMap.get(index);
            if (inter == null) {
                if (index < cacheLen) {
                    inter = mainCache[index];
                    if (inter == null) {
                        inter = new RSInterface(index, this);
                        mainCache[index] = inter;
                    }
                } else {
                    inter = new RSInterface(index, this);
                    sparseMap.put(index, inter);
                }
            }
        }
        return inter;
    }

    public RSNPC[] getAllNpcs() {
        ArrayList<RSNPC> all = new ArrayList<RSNPC>();
        for (int i = 0; i < getHook().getNpcs().length; i++) {
            Npc a = getHook().getNpcs()[i];
            if (a != null && a.getDefinition() != null
                    && a.getDefinition().getName() != null) {
                RSNPC th = new RSNPC(a, i, this);
                all.add(th);
            }
        }
        return all.toArray(new RSNPC[all.size()]);
    }

    public RSNPC getNearestNpc(String name) {
        double dist = -1;
        RSNPC cur = null;
        int curX = getMyPlayer().getLocation().getX();
        int curY = getMyPlayer().getLocation().getY();

        for (int i = 0; i < getHook().getNpcs().length; i++) {
            Npc a = getHook().getNpcs()[i];
            if (a != null && a.getDefinition() != null
                    && a.getDefinition().getName() != null)
                if (a.getDefinition().getName().equalsIgnoreCase(name)) {
                    int x, y;

                    RSNPC th = new RSNPC(a, i, this);
                    x = th.getLocation().getX();
                    y = th.getLocation().getY();
                    double distance = Math.sqrt((curX - x) * (curX - x)
                            + (curY - y) * (curY - y));

                    if (distance < dist || dist == -1) {
                        dist = distance;
                        cur = new RSNPC(a, i, this);
                    }
                }
        }
        return cur;

    }

    public RSNPC getNearestNpc(int id) {
        double dist = -1;
        RSNPC cur = null;
        int curX = getMyPlayer().getLocation().getX();
        int curY = getMyPlayer().getLocation().getY();

        for (int i = 0; i < getHook().getNpcs().length; i++) {
            if (i == id) {
                Npc a = getHook().getNpcs()[i];
                if (a != null && a.getDefinition() != null
                        && a.getDefinition().getName() != null) {
                    int x, y;

                    RSNPC th = new RSNPC(a, i, this);
                    x = th.getLocation().getX();
                    y = th.getLocation().getY();
                    double distance = Math.sqrt((curX - x) * (curX - x)
                            + (curY - y) * (curY - y));

                    if (distance < dist || dist == -1) {
                        dist = distance;
                        cur = new RSNPC(a, i, this);
                    }
                    return cur;

                }

            }
        }
        return cur;
    }
}
