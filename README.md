# rscplus

*RuneScape Classic is made and owned by Jagex Ltd. and this project is not in any way affiliated with them.*

*There is no gaurantee this client mod will not get you banned, use at your own risk.*

*The code is licensed under GPLv3 in hopes of protecting people from malicious modifications.*

I'm glad I started this project. Thanks everybody that had nice things to say about it, I enjoy reading them.

## Contributing

- Fork the project
- Create a branch in your fork
- Work on it
- Make sure the branch is up-to-date with master
- Pull request the branch

*I will not accept any automation requests.*

## Features
- Game resolution changing (about 80% complete)
- NPC Dialogue options with 1-9 on keyboard
- Roof hiding
- Option to always show combat style menu, and persistent combat style setting
- XP Drops
- Screenshots
- Mouse wheel zoom
- Middle mouse camera rotation
- HP, Prayer, and fatigue bars on the bottom right of the screen
- Fatigue on-screen alert at 100%
- Integrated Twitch chat into in-game chat
- Inventory free space counter
- Highlight friends and show their names when they're near you
- And more...

## Controls
*[Command Key]* can be either Alt or Ctrl.

- Middle mouse click - Rotate camera
- Mouse wheel scroll - Zoom camera
- *[Command Key]* + s - Take screenshot (Saved in the screenshots directory)
- *[Command Key]* + r - Toggle roof hiding
- *[Command Key]* + c - Toggle combat experience menu
- *[Command Key]* + f - Toggle fatigue alert at 100%
- *[Command Key]* + d - Toggle debug mode
- *[Command Key]* + t - Toggle twitch chat hidden/shown
- *[Command Key]* + i - Toggle Item info
- *[Command Key]* + n - Toggle NPC info
- *[Command Key]* + p - Toggle Player info
- *[Command Key]* + h - Toggle hitboxes for NPC info
- *[Command Key]* + 1-5 - World switch on login screen

## Chat Commands

*::[skillname]* - Tells skill level and xp in chat

*::total* - Shows total level and total xp in chat

*::screenshot* - Take screenshot (Saved in the screenshots directory)

*::toggleroofs* - Toggle roof hiding

*::togglecombat* - Toggle combat experience menu

*::togglefatigue* - Toggle fatigue alert at 100%

*::debug* - Toggle debug mode

*::toggletwitch* - Toggle twitch chat hidden/shown

*::toggleiteminfo* - Toggle Item info

*::togglenpcinfo* - Toggle NPC info

*::toggleplayerinfo* - Toggle Player info

*::togglehitbox* - Toggle hitboxes for NPC info

*::fov* - Change FoV from 8-9

## Building [![Build Status](https://travis-ci.org/OrN/rscplus.svg?branch=master)](https://travis-ci.org/OrN/rscplus)

*You must have git, apache-ant, and jdk 1.7+ installed to do this.*
```
git clone https://github.com/OrN/rscplus
cd rscplus
ant dist
```

The result should be in the *bin* folder.

There is also an Eclipse project in the source root you can import.

## Download
The latest release can be found [here](https://github.com/OrN/rscplus/releases/latest) (rscplus.jar).
