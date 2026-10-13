package com.mrlan123.dynamicrpghud;

/** Vanilla positions of the HUD elements (scaled GUI pixels): live geometry when known, else nominal. */
public final class HudLayout {

    private HudLayout() {
    }

    /** Vanilla bounding box {left, top, width, height} of an element for a scaled GUI size. */
    public static int[] rect(HudElement element, int w, int h) {
        if (element instanceof ExternalElement) {
            return ((ExternalElement) element).rect(w, h); // null until the mod has drawn it
        }
        HudPart part = (HudPart) element;
        int[] live = HudGeometry.get(part, w, h);
        if (live != null) {
            return live;
        }
        return nominal(part, w, h);
    }

    /** Single-row vanilla layout, used when no live geometry has been captured yet (main menu). */
    public static int[] nominal(HudPart part, int w, int h) {
        int c = w / 2;
        switch (part) {
            case ARMOR:
                return new int[]{c - 91, h - 49, 81, 9};
            case HEALTH:
                return new int[]{c - 91, h - 39, 81, 9};
            case HUNGER:
                return new int[]{c + 10, h - 39, 81, 9};
            case AIR:
                return new int[]{c + 10, h - 49, 81, 9};
            case HOTBAR:
                return new int[]{c - 91, h - 22, 182, 22};
            case XP_BAR:
                return new int[]{c - 91, h - 29, 182, 5};
            case CROSSHAIR:
                return new int[]{(w - 15) / 2, (h - 15) / 2, 15, 15};
            case JUMP_BAR:
                return new int[]{c - 91, h - 29, 182, 5};
            case VEHICLE_HEALTH:
                return new int[]{c + 10, h - 39, 81, 9};
            case EFFECTS:
                return new int[]{w - 76, 1, 75, 49};
            case XP_LEVEL:
            default:
                return new int[]{c - 12, h - 35, 24, 9};
        }
    }
}
