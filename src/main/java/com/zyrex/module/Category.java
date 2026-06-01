package com.zyrex.module;

public enum Category {
    Combat, Movement, Player, Exploit, OverPowered;

    public String displayName() {
        return name();
    }
}
