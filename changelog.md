### Additions
* Multiloader now! Fabric and Neoforge 1.21.1 currently supported.
* new `command` lootable pool. Runs the command provided with op level 2
  * Add a `desc` to your lootable pools that use this, otherwise the players won't know what the command does!
* New `resetKey` methods in `LootablsApi`. These reset a specific `IdKey`, allowing player(s) to start rolling tables keyed to that id again.
* Added `LootablesDataProvider` for generating Lootables data with datagen
  * This is currently untested! If you have any issues with it before I have a chance to get testing done myself, let me know and I'll get a patch out.

### Changes
* the `lootables` command now has a `keyed` option, allowing for command-built keyed tables
  * Keyed tables can only be rolled by specific players a certain number of times
* `LootablesData` has been made internal, as it should have been

### Fixes
* Don't "offer" empty choice pools to a player (which resulted in a blank screen with no way to make a choice)