# Dynamic RPG HUD

**Version 2.2.0 · Fabric · Minecraft Java 1.21.1, 1.21.9, 1.21.10, 1.21.11 · client-side · by Mr_Lan123 and ipEtroIlIu**

Dynamic RPG HUD hides your hotbar and status bars while you do not need them and brings them
back the moment something happens. Every element can be moved, resized and tuned in a visual
editor, and the HUD of several popular mods can be tuned the same way.

The mod is installed on the client only. It never moves anything on its own: every element stays
where vanilla draws it until you move it.

<p align="left">
  <!-- Картинка-ссылка -->
  <a href="https://modrinth.com/mod/dynamic-rpg-hud" target="_blank" style="vertical-align: middle; margin-right: 8px;">
    <img src="https://cdn2.steamgriddb.com/logo_thumb/7246274686efd0f0309184fed45ee310.png" alt="Логотип Dynamic RPG HUD" width="220">
  </a>
  <p align="left">
  <!-- Картинка-ссылка -->
  <a href="https://discord.com/invite/7qzAw5Cckx" target="_blank" style="vertical-align: middle; margin-right: 8px;">
    <img src="https://thumb.wikimedia.org/wikipedia/ru/thumb/b/b7/Discord_logo_svg.svg/3840px-Discord_logo_svg.svg.png?utm_source=ru.wikipedia.org&utm_campaign=index&utm_content=thumbnail" alt="Discord" width="220">
  </a>


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

## Building from source

Requires JDK 21.

```
./gradlew build
```

The mod jar is written to `build/libs/` (use the file without `-sources` / `-dev` in its name).
Unit tests for the pure logic run as part of the build.

## Reporting problems

Open an [issue](https://github.com/MrLan112/Dynamic-RPG-HUD/issues) with the Minecraft version,
the list of installed mods and `latest.log`. Requests to support another mod are welcome too.

## License
[MIT](LICENSE)

## Credits
Main creator: **Mr_Lan123**. Also: **Productive Analyst - ipEtroIlIu**.

---

## How it works

| Part | Approach |
| --- | --- |
| Elements | `@WrapMethod` on `Gui#renderItemHotbar`, `renderHearts`, `renderArmor`, `renderFood`, `renderExperienceBar`, `renderExperienceLevel`, `renderCrosshair`, `renderEffects`, `renderVehicleHealth`, `renderJumpMeter`; the transform is pushed and popped in `try/finally` around the vanilla call |
| Geometry | The real rectangle of hearts / armor / food is read from the vanilla arguments each frame (multi-row hearts, absorption) |
| Visibility | `HudVisibility`, a pure state machine (80 tick timer, debounced movement, minimum dwell), plus a per-frame slot observer |
| Config | `config/dynamicrpghud.json`, format version 2 (unchanged) |

Only the vanilla elements above (hearts, armor, hunger, air bubbles, hotbar, experience bar and level, crosshair, horse jump bar, mount health, status effects) plus the mods listed below are touched. The air bubbles are drawn inline by 1.21.1, so their sprite calls are wrapped one by one.

See [`CHANGELOG.md`](CHANGELOG.md) for what changed.

## Supported mods

| Mod | How it is supported | Checked against |
|---|---|---|
| Overflowing Bars | `GuiLayerHandler.onRenderPlayerHealth / onRenderArmorLevel / onRenderToughnessLevel` are wrapped as HEALTH / ARMOR (Puzzles Lib fires them outside the vanilla methods) | OB / Puzzles Lib source |
| AppleSkin and mods drawing inside vanilla methods | Move / hide with the vanilla element (the wrapper surrounds their code) | AppleSkin source |
| Jade | `ClientProxy.onRenderTick` | Jade 1.21 Fabric source |
| WTHIT | `TooltipRenderer.renderUncached` (the tooltip itself, so the box and background follow) | WTHIT 1.21.1 source |
| Xaero's Minimap | `MinimapRenderer.render(...)` | descriptor from XaeroPlus; the mod is closed source; the editor box is an approximation |
| Sodium Extra | `SodiumExtraClientMod.onHudRender` and `SodiumExtraHud.onHudRender` | source |
| FPS - Display | the FPS line drawn from `Gui#render` is recognised in `GuiGraphics#drawString` | source |
| Simple Voice Chat | `RenderEvents.onRenderHUD` | source |
| MiniHUD | `RenderHandler.onRenderGameOverlayPost` | source (Yarn-named build) |

None of these mods is needed to build or run this mod; each hook is applied only if the mod is installed, and a hook that does not match simply does nothing.


Requests for other mods: https://discord.gg/7qzAw5Cckx
