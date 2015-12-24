package com.vsbot.hooks;

import java.awt.*;

public interface Client extends RSApplet {

    public String getUsername();

    public String getPassword();

    public Component getGameComponentHook(int a);

    public Npc[] getNpcs();

    public int getBaseX();

    public int getBaseY();

    public Player getMyPlayer();

    public int getPlayerId();

    public int getTime();


    public int[] getMaxStats();

    public int[] getCurrentStats();

    public int[] getExperience();

    public boolean isLoggedIn();

    public Player[] getPlayers();

    public String getLoginMessage2();

    public boolean isMenuOpen();

    public String[] getMenuActionNames();

    public int getMenuActionRow();

    int getMenuOffsetX();

    int getMenuOffsetY();

    String[] getChatMessages();

    int getMinimapInt1();

    int getMinimapInt2();

    int getMinimapInt3();

    int getXCameraCurve();

    int getYCameraCurve();

    int getXCameraPos();

    int getYCameraPos();

    int getZCameraPos();

    int getPlane();

    public void setLoginIndex(int i);

    public int getLoginIndex();

    int getOpenTab();

    int[][][] getGroundIntArray();

    byte[][][] getGroundByteArray();

    GameInterface[][] getInterfaceCache();


    public String getSelectedItemName();

    public ItemDef getForId(int id);

    public ObjectDef getForIdObject(int id);

    int getMouseX();

    int getMouseY();

    public NodeList[][][] getGroundArray();

    public WorldController getWorldController();

}