package com.vsbot.wrappers;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;
import com.vsbot.hooks.Entity;
import com.vsbot.hooks.Npc;


public class RSNPC extends RSEntity {

    private Npc lol;
    private int id;
    private Methods methods;
    private Client getClient(){
        return methods.getHook();
    }

    public Npc getHook() {
        return lol;
    }

    public RSNPC(Npc a, int id, Methods m) {
        this.methods = m;
        this.lol = a;
        this.id = id;
    }

    public RSNPC(int id) {
        this.id = id;
        this.lol = getClient().getNpcs()[id];
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return -1;
    }


    public String getName() {
        return lol.getDefinition().getName();
    }

    @Override
    protected Entity getAccessor() {
        return lol;
    }

    @Override
    protected Methods getMethods() {
        return methods;
    }


}
