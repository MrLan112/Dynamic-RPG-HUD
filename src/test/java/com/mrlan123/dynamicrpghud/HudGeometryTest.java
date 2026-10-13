package com.mrlan123.dynamicrpghud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HudGeometryTest {

    @Test
    void singleRowOfHearts() {
        assertEquals(1, HudGeometry.heartRows(20f, 0));
        assertArrayEquals(new int[]{100, 200, 81, 9}, HudGeometry.heartsRect(100, 200, 10, 20f, 0));
    }

    @Test
    void multipleRowsGrowUpwardsByLineHeight() {
        assertEquals(2, HudGeometry.heartRows(40f, 0));
        assertEquals(3, HudGeometry.heartRows(40f, 4));   // 22 hearts -> 3 rows
        int[] r = HudGeometry.heartsRect(100, 200, 9, 60f, 0); // 30 hearts, 3 rows
        assertEquals(200 - 2 * 9, r[1]);
        assertEquals(2 * 9 + 9, r[3]);
        // bottom edge stays where vanilla draws the first row
        assertEquals(200 + 9, r[1] + r[3]);
    }

    @Test
    void absorptionAddsRows() {
        assertEquals(1, HudGeometry.heartRows(20f, 0));
        assertEquals(2, HudGeometry.heartRows(20f, 2));
    }

    @Test
    void foodUsesRightEdge() {
        assertArrayEquals(new int[]{110, 300, 81, 9}, HudGeometry.foodRect(191, 300));
    }

    @Test
    void liveRectOverridesNominalOnlyForSameGuiSize() {
        HudGeometry.clear();
        int[] nominal = HudLayout.nominal(HudPart.HEALTH, 400, 300);
        assertArrayEquals(nominal, HudLayout.rect(HudPart.HEALTH, 400, 300));
        HudGeometry.set(HudPart.HEALTH, 400, 300, 109, 251, 81, 27);
        assertArrayEquals(new int[]{109, 251, 81, 27}, HudLayout.rect(HudPart.HEALTH, 400, 300));
        assertArrayEquals(HudLayout.nominal(HudPart.HEALTH, 500, 300), HudLayout.rect(HudPart.HEALTH, 500, 300));
        HudGeometry.clear();
    }

    @Test
    void armorSitsOneRowAboveTheHeartsAndFollowsExtraRows() {
        // vanilla passes the HEALTH baseline (y = h - 39); armor is drawn 10 px higher
        assertArrayEquals(new int[]{109, 251, 81, 9}, HudGeometry.armorRect(109, 261, 1, 10));
        // two heart rows with 9 px spacing: armor lifted by one more spacing
        assertArrayEquals(new int[]{109, 242, 81, 9}, HudGeometry.armorRect(109, 261, 2, 9));
        // and it matches the nominal single-row layout
        int h = 300;
        assertArrayEquals(HudLayout.nominal(HudPart.ARMOR, 400, h),
                HudGeometry.armorRect(400 / 2 - 91, h - 39, 1, 10));
    }

    @Test
    void armorOnlyFollowsExtraHeartRowsWhenBothAreInPlace() {
        assertEquals(2, HudGeometry.armorLiftRows(2, true, true));
        assertEquals(1, HudGeometry.armorLiftRows(2, false, true)); // armor moved
        assertEquals(1, HudGeometry.armorLiftRows(2, true, false)); // hearts moved
        assertEquals(1, HudGeometry.armorLiftRows(3, false, false));
    }
}
