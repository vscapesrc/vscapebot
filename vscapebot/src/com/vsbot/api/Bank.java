package com.vsbot.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vsbot.hooks.Client;
import com.vsbot.wrappers.RSBankItem;
import com.vsbot.wrappers.RSInterfaceChild;
import com.vsbot.wrappers.RSItem;

public class Bank {


    private Client getHook() {
        return methods.getHook();
    }

    private Methods methods;

    public Bank(Methods m) {
        this.methods = m;
    }

    /**
     * Deposits all items from the inventory
     */

    public void depositAll() {
        if (isOpen()) {
            if (methods.inventory.getCount() > 0) {
                RSItem[] items = methods.inventory.getItems();
                for (RSItem i : items) {
                    if (i.getId() > 1) {
                        i.interactBank("Store all");
                        try {
                            Thread.sleep(300);
                        } catch (Exception e) {

                        }
                        depositAll();
                        return;
                    }
                }


            }
        }
    }

    public void withdraw(final String name, final int count) {
        if (isOpen()) {
            if (count < 0) {
                throw new IllegalArgumentException("count (" + count + ") < 0");
            }
            int invCountStart = methods.inventory.getCount();
            RSBankItem rsi = getItem(name);
            if (rsi == null) {
                return;
            }

            // Check tab

            switch (count) {
                case 0:
                    rsi.interact("Withdraw All");
                    methods.sleep(1300);
                    break;
                case 1:
                    rsi.interact("Withdraw 1");
                    methods.sleep(1300);
                    break;
                case 5:
                    rsi.interact("Withdraw 5");

                    methods.sleep(1300);
                    break;
                case 10:
                    rsi.interact("Withdraw 10");
                    methods.sleep(1300);
                    break;
                default:
                    rsi.interact("Withdraw X");
                    methods.sleep(1300);
                    try {
                        methods.keyboard.sendKeys(String.valueOf(count));
                    } catch (Exception e) {

                    }
            }


            int newInvCount = methods.inventory.getCount();
            if (newInvCount <= invCountStart) {
                withdraw(name, count);
                return;
            }
        }
    }

    /**
     * @return The bank cache, used by internal operations
     */

    private int[] getBankCache() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (com.vsbot.hooks.GameInterface[] inface : getHook()
                .getInterfaceCache()) {
            if (inface != null) {
                for (com.vsbot.hooks.GameInterface iface2 : inface) {
                    if (iface2 != null) {

                        if (iface2.getId() == 7602279) {
                            if (iface2.getInv() != null) {
                                for (int a = 0; a < iface2.getInv().length; a++) {
                                    list.add(iface2.getInv()[a]);
                                }
                            }
                        }
                    }
                }
            }
        }
        return convertIntegers(list);
    }

    /**
     * @return
     */
    public int getBankCount() {
        return getBankItems().length;
    }

    private int[] convertIntegers(List<Integer> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }

    /**
     * Deposits all items to the bank except ones given
     *
     * @param names names of items not to be deposited
     */

    public void depositAllExcept(String... names) {
        int len = names.length;
        String[] cache = new String[len];
        ArrayList<String> alreadyDeposit = new ArrayList<String>();
        for (int i = 0; i < len; i++) {
            cache[i] = names[i].toLowerCase();
        }
        if (isOpen()) {
            RSItem[] items = methods.inventory.getItems();
            for (RSItem a : items) {
                if (a != null && a.getId() != -1 && a.getId() != 0) {
                    for (String name : cache) {
                        if (a.getName() != null) {
                            String itemName = a.getName().toLowerCase();
                            if (a != null && !itemName.equals(name)) {
                                if (!alreadyDeposit.contains(itemName)) {
                                    a.interactBank("Store All"); // TODO: make
                                    // code
                                    // neater,
                                    // its very
                                    // messy,
                                    // sorry :>
                                    methods.sleep(1000);
                                    alreadyDeposit.add(itemName);
                                    items = methods.inventory.getItems();
                                    for (String namee : cache) {
                                        for (RSItem aa : items) {
                                            if (aa.getId() != -1
                                                    && aa.getName() != null
                                                    && !aa.getName().equals(
                                                    names)) {
                                                depositAllExcept(names);
                                                return;
                                            }
                                        }
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * @return whether the bank is open
     */

    public boolean isOpen() {
        return getHook().getInterfaceCache()[114] != null;
        ///return methods.interfaces.isParentInterfaceOpen(114);
    }

    /**
     * @param name name of the bank item. NOTE bank items are not fully working yet, it has to be visible.
     * @return the bank item specified, if not found returns null.
     */

    public RSBankItem getItem(String name) {
        name = name.toLowerCase();
        for (RSBankItem item : getBankItems()) {
            System.out.println(item.getName());
            if (item.getName().toLowerCase().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * @return All the bank items in bank.
     */

    public RSBankItem[] getBankItems() {
        ArrayList<RSBankItem> list = new ArrayList<RSBankItem>();
        RSInterfaceChild iface = new RSInterfaceChild(getHook()
                .getInterfaceCache()[116], getHook()
                .getInterfaceCache()[116][103], 116, methods);
        int[] inv = iface.getAccessor().getInv();
        for (int slot = 0; slot < inv.length - 1; slot++) {
            RSBankItem item = new RSBankItem(inv[slot], slot, iface, methods);
            if (item != null && item.getName() != null && item.getName() != "null") {
                list.add(new RSBankItem(inv[slot], slot, iface, methods));
            }
        }

        return list.toArray(new RSBankItem[list.size()]);
    }
}
