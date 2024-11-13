# Lootables

### What is it?
Lootables aims to provide more featureful and flavorful loot drops for players.
* Random Loot, like vanilla loot tables
* Choice Tiles, loot box style, where the player gets to pick the specific loot pools they want with a GUI.
* More flexible reward types, like XP, attributes, even advancements.

### Features Include
* Custom loot system that includes the vanilla loot system, but expands on it.
  * Simplified variant of the vanilla loot pool available if you want to reward 1 item stack exactly the same every time.
  * Reward players with "intangible" things like attribute boosts, status effects, healing, XP, and more.
  * define how many times a player can receive a certain loot pool, as well as how many times a certain keyed instance of a table can be rolled at all. This allows for built in per-player loot mechanisms, one-time boss loot, and so on.
  * Conditional pools, of course. Alter which pools appear based on biome, dimension, player level, and anything else you can accomplish with vanilla loot predicates. These conditions define which pools can appear in choice tiles (see below), so you can tailor rewards to something like progression stage within one lootable table.
* Loot Boxes! Loot can be randomly rolled like vanilla loot, of course, but can also be presented to players in the form of Choice Tiles
  * Define how many tiles will appear and how many choices the player has amongst those choices.
  * Perfect for loot crates, loot bags, boss reward drops, and more
  * Choices can be set as "guaranteed", meaning they will always appear in the choice tiles. Add your important progression loot this way, so the player is always able to pick it
  * Combine "guaranteed" with a limited number of times the loot can be chosen and you have the perfect "key item" loot; only available once, but always available until chosen.
* Predefined helper item
  * Item class predefined for easy implementation of loot bags.
  * Behavior is component-defined for maximum flexibility with minimum item registration.
