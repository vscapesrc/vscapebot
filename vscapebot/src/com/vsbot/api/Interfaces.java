package com.vsbot.api;

import java.util.ArrayList;
import java.util.List;

import com.vsbot.hooks.Client;
import com.vsbot.hooks.GameInterface;
import com.vsbot.wrappers.RSInterface;
import com.vsbot.wrappers.RSInterfaceChild;

public class Interfaces {

    /**
     * @author Kubko
     * NOTE: INTERFACES ARE NOT WORKING 100% YET; WILL BE LOOKED INTO IN THE FUTURE
     */

    private Methods methods;

    public Client getClient() {
        return methods.getHook();
    }

    public Interfaces(Methods m) {
        this.methods = m;
    }


    public synchronized RSInterface get(final int index) {
        RSInterface inter;
        final int cacheLen = getAllParents().length;
        if (index < cacheLen) {
            inter = getAllParents()[index];
            if (inter == null) {
                inter = new RSInterface(index, methods);
            }
            return inter;
        }
        return null;
    }

    public Interfaces() {

    }

    /**
     * @return the list of all interface parents
     */
    public RSInterface[] getAllParents() {
        GameInterface[][] cache = getClient().getInterfaceCache();
        List<RSInterface> list = new ArrayList<RSInterface>();
        for (int i = 0; i < cache.length; i++) {
            GameInterface[] parent = cache[i];
            if (parent != null) {
                list.add(new RSInterface(parent, i));
            }
        }

        return list.toArray(new RSInterface[list.size()]);
    }

    /**
     * @param id of the interface
     * @return whether the interface with the given id is open
     */

    public boolean isParentInterfaceOpen(int id) {
        RSInterface[] cache = getAllParents();
        for (RSInterface loop : cache) {
            if (loop.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public boolean isOpen(int parent, int child) {
        return getClient().getInterfaceCache()[parent][child] != null;
    }

    public boolean isInterfaceOpen(int id) {
        RSInterface[] cache = getAllParents();
        for (RSInterface loop : cache) {
            for (RSInterfaceChild loop2 : loop.getChildren()) {
                if (loop2 != null && loop2.getId() != -1 && loop2.getId() == id) {

                    return true;
                }
            }
            if (loop != null && loop.getId() == id) {
                return true;
            }
        }
        return false;
    }

}
