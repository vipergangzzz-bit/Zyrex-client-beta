package com.zyrex.module;

public enum Category {
    Combat("Combat"),
    Movement("Movement"),
    Player("Player"),
    Exploit("Exploit"),
    OverPowered("OverPowered");

    private final String display;

    Category(String display) {
        this.display = display;
    }

    public String displayName() {
        return display;
    }
}
