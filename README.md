# Zyrex Client Beta

A Minecraft 1.8.9 Forge client with a clean, lightweight GUI.

## Features

- Module-based system with toggleable modifications
- Clean ClickGUI (press **RShift** to open)
- Category sidebar with combat, movement, player, and exploit modules
- Per-module settings (number sliders, mode toggles, keybinds)
- Config auto-save/load

## Building

**Requirements:** JDK 8 (GraalVM CE 8 or equivalent)

Run the build script:
```
build.bat
```

Or manually:
```
gradlew setupDecompWorkspace
gradlew build
```

Output JAR will be in `build/libs/`.

## Usage

1. Place the JAR in your `mods/` folder
2. Launch Minecraft 1.8.9 with Forge
3. Press **RShift** to open the ClickGUI
4. **Left-click** a module to toggle it
5. **Right-click** a module to expand its settings
6. **Middle-click** a module to set a keybind
