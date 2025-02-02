# rscplus
*RuneScape Classic is made and owned by Jagex Ltd. and this project is not in any way affiliated with them.*

*The code is licensed under GPLv3 in hopes of protecting people from malicious modifications.*

## Download
The latest release can be found [here](https://github.com/RSCPlus/rscplus/releases/latest).

## Chat
You can reach us on #rscplus at irc.libera.chat, or join our Discord server ([Invite link](https://discord.gg/92gG87h))

## Changes since RSC went offline

- No longer contact RuneScape.com to get world total counts
- Toggleable buttons to control Replay playback
- Queued replay recordings
- Bank filter / sorting
- Many more!

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
- Fatigue on-screen alert
- Integrated Twitch chat into in-game chat
- Inventory free space counter
- Highlight friends and show their names when they're near you
- Notifications for PMs, trades, duel requests, low HP, and high fatigue
- Configurable item name patching
- Recording and playback of game sessions using the real data the server sent
- And more...

All features that modify the interface can be disabled if desired.

## Chat Commands
*::help* - displays all keyboard commands and chat commands in game

*::[skillname]* - Tells skill level and xp in chat

*::next_[skillname]* - Tells how much xp needed until next level in chat

*::total* - Shows total level and total xp in chat

*::fatigue* - Shows the fatigue percentage accurately in chat

*::screenshot* - Take screenshot (Saved in the screenshots directory)

*::banksearch [aWord]* - Searches current bank state for banked items 
containing "aWord". Note that withdrawing all of a certain item's type 
will push it to the end of the bank and thus withdrawing all should not 
be done if your bank is tidy. To exit the search mode, speak to the 
banker again.

*::togglestartsearchedbank [[aWord]]* - Toggle between searchable bank 
mode and normal mode; specifying the parameter updates the search 
keyword stored.

*::togglebypassattack* - Toggle bypass right click attack

*::toggleroofs* - Toggle roof hiding

*::togglecolor* - Toggle colored text in terminal

*::togglecombat* - Toggle combat experience menu

*::togglefatigue* - Toggle fatigue alert

*::debug* - Toggle debug mode

*::togglelogindetails* - Toggle IP/Dns login details shown at welcome screen

*::toggletwitch* - Toggle twitch chat hidden/shown

*::toggleiteminfo* - Toggle Item info

*::togglenpcinfo* - Toggle NPC info

*::toggleplayerinfo* - Toggle Player info

*::toggleowninfo* - Toggle own player info

*::togglehitbox* - Toggle hitboxes for NPC info

*::togglexpdrops* - Toggle XP drops

*::togglefatiguedrops* - Toggle fatigue drops

*::fov \<value\>* - Change FoV to specified value (range of 7 to 16)

*::logout* - Logout

*::togglefriendinfo* - Toggle Player info for Friends only

*::toggleposition* - Toggle player's position

*::toggleretrofps* - Toggle retro FPS overlay (Early RSC style)

*::toggleinvcount* - Toggle the overlay of current inventory used

*::togglebuffs* - Toggle combat (de)buffs and cooldowns display

*::toggledeathitems* - Toggle displaying which items are kept upon dying

*::togglestatusdisplay* - Toggle Hits/Prayer/Fatigue display

*::cmb* - WARNING! Because of color codes, this command will break the character limit and *may be bannable*. Don't send this command across PM to RS2/RS3 because they'll see a bunch of RS1 color codes and you may get banned if you're reported. Mimic's osbuddy's !cmb; outputs combat level (with decimal), Att, Str, Def, Hits, Ranged, Magic, and Prayer levels.

*::cmbnocolor* - Mimic's osbuddy's !cmb; doesn't use color codes and is within character limits

*::xmas* - Formats your message in festive Red Green and White colours

*::rainbow* - Formats your message in all the colours of the rainbow (or at least 6)

*::update* - Manually check if RSC+ is up to date

## Controls
- Middle mouse click - Rotate camera
- Mouse wheel scroll - Zoom camera

All keybinds are configurable via the Settings GUI

#### General
- Ctrl + l - Logout
- Ctrl + s - Take screenshot (Saved in the screenshots directory)
- Ctrl + o - Open the options/settings window
- Ctrl + c - Toggle combat experience menu
- Ctrl + [ - Toggle XP drops
- Ctrl + ] - Toggle fatigue drops
- Ctrl + f - Toggle fatigue alert
- Ctrl + v - Toggle inventory full alert
- Ctrl + a - Toggle bypass right click attack
- Ctrl + r - Toggle roof hiding
- Ctrl + q - Toggle start on searchable bank mode (uses previously 
stored keyword on searchable bank)
- Ctrl + z - Toggle colored chat text in terminal

#### Overlays
- Ctrl + u - Toggle Hits/Prayer/Fatigue display
- Ctrl + y - Toggle combat (de)buffs and cooldowns display
- Ctrl + e - Toggle inventory count overlay
- Alt + p - Toggle position
- Alt + f - Toggle retro FPS overlay
- Ctrl + i - Toggle Item name ovelay
- Ctrl + p - Toggle Player name overlay
- Unbound - Toggle Friend name overlay
- Ctrl + n - Toggle NPC name overlay
- Ctrl + h - Toggle hitboxes
- Ctrl + g - Toggle food heal overlay
- Unbound - Toggle time until health regen
- Ctrl + d - Toggle debug mode

#### Streaming & Privacy
- Ctrl + t - Toggle twitch chat hidden/shown
- Ctrl + j - Toggle IP/DNS shown at login screen

#### Replay (only used while a recording is played back)
- Ctrl + b - Stop playback
- Space - Pause playback
- Ctrl + Right - Increase playback speed
- Ctrl + Left - Decrease playback speed
- Ctrl + Down - Reset playback speed

#### Miscellaneous
- Ctrl + 1-5 - World switch on login screen

## Server Extensions
Custom features specific to private servers must be implemented via the Server Extension framework

See [Documentation](src/Client/Extensions/README.md) for more details

## Contributing
- Fork the project
- Work on it
- Pull request the branch

*No game automation features (macroing, etc.) will be accepted, even within private server extensions*

### Building
*You must have git, apache-ant, and jdk 1.7 or 1.8 installed to do this.*
```
git clone https://github.com/RSCPlus/rscplus
cd rscplus
ant dist
```

The result should be in the *dist* folder.

There is an Eclipse project in the source root you can import.

If you are running rsc+ in Eclipse and want colorized console output, you may want to use an extension called [ANSI Escape in Console](https://marketplace.eclipse.org/content/ansi-escape-console) and add -Djansi.passthrough=true to VM arguments for your run configuration.

If you are running rsc+ in IntelliJ IDEA and want colorized console output, you need to add -Djansi.passthrough=true to VM options for your run configuration.

If you are running rsc+ from console, and want to output console text with colorization to a log file, add -Djansi.passthrough=true to your java arguments.

### Contributors
Check [here](https://github.com/RSCPlus/rscplus/graphs/contributors) for contributors

Thanks to Warriorccc0 (Brinner) for testing various things on Windows.
