# rscplus

*RuneScape Classic is made and owned by Jagex Ltd. and this project is not in any way affiliated with them.*

*There is no guarantee this client mod will not get you banned, use at your own risk.*

*The code is licensed under GPLv3 in hopes of protecting people from malicious modifications.*

## Contributing

- Fork the project
- Work on it
- Pull request the branch

*I will not accept any game automation features (macroing, etc.)*

## Discord

Join our Discord server ([Invite link](https://discord.gg/92gG87h))

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
- Notifications for PMs, trades, duel requests, low HP, and high fatigue
- Configurable item name patching
- And more...

## Controls
Most keybinds are configurable via the Settings GUI

- Middle mouse click - Rotate camera
- Mouse wheel scroll - Zoom camera
- Ctrl + o - Open the options/settings window
- Ctrl + s - Take screenshot (Saved in the screenshots directory)
- Ctrl + x - Sleep, provided you have a sleeping bag in inventory
- Ctrl + r - Toggle roof hiding
- Ctrl + c - Toggle combat experience menu
- Ctrl + f - Toggle fatigue alert at 100%
- Ctrl + v - Toggle inventory full alert
- Ctrl + e - Toggle inventory count overlay
- Ctrl + u - Toggle Hits/Prayer/Fatigue display
- Ctrl + g - Toggle Friend info
- Ctrl + d - Toggle debug mode
- Ctrl + t - Toggle twitch chat hidden/shown
- Ctrl + i - Toggle Item info
- Ctrl + n - Toggle NPC info
- Ctrl + p - Toggle Player info
- Ctrl + h - Toggle hitboxes for NPC info
- Ctrl + q - Toggle start on searchable bank mode (uses previously 
stored keyword on searchable bank)
- Ctrl + [ - Toggle XP drops
- Ctrl + ] - Toggle fatigue drops
- Ctrl + l - Logout
- Ctrl + z - Toggle colored chat text in terminal
- Ctrl + 1-5 - World switch on login screen

## Chat Commands

*::help* - displays all keyboard commands and chat commands in game

*::[skillname]* - Tells skill level and xp in chat

*::next_[skillname]* - Tells how much xp needed until next level in chat

*::total* - Shows total level and total xp in chat

*::fatigue* - Shows the fatigue percentage accurately in chat

*::screenshot* - Take screenshot (Saved in the screenshots directory)

*::sleep* - Sleep, provided you have a sleeping bag in inventory

*::banksearch [aWord]* - Searches current bank state for banked items 
containing "aWord". Note that withdrawing all of a certain item's type 
will push it to the end of the bank and thus withdrawing all should not 
be done if your bank is tidy. To exit the search mode, speak to the 
banker again.

*::togglestartsearchedbank <aWord>* - Toggle between searchable bank 
mode and normal mode; specifying the parameter updates the search 
keyword stored.

*::toggleroofs* - Toggle roof hiding

*::togglecolor* - Toggle colored text in terminal

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

*::fov \<value\>* - Change FoV to specified value (range of 7 to 16)

*::logout* - Logout

*::togglefriendinfo* - Toggle Player info for Friends only

*::toggleinvcount* - Toggle the overlay of current inventory used

*::togglestatusdisplay* - Toggle Hits/Prayer/Fatigue display

*::cmb* - WARNING! Because of color codes, this command will break the character limit and *may be bannable*. Don't send this command across PM to RS2/RS3 because they'll see a bunch of RS1 color codes and you may get banned if you're reported. Mimic's osbuddy's !cmb; outputs combat level (with decimal), Att, Str, Def, Hits, Ranged, Magic, and Prayer levels.

*::cmbnocolor* - Mimic's osbuddy's !cmb; doesn't use color codes and is within character limits

*::xmas* - Formats your message in festive Red Green and White colours

*::rainbow* - Formats your message in all the colours of the rainbow (or at least 6)

*::system* - Hides your username and outputs your message

*::update* - Manually check if RSC+ is up to date

## Config Options

### User configured via the settings GUI or config.ini only
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
  <dd>Your Twitch channel name</dd>

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
  
### Automatically configured via user input in game or the settings GUI
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
  
  <dt>custom_client_size</dt>
  <dd>Range: <i>true or false</i><br>
  Enables resizing the client on startup to the specified size. When set with the settings GUI, it will resize the game immediately.
  </dd>
  
  <dt>custom_client_size_x</dt>
  <dd>Min: <i>512</i><br>
  Default width of client at startup
  </dd>
  
  <dt>custom_client_size_y</dt>
  <dd>Min: <i>346</i><br>
  Default height of client at startup
  </dd>
  
  <dt>debug</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether debug mode is enabled</dd>
  
  <dt>duel_notifications</dt>
  <dd>Range: <i>true or false</i><br>
  Sets if notification is shown when receiving a duel request</dd>
  
  <dt>fatigue_alert</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether a large notice is displayed when fatigue approaches 100%</dd>
  
  <dt>fatigue_notifications</dt>
  <dd>Range: <i>true or false</i><br>
  Sets if notification is shown when fatigue gets over a specified value</dd>
  
  <dt>fatigue_notif_value</dt>
  <dd>Range: <i>1-99</i><br>
  Sets fatigue value when a high fatigue notification is triggered</dd>
  
  <dt>hide_roofs</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether rooftops are hidden</dd>
  
  <dt>inventory_full_alert</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether a large notice is displayed when the inventory is full</dd>
  
  <dt>logout_notifications</dt>
  <dd>Range: <i>true or false</i><br>
  Sets if notification is shown when about to log out from not moving</dd>
  
  <dt>low_hp_notifications</dt>
  <dd>Range: <i>true or false</i><br>
  Sets if notification is shown when players HP is below a specified percent</dd>
  
  <dt>low_hp_notif_value</dt>
  <dd>Range: <i>1-99</i><br>
  Sets HP percent value when a low HP notification is triggered</dd>
  
  <dt>notification_sounds</dt>
  <dd>Range: <i>true or false</i><br>
  Sets if a sound is played when a notification is triggered</dd>
  
  <dt>pm_notifications</dt>
  <dd>Range: <i>true or false</i><br>
  Sets if notification is shown when receiving a PM</dd>
  
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

  <dt>start_searched_bank</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether bank should start on search mode</dd>

  <dt>search_bank_word</dt>
  <dd>Range: <i>Any non-empty string</i><br>
  Sets the word that will be used when starting bank search 
mode</dd>

  <dt>highlight_list</dt>
  <dd>Range: <i>Any non-empty sequence of comma separated strings</i><br>
  Sets the list of items that will be highlighted on the ground when `show_iteminfo` is enabled</dd>

  <dt>block_list</dt>
  <dd>Range: <i>Any non-empty sequence of comma separated strings</i><br>
  Sets the list of items that will be not shown on the ground when `show_iteminfo` is enabled</dd>
  
  <dt>trade_notifications</dt>
  <dd>Range: <i>true or false</i><br>
  Sets if notification is shown when receiving a trade request</dd>
  
  <dt>tray_notifs</dt>
  <dd>Range: <i>true or false</i><br>
  Enables or disables <i>all</i> notifications</dd>
  
  <dt>tray_notifs_always</dt>
  <dd>Range: <i>true or false</i><br>
  If false, notifications will only trigger when the client window is focused. If true, notifications will always trigger, regardless of client window focus.</dd>
  
  <dt>twitch_hide</dt>
  <dd>Range: <i>true or false</i><br>
  Sets whether Twitch chat is shown in game chat</dd>
  
  <dt>use_system_notifications</dt>
  <dd>Range: <i>true or false</i><br>
  Sets if system notifications are used instead of custom notifications. Set to false if your system doesn't support notifications or if you don't like the appearance of your system's notifications</dd>
  
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

Before submitting a pull request, please update the version number using ant:
```
ant setversion
```

There is also an Eclipse project in the source root you can import.

## Contributors
Check [here](https://github.com/OrN/rscplus/graphs/contributors)

Thanks to Warriorccc0 (Brinner) for testing various things on Windows.

## Download
The latest release can be found [here](https://github.com/OrN/rscplus/releases/latest) (rscplus.jar).
