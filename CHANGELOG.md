# Changelog

## 2.2.0

Brings Minecraft 1.21.9, 1.21.10 and 1.21.11 to the same feature set as 1.21.1.

### Added
* **New hide animations:** smooth slide **up**, **left** and **right** (the existing slide is
  "down"). Choose them in the main settings or per element.
* **Air bubbles:** a new switch "Show bubbles while I am under water" (on by default).

### Changed
* **Slide animations always leave the screen completely.** Every element travels exactly one
  screen (plus a small margin) in the chosen direction, so all elements move at the same speed and
  none of them vanishes before it is out of sight. When the HUD returns, each element starts
  outside the screen and slides smoothly into its place.
* **One speed and one curve for every element:** hiding and showing take the same time
  (0.45 s) with a gentle start and end.
* **Smooth fade-out for every element:** the fade multiplies the colour of every sprite, text and
  filled rectangle drawn while an element is faded, so the HUD of other mods fades too instead of
  vanishing at once. *Limitation:* items in the hotbar are drawn by the game's item renderer,
  which does not accept a tint in these versions, so they disappear at the end of the fade.
* **Gradual shrink scales every element around its own centre.** The status effects, whose box is
  not fixed, are now measured for that.

## 2.1.1

### Added
* **Layers in the editor.** A small square button to the right of "Reset All Positions" switches
  the layer. **Layer 1** (opens by default): hearts, armor, hunger, hotbar, experience, the
  crosshair and the HUD of other mods. **Layer 2:** HUD that is not always on screen - air
  bubbles, horse jump bar, mount health and status effects.
* **Crosshair** is now an element: its size, hide animation and "stay visible" can be changed. It
  cannot be moved, and it never hides by default.
* **Air bubbles** have two switches in their panel: show the bubbles as soon as you are under
  water or only once you start losing air, and keep them until the air has refilled after you
  leave the water or hide them at once.
* **Horse jump bar** is hidden all the time and shows only while the jump key is held, until the
  horse jumps. The experience bar and the jump bar are separate elements.
* **Mount health** and **status effects** are elements of Layer 2.
* **Hotbar key press** is a new "Show HUD when..." event (on by default): the HUD appears on any
  press of 1 - 9, also when that slot is already selected.

### Fixed
* **Armor no longer moves up when you have moved it.** It is lifted above a second row of hearts
  only while both the armor and the hearts are in their default places.
* **Xaero's Minimap:** in 1.21.9+ the minimap is a picture-in-picture texture that was drawn
  without the transform, so only its coordinates moved; the picture is now moved with them.
* **Experience bar:** its empty background and its fill are wrapped separately and together, so
  they always move as one.
* **Opening the inventory:** elements of other mods that were hidden no longer slide in late and
  at a different speed after the inventory is closed; every element keeps animating even while it
  is not drawn. (Some mods do not draw their HUD at all while a screen is open; that is their own
  behaviour and cannot be changed from here.)

## 2.1.0

All changes since 2.0.0. Project for Minecraft Java **1.21.11** (Fabric). New creator credit:
Productive Analyst - **ipEtroIlIu**. Mr_Lan123 remains the main creator of the mod.

### Supported mods
Jade, WTHIT, Xaero's Minimap, Sodium Extra, FPS - Display, Simple Voice Chat, MiniHUD and
Overflowing Bars; AppleSkin and similar mods follow the vanilla element they draw into. None of
them is needed to run or build the mod.
**Want another mod supported? Write in Discord: https://discord.gg/7qzAw5Cckx**

### Added
* **Automatic Mode screen** (new button in the main settings):
  * **Hide delay** slider, 0.5 - 30 seconds (was a fixed 4 seconds).
  * **Show HUD when...** - 20 separate switches: takes damage, starts healing, starts drowning,
    takes fire damage, is burning, hunger / health / armor / experience changes, item is
    switched, hotbar is scrolled, attacks, interacts, jumps, starts sprinting, starts sneaking,
    picks up an item, uses an item, opens inventory, receives an effect. Enable All, Disable All
    and Defaults buttons; the list scrolls.
  * Stored in `config/dynamicrpghud.json` as `hideDelaySeconds` and `showHudWhen`.
* **Other mods' HUD in the editor**, mod by mod: Jade, WTHIT, Xaero's Minimap, Sodium Extra
  (including the FPS counter), **FPS - Display**, Simple Voice Chat, MiniHUD. They can be moved,
  scaled, hidden and animated; by default they stay visible.
* **Overflowing Bars support:** its health, armor and toughness bars now move, scale and hide with
  the vanilla Health / Armor elements. AppleSkin and similar mods follow their element.
* **Per-element hide animation** (slide, fade, instant, shrink or "same as global").
* Pure, unit-tested logic: `HudVisibility`, `HudGeometry`, `RevealDetector`.

### Changed
* Ported to Minecraft 1.21.11 (Matrix3x2f GUI stack; experience bar and level wrapped through the Fabric HUD registry, the rest with `@WrapMethod`; fade-out also tints text and filled rectangles).
* **Overflowing Bars:** in these versions its layer handlers return void and replace the vanilla health / armor elements; the hooks now match that, so its hearts, armor and toughness bars move, scale and hide.
* **Text never gets cut with "..." any more:** labels, descriptions, hints, panel headings and
  element name tags wrap onto a new line, and buttons and panels grow to fit.
* **WTHIT:** the tooltip itself is hooked (not the whole overlay), so its box, text and background
  move together.
* Reveal events are now the configurable list above (the old fixed rules: slot / item change,
  damage, hunger loss, new effect are on by default, plus the others listed as defaults).
* Mod version 2.1.0. Config format version 2 is unchanged (new fields are additive).

### Fixed
* **Armor selection box was one row too low.**
* **HUD transform can no longer leak** (`@WrapMethod` with `finally`); this fixed shifted screens,
  buttons and editor when another mod cancelled the same vanilla method.
* **Stay visible is absolute:** such elements are always fully drawn.
* **Multi-row hearts, absorption:** boxes, hit areas, shrink pivot and slide distance follow the
  real rectangle.
* **HUD flapping** while walking: movement debounce and minimum dwell.
* **Fast hotbar scrolling** between two ticks is no longer lost.
* Held-item durability changes no longer count as "item switched".
* Mixin config fails loudly if a target method disappears.

## 2.0.0

Supports Minecraft Java 1.21.11 - 1.21.11 and 26.1 - 26.3 (Fabric); 14 projects, one jar per Minecraft version.

### Added
* **HUD layout editor** ("Configure Elements" in the settings): dimmed semi-transparent
  background that keeps the game visible, every HUD element shown as a draggable box, and a
  top banner with instructions.
* **Per-element settings panel** next to the clicked element: X / Y position (typed), scale
  (slider or typed percentage), "stay visible when HUD hides" switch, reset for that element.
* **Fade-out animation selection:** smooth slide down, smooth fade-out via transparency
  (default), instant hide, gradual shrink.
* **HUD visibility trigger selection:** automatic (default) or hotkey only. New key binding
  "Toggle HUD", default **H**.
* **Air bubbles** are now a configurable element (Minecraft 1.21.2+).
* Single shareable config file `config/dynamicrpghud.json` (animation, trigger and every
  element's position, scale and visibility). Drop it into the config folder to apply it.
* Reset buttons: reset one element, reset all positions, reset everything.
* Modern flat UI: new buttons, sliders, panels and selection screens.
* Confirmation pop-up before "Reset Everything" and "Reset All Positions".
* In the main menu (no world loaded) the editor draws every element with its real vanilla
  textures instead of placeholders.
* Russian translation of every new text.

### Changed
* **The HUD no longer moves by itself.** The "floor" system that stacked kept elements at the
  bottom of the screen was removed; elements always stay at their vanilla positions unless you
  move them in the editor. This restores compatibility with other mods that interact with the HUD.
* The old per-element cross / check list moved into the editor ("Stay visible when HUD hides").
* Settings are now split into a main screen plus separate animation and trigger screens.
* Default hide animation is now the smooth fade-out (previously: slide down).
* Config format upgraded to version 2; version 1 files are still read.
* Mod version: 2.0.0 (jars are named `Dynamic-RPG-HUD-<minecraft>-2.0.0.jar`).

### Fixed
* **The mod now only touches the elements it owns.** An element that is exactly where vanilla
  draws it is not touched at all (no matrix push, no colour change), anything the mod changes is
  always restored right after that one element, and on 1.21.1 - 1.21.5 a safety net restores
  everything at the end of the HUD frame even if another mod cancelled an element halfway.
  Hidden elements are now skipped instead of drawn at zero size. HUD elements of other mods
  (and vanilla elements the mod does not manage) are never moved, scaled or faded.
* Elements can be grabbed and dragged anywhere in the editor, including under the top banner
  (they used to get stuck there).
* Elements can no longer be dragged (or scaled / typed) off the screen in the editor.
* "Smooth slide down" now moves each element from its actual, custom position all the way off
  the bottom of the screen, so elements placed near the top disappear completely.
* Extra rows of absorption hearts overlapping the armor bar: no element is relocated any more,
  so the vanilla spacing between hearts and armor is untouched.
* Wrapping the status bars no longer depends on one big method, so each element is animated
  independently.

### Technical
* 26.1 / 26.2 / 26.3: new projects for the unobfuscated Minecraft 26.x (Loom 1.18, Java 25,
  Gradle 9.7.1, Fabric API `HudElementRegistry`). The 26.1 jar supports 26.1, 26.1.1 and 26.1.2.
  26.2 moved screen handling to `Minecraft#gui`; 26.3 uses SDL input and Renderpearl.
* 1.21.1 - 1.21.5: Mixins around each vanilla HUD method (hotbar, hearts, armor, food, air,
  experience bar, experience level); fade through the shader colour.
* 1.21.6 - 1.21.11: Fabric API `HudElementRegistry` wrappers; fade through a mixin on
  `GuiGraphics.blitSprite` colour arguments (skipped silently if it does not match).
