package com.mrlan123.dynamicrpghud;

import java.util.EnumSet;
import java.util.Set;

/** Pure comparison of two snapshots: which events happened between them. */
public final class RevealDetector {

    private RevealDetector() {
    }

    public static Set<RevealTrigger> detect(PlayerSnapshot a, PlayerSnapshot b) {
        EnumSet<RevealTrigger> out = EnumSet.noneOf(RevealTrigger.class);
        if (a == null || b == null) {
            return out;
        }
        float before = a.health + a.absorption;
        float after = b.health + b.absorption;
        boolean damaged = after < before - 0.0001f;
        if (damaged) {
            out.add(RevealTrigger.DAMAGE);
            if (b.onFire || a.onFire) {
                out.add(RevealTrigger.FIRE_DAMAGE);
            }
        }
        if (b.health > a.health + 0.0001f) {
            out.add(RevealTrigger.HEAL);
        }
        if (b.health != a.health || b.absorption != a.absorption) {
            out.add(RevealTrigger.HEALTH);
        }
        if (b.air < a.air && b.air < b.maxAir) {
            out.add(RevealTrigger.DROWNING);
        }
        if (b.onFire) {
            out.add(RevealTrigger.BURNING);
        }
        if (b.food != a.food) {
            out.add(RevealTrigger.HUNGER);
        }
        if (b.armor != a.armor) {
            out.add(RevealTrigger.ARMOR);
        }
        if (b.experience != a.experience) {
            out.add(RevealTrigger.EXPERIENCE);
        }
        if (b.slot != a.slot) {
            out.add(RevealTrigger.HOTBAR_SCROLL);
        }
        if (b.heldItem != a.heldItem) {
            out.add(RevealTrigger.ITEM_SWITCH);
        }
        if (b.attackKey && !a.attackKey) {
            out.add(RevealTrigger.ATTACK);
        }
        if (b.useKey && !a.useKey) {
            out.add(RevealTrigger.INTERACT);
        }
        if (a.onGround && !b.onGround && b.velocityY > 0.0) {
            out.add(RevealTrigger.JUMP);
        }
        if (b.sprinting && !a.sprinting) {
            out.add(RevealTrigger.SPRINT);
        }
        if (b.sneaking && !a.sneaking) {
            out.add(RevealTrigger.SNEAK);
        }
        if (b.itemCount > a.itemCount) {
            out.add(RevealTrigger.PICKUP);
        }
        if (b.usingItem && !a.usingItem) {
            out.add(RevealTrigger.USE_ITEM);
        }
        for (Long e : b.effects) {
            if (!a.effects.contains(e)) {
                out.add(RevealTrigger.EFFECT);
                break;
            }
        }
        return out;
    }
}
