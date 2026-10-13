package com.mrlan123.dynamicrpghud;

/**
 * Notices every press of a hotbar key (1 - 9), also when it selects the slot that is already
 * selected (the slot does not change then, so the slot comparison cannot see it). Pure logic:
 * the caller passes the key states, so it can be unit-tested.
 */
public final class HotbarKeys {

    private final boolean[] down = new boolean[9];

    /** @return true when at least one key went from released to pressed since the last call */
    public boolean update(boolean[] now) {
        boolean pressed = false;
        for (int i = 0; i < down.length; i++) {
            boolean d = i < now.length && now[i];
            if (d && !down[i]) {
                pressed = true;
            }
            down[i] = d;
        }
        return pressed;
    }

    public void reset() {
        java.util.Arrays.fill(down, false);
    }
}
