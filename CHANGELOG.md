# Changelog

## 2.2.0

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
  (0.45 s) with a gentle start and end, instead of the old exponential easing that started
  abruptly and crawled at the end.
* **Smooth fade-out for every element:** the fade is applied to every colour change made while an
  element is drawn, so elements (and other mods' HUD) that reset the colour inside their drawing
  code no longer pop back to opaque and vanish at once.
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
* **Air bubbles** (now supported on 1.21.1) have two switches in their panel: show the bubbles as
  soon as you are under water or only once you start losing air, and keep them until the air has
  refilled after you leave the water or hide them at once.
* **Horse jump bar** is hidden all the time and shows only while the jump key is held, until the
  horse jumps.
* **Mount health** and **status effects** are elements of Layer 2.
* **Hotbar key press** is a new "Show HUD when..." event (on by default): the HUD appears on any
  press of 1 - 9, also when that slot is already selected.

### Fixed
* **Armor no longer moves up when you have moved it.** It is lifted above a second row of hearts
  only while both the armor and the hearts are in their default places.
* **Fade-out is smooth for every element.** Block items in the hotbar stayed fully visible during
  the fade and vanished at the end; they are now faded like the rest.
* **Opening the inventory:** elements of other mods that were hidden no longer slide in late and
  at a different speed after the inventory is closed; every element keeps animating even while it
  is not drawn. (Some mods do not draw their HUD at all while a screen is open; that is their own
  behaviour and cannot be changed from here.)
