package com.vsbot.api;

import com.vsbot.hooks.Client;

public class Skills {

    Methods methods;

    public Client getHook() {
        return methods.getHook();
    }

    public Skills(Methods m) {
        this.methods = m;
    }

    public Skills() {
        // TODO Auto-generated constructor stub
    }

    public final int ATTACK = 0;
    public final int DEFENSE = 1;
    public final int STRENGTH = 2;
    public final int HITPOINTS = 3;
    public final int RANGED = 4;
    public final int PRAYER = 5;
    public final int MAGIC = 6;
    public final int COOKING = 7;
    public final int WOODCUTTING = 8;
    public final int FLETCHING = 9;
    public final int FISHING = 10;
    public final int FIREMAKING = 11;
    public final int CRAFTING = 12;
    public final int SMITHING = 13;
    public final int MINING = 14;
    public final int HERBLORE = 15;
    public final int AGILITY = 16;
    public final int THIEVING = 17;
    public final int SLAYER = 18;
    public final int FARMING = 19;
    public final int RUNECRAFTING = 20;


    /**
     * Gets a level in specified skill
     *
     * @param skillId ID of skill
     * @return the level in specified skill
     */
    public int getLevel(int skillId) {
        return getHook().getCurrentStats()[skillId];
    }

    /**
     * Gets experience in specified skill
     *
     * @param skillId ID of skill
     * @return the experience in specified skill
     */

    public int getExperience(int skillId) {
        return getHook().getExperience()[skillId];
    }

}
