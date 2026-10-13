package com.mrlan123.dynamicrpghud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

/** Plain copy of the player values {@link RevealDetector} compares from tick to tick. */
public final class PlayerSnapshot {

    public float health;
    public float absorption;
    public int food;
    public int air;
    public int maxAir = 300;
    public boolean onFire;
    public int armor;
    public int experience;
    public int slot;
    public int heldItem;
    public int itemCount;
    public boolean onGround = true;
    public double velocityY;
    public boolean sprinting;
    public boolean sneaking;
    public boolean usingItem;
    public boolean attackKey;
    public boolean useKey;
    public Set<Long> effects = new HashSet<>();

    /** Reads the current state of the player. */
    public static PlayerSnapshot of(Minecraft mc, LocalPlayer p) {
        PlayerSnapshot s = new PlayerSnapshot();
        s.health = p.getHealth();
        s.absorption = p.getAbsorptionAmount();
        s.food = p.getFoodData().getFoodLevel();
        s.air = p.getAirSupply();
        s.maxAir = p.getMaxAirSupply();
        s.onFire = p.isOnFire();
        s.armor = p.getArmorValue();
        s.experience = p.experienceLevel * 100000 + Math.round(p.experienceProgress * 1000f);
        s.slot = p.getInventory().selected;
        ItemStack held = p.getMainHandItem();
        s.heldItem = held == null || held.isEmpty() ? 0 : System.identityHashCode(held.getItem());
        int count = 0;
        int size = p.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack st = p.getInventory().getItem(i);
            if (st != null && !st.isEmpty()) {
                count += st.getCount();
            }
        }
        s.itemCount = count;
        s.onGround = p.onGround();
        s.velocityY = p.getDeltaMovement().y;
        s.sprinting = p.isSprinting();
        s.sneaking = p.isShiftKeyDown();
        s.usingItem = p.isUsingItem();
        if (mc.options != null) {
            s.attackKey = mc.options.keyAttack.isDown();
            s.useKey = mc.options.keyUse.isDown();
        }
        for (MobEffectInstance e : p.getActiveEffects()) {
            s.effects.add(((long) e.getEffect().hashCode() << 8) | (e.getAmplifier() & 0xFF));
        }
        return s;
    }
}
