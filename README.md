# rscplus

*RuneScape Classic is made and owned by Jagex Ltd. and this project is not in any way affiliated with them.*

*There is no guarantee this client mod will not get you banned, use at your own risk.*

*The code is licensed under GPLv3 in hopes of protecting people from malicious modifications.*

## Contributing

- Fork the project
- Create a branch in your fork
- Work on it
- Make sure the branch is up-to-date with master
- Pull request the branch

*I will not accept any game automation features (macroing, etc.)*

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
- Configurable item name patching
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
- *[Command Key]* + [ - Toggle XP drops
- *[Command Key]* + ] - Toggle fatigue drops
- *[Command Key]* + l - Logout
- *[Command Key]* + 1-5 - World switch on login screen

## Chat Commands

*::[skillname]* - Tells skill level and xp in chat

*::next_[skillname]* - Tells how much xp needed until next level in chat

*::total* - Shows total level and total xp in chat

*::fatigue* - Shows the fatigue percentage accurately in chat

*::screenshot* - Take screenshot (Saved in the screenshots directory)

*::toggleroofs* - Toggle roof hiding

*::togglecombat* - Toggle combat experience menu

*::togglefatigue* - Toggle fatigue alert at 100%

*::debug* - Toggle debug mode

*::togglelogindetails* - Toggle IP/Dns login details shown at welcome screen

*::toggletwitch* - Toggle twitch chat hidden/shown

*::toggleiteminfo* - Toggle Item info

*::togglenpcinfo* - Toggle NPC info

*::toggleplayerinfo* - Toggle Player info

*::togglehitbox* - Toggle hitboxes for NPC info

*::togglexpdrops* - Toggle XP drops

*::togglefatiguedrops* - Toggle fatigue drops

*::fov* - Change FoV from 8-9

*::logout* - Logout

## Config Options

### User configured via config.ini only
<dl>
  <dt>name_patch_type</dt>
  <dd>Range: <i>0 to 3</i><br>
  Sets how item names are patched:<br>
  <ol start=0>
    <li>No item name patching</li>
    <li>Purely practical name changes (potion dosages, unidentified herbs, unfinished potions)</li>
    <li>Capitalizations and fixed spellings on top of type 1 changes</li>
    <li>Reworded vague stuff to be more descriptive on top of type 1 & 2 changes</li>
  </ol></dd>
  </dd>
  
  <dt>save_logininfo</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether login information is retained between logins during the same session</dd>
  
  <dt>software_cursor</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether a custom cursor overrides the default cursor</dd>
  
  <dt>twitch_channel</dt>
  <dd>Your Twitch name as you want it to appear in game chat</dd>

  <dt>twitch_oauth</dt>
  <dd>Your Twitch OAuth token (not your Stream Key)</dd>
  
  <dt>twitch_username</dt>
  <dd>Your Twitch username</dd>
  
  <dt>view_distance</dt>
  <dd>Range: <i>2,300 to 10,000</i><br>
  Sets max render distance of structures and landscape</dd>
  
  <dt>fatigue_figures</dt>
  <dd>Range: <i>1 to 7</i><br>
  Sets max number of digits to display after the decimal point on fatigue drops</dd>
</dl>
  
### Automatically configured via user input in game
<dl>
  <dt>combat_menu</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether the combat style choices are displayed outside of combat</dd>
  
  <dt>combat_style</dt>
  <dd>Range: <i>0 to 3</i><br>
  Sets combat attack style<br>
  <ol start=0>
    <li>Controlled</li>
    <li>Aggressive</li>
    <li>Accurate</li>
    <li>Defensive</li>
  </ol></dd>
  
  <dt>debug</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether debug mode is enabled</dd>
  
  <dt>fatigue_alert</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether a large notice is displayed when fatigue approaches 100%</dd>
  
  <dt>hide_roofs</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether rooftops are hidden</dd>
  
  <dt>show_hitbox</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether the clickable areas on NPCs and players are visible</dd>
  
  <dt>show_iteminfo</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether item names appear over dropped items</dd>
  
  <dt>show_npcinfo</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether NPC names appear over NPCs</dd>
  
  <dt>show_playerinfo</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether player display names appear over players; does not affect players on your friends list</dd>
    
  <dt>show_logindetails</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether login details such as IP or DNS will appear at the login screen</dd>

  <dt>show_xpdrops</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether XP drops appear on screen</dd>
  
  <dt>show_fatiguedrops</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether fatigue drops appear on screen</dd>
  
  <dt>twitch_hide</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether Twitch chat is shown in game chat</dd>
  
  <dt>world</dt>
  <dd>Range: <i>1 to 5</i><br>
  Sets current world to login to</dd>
</dl>

## Building [![Build Status](https://travis-ci.org/OrN/rscplus.svg?branch=master)](https://travis-ci.org/OrN/rscplus)

*You must have git, apache-ant, and jdk 1.7+ installed to do this.*
```
git clone https://github.com/OrN/rscplus
cd rscplus
ant dist
```

The result should be in the *bin* folder.

There is also an Eclipse project in the source root you can import.

## Contributors
Check [here](https://github.com/OrN/rscplus/graphs/contributors)

Thanks to Warriorccc0 (Brinner) for testing various things on Windows.

## Download
The latest release can be found [here](https://github.com/OrN/rscplus/releases/latest) (rscplus.jar).
