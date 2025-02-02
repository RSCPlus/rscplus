[Setup]
AppName=RSCPlus
AppPublisher=RSCPlus
DisableWelcomePage=No
DisableDirPage=Yes
DisableProgramGroupPage=Yes
UninstallDisplayName=RSCPlus
; TODO: Bump when updating the Windows binary
AppVersion=1.0.0
AppSupportURL=https://rsc.plus/
DefaultDirName={userappdata}\RSCPlus
MinVersion=6.1

; ~15 mb headroom
ExtraDiskSpaceRequired=15000000
ArchitecturesAllowed=x64
PrivilegesRequired=lowest

WizardImageFile=wizard.bmp
WizardSmallImageFile=wizard_small.bmp
SetupIconFile=..\rscplus.ico
UninstallDisplayIcon={app}\RSCPlus.exe

Compression=lzma2
SolidCompression=yes

OutputDir=..\..\..\
OutputBaseFilename=RSCPlusSetup

[Tasks]
Name: DesktopIcon; Description: "Create a &desktop icon";
Name: DesktopIconConsole; Description: "Create a &desktop icon for the console launcher";

[Files]
Source: "..\..\..\native-win64\RSCPlus.exe"; DestDir: "{app}"
Source: "..\..\..\native-win64\rscplus.ico"; DestDir: "{app}"
Source: "..\..\..\native-win64\rscplus_console.ico"; DestDir: "{app}"
Source: "..\..\..\native-win64\rscplus.jar"; DestDir: "{app}"
Source: "..\..\..\native-win64\config.json"; DestDir: "{app}"
Source: "..\..\..\native-win64\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs

[Icons]
; start menu
Name: "{userprograms}\RSCPlus\RSCPlus"; Filename: "{app}\RSCPlus.exe"; IconFileName: "{app}\rscplus.ico"
Name: "{userprograms}\RSCPlus\RSCPlus (console)"; Filename: "{cmd}"; IconFilename: "{app}\rscplus_console.ico"; Parameters: "/c ""{app}\RSCPlus.exe"""
; Desktop Shortcuts
Name: "{userdesktop}\RSCPlus"; Filename: "{app}\RSCPlus.exe"; IconFileName: "{app}\rscplus.ico"; Tasks: DesktopIcon
Name: "{userdesktop}\RSCPlus (console)"; Filename: "{cmd}"; Parameters: "/c ""{app}\RSCPlus.exe"""; IconFilename: "{app}\rscplus_console.ico"; Tasks: DesktopIconConsole

[Run]
Filename: "{app}\RSCPlus.exe"; Description: "&Open RSCPlus"; Flags: postinstall skipifsilent nowait

[InstallDelete]
; Delete the old jvm so it doesn't try to load old stuff with the new vm and crash
Type: filesandordirs; Name: "{app}\jre"
; previous shortcut
Type: files; Name: "{userprograms}\RSCPlus.lnk"

[UninstallDelete]
Type: filesandordirs; Name: "{app}\RSCPlus.exe"
Type: filesandordirs; Name: "{app}\rscplus.ico"
Type: filesandordirs; Name: "{app}\rscplus_console.ico"
Type: filesandordirs; Name: "{app}\rscplus.jar"
Type: filesandordirs; Name: "{app}\config.json"
Type: filesandordirs; Name: "{app}\jre"
; the lib directory is created by rscplus.jar
Type: filesandordirs; Name: "{app}\lib"
