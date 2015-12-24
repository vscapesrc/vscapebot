package com.vsbot.wrappers;

import java.util.ArrayList;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;
import com.vsbot.hooks.GameInterface;


public class RSInterface {


    private int index;
    private Methods methods;
    private Client hook;

    private com.vsbot.hooks.GameInterface[] ifaceHook;

    public com.vsbot.hooks.GameInterface[] getAccessor() {
        return ifaceHook;
    }

    public RSInterface(final int iface, Methods m) {
        this.methods = m;
        this.hook = methods.getHook();
        index = iface;
        ifaceHook = hook.getInterfaceCache()[iface];
    }

    public RSInterface(com.vsbot.hooks.GameInterface[] iface, int id) {
        index = id;
        ifaceHook = iface;
    }


    public GameInterface[] getParentInterface() {
        return ifaceHook;
    }

    public RSInterfaceChild[] getChildren() {
        ArrayList<RSInterfaceChild> child = new ArrayList<RSInterfaceChild>();
        for (GameInterface i : ifaceHook) {
            child.add(new RSInterfaceChild(ifaceHook, i, index, methods));
        }
        return child.toArray(new RSInterfaceChild[child.size()]);
    }

    public int getId() {
        return index;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof RSInterface) {
            final RSInterface inter = (RSInterface) obj;
            return inter.index == index;
        }
        return false;
    }


    @Override
    public int hashCode() {
        return index;
    }

}
