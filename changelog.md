### Additions
* None.

### Changes
* Lootable tables now reload (async) on /reload. Previously they only loaded on server start.
* Sync data now supports player-specific client data

### Fixes
* Fix loot table entries not loading properly.
* Pool loot entries properly display their fallback description.
* Fix potential sync issue on quit and rejoin thanks to stale registry references.