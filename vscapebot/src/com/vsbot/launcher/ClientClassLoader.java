package com.vsbot.launcher;

// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClientClassLoader {

    public ClientClassLoader(byte abyte0[])
            throws IOException {
        a = new Hashtable();
        ZipInputStream zipinputstream = new ZipInputStream(new ByteArrayInputStream(abyte0));
        byte abyte1[] = new byte[1000];
        do {
            ZipEntry zipentry = zipinputstream.getNextEntry();
            if (zipentry == null)
                break;
            String s = zipentry.getName();
            if (s.endsWith(".class")) {
                s = s.substring(0, s.length() - 6);
                s = s.replace('/', '.');
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                do {
                    int i = zipinputstream.read(abyte1, 0, 1000);
                    if (i == -1)
                        break;
                    bytearrayoutputstream.write(abyte1, 0, i);
                } while (true);
                byte abyte2[] = bytearrayoutputstream.toByteArray();
                a.put(s, abyte2);
            }
        } while (true);
        zipinputstream.close();
    }

    public byte[] a(String s) {
        return (byte[]) a.remove(s);
    }

    public Hashtable a;
}
