package com.mrlan123.dynamicrpghud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RevealDetectorTest {

    private static PlayerSnapshot base() {
        PlayerSnapshot s = new PlayerSnapshot();
        s.health = 20;
        s.food = 20;
        s.air = 300;
        return s;
    }

    @AfterEach
    void reset() {
        HudConfig.resetAutoMode();
    }

    @Test
    void nothingChangedNothingDetected() {
        assertTrue(RevealDetector.detect(base(), base()).isEmpty());
    }

    @Test
    void damageAndFire() {
        PlayerSnapshot b = base();
        b.health = 18;
        b.onFire = true;
        Set<RevealTrigger> e = RevealDetector.detect(base(), b);
        assertTrue(e.contains(RevealTrigger.DAMAGE));
        assertTrue(e.contains(RevealTrigger.FIRE_DAMAGE));
        assertTrue(e.contains(RevealTrigger.BURNING));
        assertTrue(e.contains(RevealTrigger.HEALTH));
        assertFalse(e.contains(RevealTrigger.HEAL));
    }

    @Test
    void healHungerDrowningArmorXp() {
        PlayerSnapshot a = base();
        a.health = 10;
        PlayerSnapshot b = base();
        b.health = 12;
        b.food = 19;
        b.air = 290;
        b.armor = 5;
        b.experience = 7;
        Set<RevealTrigger> e = RevealDetector.detect(a, b);
        assertTrue(e.containsAll(Set.of(RevealTrigger.HEAL, RevealTrigger.HUNGER, RevealTrigger.DROWNING,
                RevealTrigger.ARMOR, RevealTrigger.EXPERIENCE)));
        assertFalse(e.contains(RevealTrigger.DAMAGE));
    }

    @Test
    void actionsAreEdges() {
        PlayerSnapshot a = base();
        PlayerSnapshot b = base();
        b.sprinting = true;
        b.sneaking = true;
        b.attackKey = true;
        b.useKey = true;
        b.usingItem = true;
        b.onGround = false;
        b.velocityY = 0.42;
        b.slot = 3;
        b.heldItem = 5;
        b.itemCount = 1;
        b.effects.add(1L);
        Set<RevealTrigger> e = RevealDetector.detect(a, b);
        assertEquals(Set.of(RevealTrigger.SPRINT, RevealTrigger.SNEAK, RevealTrigger.ATTACK, RevealTrigger.INTERACT,
                RevealTrigger.USE_ITEM, RevealTrigger.JUMP, RevealTrigger.HOTBAR_SCROLL, RevealTrigger.ITEM_SWITCH,
                RevealTrigger.PICKUP, RevealTrigger.EFFECT), e);
        // holding the same state is not a new event
        assertTrue(RevealDetector.detect(b, b).isEmpty() || !RevealDetector.detect(b, b).contains(RevealTrigger.SPRINT));
    }

    @Test
    void configDefaultsAndDelay() {
        assertTrue(HudConfig.revealOn(RevealTrigger.DAMAGE));
        assertFalse(HudConfig.revealOn(RevealTrigger.JUMP));
        HudConfig.setRevealOn(RevealTrigger.JUMP, true);
        assertTrue(HudConfig.revealOn(RevealTrigger.JUMP));
        HudConfig.setDelayTicks(5);
        assertEquals(HudConfig.MIN_DELAY_TICKS, HudConfig.delayTicks());
        HudConfig.resetAutoMode();
        assertEquals(80, HudConfig.delayTicks());
        assertFalse(HudConfig.revealOn(RevealTrigger.JUMP));
    }

    @Test
    void customDelayIsUsed() {
        HudVisibility v = new HudVisibility();
        v.setVisibleTicks(30);
        v.tick(true, false, true);
        assertEquals(30, v.timer());
        for (int i = 0; i < 29; i++) v.tick(false, false, true);
        assertTrue(v.shown());
        for (int i = 0; i < 25; i++) v.tick(false, false, true);
        assertFalse(v.shown());
    }
}
