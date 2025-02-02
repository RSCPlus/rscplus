# RSCPlus Binaries

Builds are provided for the following systems:

| Operating System | Architecture(s)      | Format           |
|------------------|----------------------|------------------|
| Windows          | x64, x86<sup>†</sup> | Installer wizard |
| MacOS            | ARM, x64             | DMG              |
| Linux            | x64, x86             | AppImage         |

_<sup>†</sup> Compatible with Windows XP_

### Building

Binary generation is performed via the "Build Binaries" GitHub action.

The provided drop-down allows for the selection of various system types. A release version must be provided in the
format of _yyyyMMdd.HHmmss_.

Upon successful completion of the workflow, a ZIP file containing all produced artifacts is then uploaded to the
"binaries-pre-release" tag.

### Releasing

For compatibility with the RSCPlus update system, all downloadable binaries must reside alongside their respective
versioning files.

When a new version is published, users who have elected to accept software updates will be prompted to update their
application.

With the exception of Linux AppImages, regular RSCPlus client updates do not require the release of new binaries, as the
underlying JAR is simply replaced on the file system. Because AppImages are read-only by design, a new version must be
made available with every client update in order for those users to receive updates.

**Note**: Because RSCPlus client updates may occur out-of-band with application updates, brand-new binary users will be
met with an immediate client update upon installation, if one is available.
