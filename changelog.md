### Additions
* new `command` lootable pool. Runs the command provided with op level 2
  * Add a `desc` to your lootable pools that use this, otherwise the players won't know what the command does!

### Changes
* the `lootables` command now has a `keyed` option, allowing for command-built keyed tables
  * Keyed tables can only be rolled by specific players a certain number of times
* `LootablesData` has been made internal, as it should have been

### Fixes
* None.