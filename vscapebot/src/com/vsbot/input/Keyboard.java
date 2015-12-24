package com.vsbot.input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Random;

import com.vsbot.api.Methods;
import com.vsbot.hooks.Client;

/**
 * Sends completly legit key events to a the Component returned by ac.getKeyListener().
 * This class emulates a standard US keyboard.
 * Copyright (C) 2007  Travis Burtrum (moparisthebest)
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * <p/>
 * The code *may* be used under a lesser license (such as the LGPL) only with
 * express written permission from Travis Burtrum (moparisthebest)
 */
public class Keyboard {

    /* private AccessorMethods ac;

   /**
    * Sole constructor.
    * @param ac AccessorMethods class.
    */
    Methods methods;

    public Client getHook() {
        return methods.getHook();
    }

    public Keyboard(Methods m) {
        this.methods = m;
    }

    /**
     * This can be called from scripts and sends a String to the applet.
     *
     * @param s The string to send.
     */
    public void sendKeys(String s) {
        /////  FocusManager.readyForInput(ac.getFocusListener());
        Component target = getHook().getGameComponentHook(0);
        pressTime = System.currentTimeMillis();
        for (char c : s.toCharArray())
            for (KeyEvent ke : createKeyClick(target, c)) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                target.dispatchEvent(ke);
            }
        clickKey(10); /// press enter
    }

    /**
     * This can be called from scripts and sends a character to the applet.
     *
     * @param c The character to send.
     */
    public void clickKey(char c) {
        ////// FocusManager.readyForInput(ac.getFocusListener());
        Component target = getHook().getGameComponentHook(0);
        pressTime = System.currentTimeMillis();
        for (KeyEvent ke : createKeyClick(target, c))
            target.dispatchEvent(ke);
    }

    /**
     * This can be called from scripts and sends a key to the applet.
     *
     * @param keyCode The key code to send.
     */
    public void clickKey(int keyCode) {
        ////  FocusManager.readyForInput(ac.getFocusListener());
        Component target = getHook().getGameComponentHook(0);
        pressTime = System.currentTimeMillis();
        for (KeyEvent ke : createKeyClick(target, keyCode))
            target.dispatchEvent(ke);
    }

    /**
     * Causes a key press event, make sure you use releaseKey(int) afterwards
     *
     * @param keyCode the key code to send
     */
    public void pressKey(int keyCode) {
        //FocusManager.readyForInput(ac.getFocusListener());
        Component target = getHook().getGameComponentHook(0);
        pressTime = System.currentTimeMillis();
        KeyEvent ke = createKeyPress(target, keyCode);
        target.dispatchEvent(ke);
    }

    /**
     * Causes a key release event
     *
     * @param keyCode the key code to send
     */
    public void releaseKey(int keyCode) {
        ///FocusManager.readyForInput(ac.getFocusListener());
        Component target = getHook().getGameComponentHook(0);
        pressTime = System.currentTimeMillis();
        KeyEvent ke = createKeyRelease(target, keyCode);
        target.dispatchEvent(ke);
    }

    /* Internal Event construction  */

    private static HashMap<Character, Character> specialChars;
    private static long pressTime;

    static {
        char[] spChars = {'~', '!', '@', '#', '%', '^', '&', '*', '(', ')', '_', '+', '{', '}', ':', '<', '>', '?', '"', '|'};
        char[] replace = {'`', '1', '2', '3', '5', '6', '7', '8', '9', '0', '-', '=', '[', ']', ';', ',', '.', '/', '\'', '\\'};
        specialChars = new HashMap<Character, Character>(spChars.length);
        for (int x = 0; x < spChars.length; ++x)
            specialChars.put(spChars[x], replace[x]);
    }

    /**
     * Gets a random number.
     * todo: need some testing to determine a legitimate lagtime
     *
     * @return Random number used in bewtween keystrokes and also presses.
     */
    private static long getRandom() {
        Random rand = new Random();
        return rand.nextInt(100) + 40;
    }

    /**
     * Generates events for pressing a key that sends a character, also takes care of the needed masks and
     * changes whatever is needed for special characters so that the events are legitimate.
     *
     * @param target Component the events are being sent to.
     * @param c      The character to send.
     * @return A KeyEvent array for you to dispatch to the component.
     */
    private static KeyEvent[] createKeyClick(Component target, char c) {
        //takes about 2x as long to get to another key than to release a key?
        pressTime += 2 * getRandom();

        Character newChar = specialChars.get(c);
        int keyCode = (int) Character.toUpperCase((newChar == null) ? c : newChar);

        if (Character.isLowerCase(c) || (!Character.isLetter(c) && (newChar == null))) {
            KeyEvent pressed = new KeyEvent(target, KeyEvent.KEY_PRESSED, pressTime, 0, keyCode, c);
            KeyEvent typed = new KeyEvent(target, KeyEvent.KEY_TYPED, pressTime, 0, 0, c);
            pressTime += getRandom();
            KeyEvent released = new KeyEvent(target, KeyEvent.KEY_RELEASED, pressTime, 0, keyCode, c);

            return new KeyEvent[]{pressed, typed, released};
        } else {
            KeyEvent shiftDown = new KeyEvent(target, KeyEvent.KEY_PRESSED, pressTime, KeyEvent.SHIFT_MASK, KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED);

            pressTime += getRandom();
            KeyEvent pressed = new KeyEvent(target, KeyEvent.KEY_PRESSED, pressTime, KeyEvent.SHIFT_MASK, keyCode, c);
            KeyEvent typed = new KeyEvent(target, KeyEvent.KEY_TYPED, pressTime, KeyEvent.SHIFT_MASK, 0, c);
            pressTime += getRandom();
            KeyEvent released = new KeyEvent(target, KeyEvent.KEY_RELEASED, pressTime, KeyEvent.SHIFT_MASK, keyCode, c);
            pressTime += getRandom();
            KeyEvent shiftUp = new KeyEvent(target, KeyEvent.KEY_RELEASED, pressTime, 0, KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED);

            return new KeyEvent[]{shiftDown, pressed, typed, released, shiftUp};
        }
    }

    /**
     * Generates events for pressing a key that doesn't send a character, also takes care of the needed masks.
     *
     * @param target  Component the events are being sent to.
     * @param keyCode The keycode to send.
     * @return A KeyEvent array for you to dispatch to the component.
     */
    private static KeyEvent[] createKeyClick(Component target, int keyCode) {
        int modifier = 0;
        switch (keyCode) {
            case KeyEvent.VK_SHIFT:
                modifier = KeyEvent.SHIFT_MASK;
                break;
            case KeyEvent.VK_ALT:
                modifier = KeyEvent.ALT_MASK;
                break;
            case KeyEvent.VK_CONTROL:
                modifier = KeyEvent.CTRL_MASK;
                break;
        }
        KeyEvent pressed = new KeyEvent(target, KeyEvent.KEY_PRESSED, pressTime, modifier, keyCode, KeyEvent.CHAR_UNDEFINED);
        KeyEvent released = new KeyEvent(target, KeyEvent.KEY_RELEASED, pressTime + getRandom(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);

        return new KeyEvent[]{pressed, released};
    }

    private static KeyEvent createKeyPress(Component target, int keyCode) {
        int modifier = 0;
        switch (keyCode) {
            case KeyEvent.VK_SHIFT:
                modifier = KeyEvent.SHIFT_MASK;
                break;
            case KeyEvent.VK_ALT:
                modifier = KeyEvent.ALT_MASK;
                break;
            case KeyEvent.VK_CONTROL:
                modifier = KeyEvent.CTRL_MASK;
                break;
        }
        KeyEvent pressed = new KeyEvent(target, KeyEvent.KEY_PRESSED, pressTime, modifier, keyCode, KeyEvent.CHAR_UNDEFINED);

        return pressed;
    }

    //creates a key release event
    private static KeyEvent createKeyRelease(Component target, int keyCode) {
        int modifier = 0;
        switch (keyCode) {
            case KeyEvent.VK_SHIFT:
                modifier = KeyEvent.SHIFT_MASK;
                break;
            case KeyEvent.VK_ALT:
                modifier = KeyEvent.ALT_MASK;
                break;
            case KeyEvent.VK_CONTROL:
                modifier = KeyEvent.CTRL_MASK;
                break;
        }
        KeyEvent released = new KeyEvent(target, KeyEvent.KEY_RELEASED, pressTime + getRandom(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);

        return released;
    }
}