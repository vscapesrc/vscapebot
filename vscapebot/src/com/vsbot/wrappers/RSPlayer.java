package com.vsbot.wrappers;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;
import com.vsbot.hooks.Entity;
import com.vsbot.hooks.Player;

public class RSPlayer extends RSEntity {
    Client hook;
    Methods methods;

    Player pl;

    public RSPlayer(Player p, Methods m) {
        this.methods = m;
        this.hook = methods.getHook();
        this.pl = p;
        super.setMouse(m.mouse);
    }

    protected Methods getMethods() {
        return methods;
    }

    @Override
    protected Entity getAccessor() {
        return pl;
    }

    public String getName() {
        if (pl != null && pl.getPlayerName() != null) {
            return pl.getPlayerName();
        }
        return null;
    }


    @Override
    public int getCurrentHealth() {

        if (pl.equals(hook.getMyPlayer())) {
            return hook.getCurrentStats()[3];
        }
        return super.getCurrentHealth();

    }

    @Override
    public int getMaxHealth() {
        if (pl.equals(hook.getMyPlayer())) {
            return hook.getMaxStats()[3];
        }
        return super.getMaxHealth();

    }

    public double getHpPercent() {
        if (pl.equals(hook.getMyPlayer()) || this.equals(getMethods().getMyPlayer())) {
            double maxHp = getMethods().getMyPlayer().getCurrentHealth();
            double currentHp = getMethods().getMyPlayer().getMaxHealth();
            double div = currentHp / 100;
            double res = maxHp / div;
            return res;
        } else {
            System.out.println(pl.getCurrentHealth() + "/" + pl.getMaxHealth());
            return pl.getMaxHealth() / 100 * pl.getCurrentHealth();
        }
    }

    @Override
    public RSTile getLocation() {
        Entity c = getAccessor();
        if (c == null) {
            return new RSTile(-1, -1);
        }
        int x = hook.getBaseX() + (c.getX() >> 7);
        int y = hook.getBaseY() + (c.getY() >> 7);
        return new RSTile(x, y);
    }


}
