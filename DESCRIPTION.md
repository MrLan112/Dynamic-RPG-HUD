# Dynamic RPG HUD

**Version 2.2.0 · Fabric · Minecraft Java 1.21.11 · client-side · by Mr_Lan123 and ipEtroIlIu**

Dynamic RPG HUD hides your hotbar and status bars while you do not need them and brings them
back the moment something happens. Every element can be moved, resized and tuned in a visual
editor, and the HUD of several popular mods can be tuned the same way.

The mod is installed on the client only. It never moves anything on its own: every element stays
where vanilla draws it until you move it.

## Quick start

1. Install Fabric Loader and Fabric API, then put the mod into your `mods` folder.
2. Open the settings through **Mod Menu** (Mods → Dynamic RPG HUD → Configure), or bind a key to
   **Open Dynamic RPG HUD Settings** in Controls (it has no default key).
3. Press **H** at any time to switch the HUD when "Hotkey only" mode is selected.

Out of the box the HUD works in Automatic mode: it hides while you stand still at full health and
hunger or while you run around, and appears when something happens.

## What every button does

### Main settings screen
* **Configure Elements** - opens the layout editor (see below).
* **Fade-out Animation** - the default way the HUD disappears: smooth fade-out (default), smooth
  slide down / up / left / right, gradual shrink or instant hide. Any single element can override it in the editor.
* **HUD Visibility Trigger** - **Automatic** (the HUD reacts to what you do) or **Hotkey only**
  (only the toggle key, H by default, shows or hides it).
* **Automatic Mode** - fine settings for Automatic mode (see below).
* **Reset Everything** - returns every setting to the default. You will be asked to confirm.
* **Done** - saves and closes.

### Layout editor
The game stays visible behind a dark, semi-transparent layer and every HUD element is drawn as a
box.

* **Layer button** (small square button to the right of "Reset All Positions"): switches between
  **Layer 1** (default) - hearts, armor, hunger, hotbar, experience, the crosshair and the HUD of
  other mods - and **Layer 2** - HUD that is not always on screen: air bubbles, horse jump bar,
  mount health and status effects.
* **Drag a box** with the mouse to move the element.
* **Click a box** to open its panel:
  * **X / Y** - type the offset from the normal position (0 / 0 means "as in vanilla").
  * **Scale** - a slider or a typed percentage.
  * **Stay visible when HUD hides** - this element never fades. Vanilla elements are off by
    default, elements of other mods are on by default.
  * **Hide animation** - an animation for this one element, or "Same as global setting".
  * **Reset This Element** - puts only this element back.
  * **Crosshair:** it cannot be moved. You can only change its scale, its hide animation and
    whether it hides at all (it never hides by default).
  * **Air Bubbles** have three extra switches: show the bubbles at all while you are under water; show the bubbles as soon as you are under water (or
    only once you start losing air), and keep them until the air has refilled after you leave the
    water (or hide them at once).
  * **Horse Jump Bar** is hidden all the time and shows only while you hold the jump key, until
    the horse jumps. "Stay visible" turns this off.
* **Reset All Positions** - puts every element back at its place (with confirmation).

### Automatic Mode screen
* **Hide delay** - how many seconds the HUD stays on screen after the last event. From 0.5 to 30
  seconds, 4 seconds by default.
* **Show HUD when...** - a list of switches (✓ on, ✗ off). Scroll it with the mouse wheel.
  The HUD appears when any switched-on event happens:

| Event | Default |
|---|---|
| Player takes damage | ✓ |
| Player starts healing | ✓ |
| Player starts drowning | ✓ |
| Player takes fire damage | ✓ |
| Player is burning | ✓ |
| Hunger changes | ✓ |
| Health changes (any change) | ✗ |
| Armor changes | ✓ |
| Experience changes | ✗ |
| Item is switched | ✓ |
| Hotbar is scrolled | ✓ |
| Hotbar key is pressed (also for the slot that is already selected) | ✓ |
| Player attacks | ✗ |
| Player interacts (right click) | ✗ |
| Player jumps | ✗ |
| Player starts sprinting | ✗ |
| Player starts sneaking | ✗ |
| Player picks up an item | ✗ |
| Player uses an item | ✗ |
| Player opens inventory | ✓ |
| Player receives an effect | ✓ |

* **Enable All / Disable All / Defaults** - quick presets for the whole list.

Note: the HUD hides after the delay only while you are moving or while your health and hunger
are full. If you stand still with damaged health or hunger, it stays visible.

## Supported mods

When one of these mods is installed, its HUD appears in the layout editor as its own box. You can
move it, scale it, choose whether it hides, and give it its own animation. By default the HUD of
other mods does **not** hide.

| Mod | What you can edit |
|---|---|
| Jade | the tooltip |
| WTHIT | the tooltip (and its background) |
| Xaero's Minimap | the minimap (the editor box is an approximation) |
| Sodium Extra | the FPS / coordinates overlay |
| FPS - Display | the FPS counter |
| Simple Voice Chat | the voice icons |
| MiniHUD | the info lines |
| Overflowing Bars | the health, armor and toughness bars (move and hide together with the vanilla Health / Armor elements) |
| AppleSkin and similar | they follow the vanilla element they draw into |

None of these mods is required, and nothing is bundled.

**Want another mod supported?** Write to us in Discord: https://discord.gg/7qzAw5Cckx

## Good to know
* Settings are stored in `config/dynamicrpghud.json`. Send that file to a friend to share your
  setup.
* Support for other mods is built from their public source code. If a mod updates and its HUD
  stops reacting, tell us in Discord.
* Russian and English are included.

## Credits
Main creator: **Mr_Lan123**. Also: **Productive Analyst - ipEtroIlIu**.
