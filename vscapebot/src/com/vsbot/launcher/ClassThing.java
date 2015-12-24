package com.vsbot.launcher;

// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.util.Hashtable;

public class ClassThing extends ClassLoader {

    public ClassThing() {
        _fldif = new Hashtable();
    }

    public synchronized Class loadClass(String s, boolean flag)
            throws ClassNotFoundException {
        Class class1 = (Class) _fldif.get(s);
        if (class1 != null)
            return class1;
        if (a != null) {
            byte abyte0[] = a.a(s);
            if (abyte0 != null) {
                Class class2 = defineClass(s, abyte0, 0, abyte0.length, super.getClass().getProtectionDomain());
                if (flag)
                    resolveClass(class2);
                _fldif.put(s, class2);
                return class2;
            }
        }
        return super.findSystemClass(s);
    }

    public Hashtable _fldif;
    public ClientClassLoader a;
}
