# advancement confetti

<img src="src/main/resources/assets/advancementconfetti/icon.png" width="128" alt="icon">

confetti shoots across your screen every time you get an advancement, and it takes a screenshot a second later so you actually keep the moment instead of forgetting it happened.

rare (purple) advancements get their own thing - way more confetti, gold and purple instead of the normal colours, and a different sound.

works on vanilla servers, nobody else needs it.

built for minecraft 26.1.2 on fabric.

## download

get it from [curseforge](https://www.curseforge.com/minecraft/mc-mods/advancementconfetti), or grab the jar straight from [releases](https://github.com/maarrccoos/AdvancementConfetti/releases) and drop it in your `mods` folder. you need fabric loader and fabric api.

coming to modrinth soon.

## what it does

get an advancement, confetti fires in from both sides of the screen and falls, sound plays, screenshot happens a second later so the toast is still in frame.

screenshots go in `advancementconfetti/screenshots` in your game folder, not your normal screenshots folder, so they don't get mixed up with the ones you took.

## the gallery

open it from the small <ins>Gallery</ins> button top right of the pause menu, or `/advancementconfetti gallery`.

- click a screenshot to open it full size
- tick the little box top left of one to select it
- <ins>delete</ins> removes everything you've selected from your disk, so you can free up space if needed
- <ins>open folder</ins> takes you to them on your disk
- gold border means it was a rare / challenge advancement, grey means normal

each one shows the name of the advancement under it.

## settings

`/advancementconfetti settings`, or through [mod menu](https://modrinth.com/mod/modmenu) if you have it.

you can turn normal and rare celebrations on and off separately:

```
/advancementconfetti toggle normal
/advancementconfetti toggle rare
```

`/advancementconfetti` on its own tells you what they're currently set to. saved in `config/advancementconfetti.txt`.

## building

needs jdk 25, minecraft 26.1.2 requires it.

```
./gradlew build
```

jar ends up in `build/libs`. grab the plain one, not the `-sources` one.

## license

CC BY-NC-SA 4.0 - use it, mod it, put it in your modpack, just credit me and link back, don't sell it, and keep the same license if you redistribute a modified version. full text in [LICENSE](LICENSE).
