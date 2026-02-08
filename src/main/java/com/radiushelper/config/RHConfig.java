package com.radiushelper.config;

public class RHConfig {
    public enum Mode {
        PRIVATE,
        TNT
    }

    public int privateSize = 31;
    public int tntRadius = 12;
    public Mode mode = Mode.PRIVATE;

    public boolean renderFill = true;
    public boolean renderOutline = true;
    public boolean xray = true;
    public boolean showSelectionBox = true;
    public boolean showPointMarkers = true;
    public boolean showSelection = true;

    public int selectionColor = 0x5AA6FF;
    public int selectionAlpha = 80;

    public int privateColor = 0x28FFFF;
    public int privateAlpha = 160;

    public int tntColor = 0xFF7A2F;
    public int tntAlpha = 160;

    public void clamp() {
        if (privateSize < 1) {
            privateSize = 1;
        }
        if (tntRadius < 1) {
            tntRadius = 1;
        }
        selectionColor = clampColor(selectionColor);
        privateColor = clampColor(privateColor);
        tntColor = clampColor(tntColor);
        selectionAlpha = clamp255(selectionAlpha);
        privateAlpha = clamp255(privateAlpha);
        tntAlpha = clamp255(tntAlpha);
    }

    private static int clamp255(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 255) {
            return 255;
        }
        return value;
    }

    private static int clampColor(int value) {
        return value & 0xFFFFFF;
    }
}
