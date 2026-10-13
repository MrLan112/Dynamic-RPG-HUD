package com.mrlan123.dynamicrpghud;



/**
 * Where vanilla actually drew each element in the last HUD frame. The vanilla methods receive
 * their own coordinates (heart row spacing, armor row, food right edge), so reading them from
 * the arguments keeps the editor boxes, the scale pivot and the slide distance correct when the
 * health bar has several rows (extra hearts, absorption) or other mods move the bars.
 */
public final class HudGeometry {

    private static final int[][] live = new int[HudPart.COUNT][4];
    private static final boolean[] has = new boolean[HudPart.COUNT];
    private static int stampW = -1;
    private static int stampH = -1;

    private HudGeometry() {
    }

    public static void set(HudPart part, int guiW, int guiH, int left, int top, int w, int h) {
        int i = part.ordinal();
        if (guiW != stampW || guiH != stampH) {
            clear();
            stampW = guiW;
            stampH = guiH;
        }
        live[i][0] = left;
        live[i][1] = top;
        live[i][2] = w;
        live[i][3] = Math.max(1, h);
        has[i] = true;
    }

    /** @return the live rect {left, top, w, h} or null if vanilla has not drawn it for this GUI size. */
    public static int[] get(HudPart part, int guiW, int guiH) {
        int i = part.ordinal();
        if (!has[i] || guiW != stampW || guiH != stampH) {
            return null;
        }
        return live[i];
    }

    public static void clear() {
        java.util.Arrays.fill(has, false);
    }

    // ---- pure geometry of the vanilla bars (unit-tested) -----------------------------------

    /** Number of heart rows vanilla draws for the given max health and absorption. */
    public static int heartRows(float maxHealth, int absorption) {
        int hearts = (int) Math.ceil((maxHealth + absorption) / 2.0f);
        return Math.max(1, (hearts + 9) / 10);
    }

    /** Box of the hearts: {@code y} is the bottom row, rows go upwards by {@code lineHeight}. */
    public static int[] heartsRect(int x, int y, int lineHeight, float maxHealth, int absorption) {
        int rows = heartRows(maxHealth, absorption);
        int top = y - (rows - 1) * lineHeight;
        return new int[]{x, top, 81, (rows - 1) * lineHeight + 9};
    }

    /**
     * Box of the armor row. Vanilla passes the HEALTH baseline as {@code y} and lifts the armor by
     * the extra heart rows plus one row (10 px) itself, so the box must do the same.
     */
    public static int[] armorRect(int x, int healthY, int heartRows, int lineHeight) {
        return new int[]{x, healthY - (heartRows - 1) * lineHeight - 10, 81, 9};
    }

    /**
     * Heart rows the armor row is lifted by. Vanilla always lifts the armor above every heart row;
     * that only makes sense while the armor and the hearts are both where vanilla puts them. When
     * the user has moved either one, the armor keeps the single-row position instead of jumping
     * when a second row of hearts appears.
     */
    public static int armorLiftRows(int heartRows, boolean armorInPlace, boolean heartsInPlace) {
        return armorInPlace && heartsInPlace ? heartRows : 1;
    }

    /** Box of the food bar: vanilla passes the RIGHT edge as {@code x}. */
    public static int[] foodRect(int rightX, int y) {
        return new int[]{rightX - 81, y, 81, 9};
    }
}
