#!/bin/bash

set -e

APPIMAGE_VERSION="13"

umask 022

source binaries/.jdk-versions.sh
source binaries/build-scripts/common.sh

# Validate binary version formatting
validate_binary_version

# Download RSCPlus jar
download_rscplus_jar

# Set JAR permissions
chmod 644 rscplus.jar

# Download JRE
if ! [ -f linux64_jre.tar.gz ] ; then
  echo "Downloading JRE..."
  curl -Lo linux64_jre.tar.gz "$LINUX_AMD64_LINK"
fi

# packr requires a "jdk" and pulls the jre from it - so we have to place it inside the jdk folder at jre/
if ! [ -d linux-jdk ] ; then
  tar zxf linux64_jre.tar.gz
  mkdir linux-jdk
  mv jdk"$LINUX_AMD64_VERSION"-jre linux-jdk/jre
fi

# Download packr
download_packr

# Note: Host umask may have checked out this directory with g/o permissions blank
chmod -R u=rwX,go=rX binaries/linux/appimage

# Run packr
java -jar packr.jar \
  --platform linux64 \
  --jdk linux-jdk \
  --executable $PACKR_EXE \
  --classpath $PACKR_CLASSPATH \
  --mainclass $PACKR_MAIN_CLASS \
  --vmargs $PACKR_VM_ARGS \
  --output native-linux-x86_64/RSCPlus.AppDir/ \
  --resources binaries/linux/appimage/rscplus.desktop binaries/linux/appimage/rscplus.png

pushd native-linux-x86_64/RSCPlus.AppDir

# Symlink AppRun -> RSCPlus
ln -s RSCPlus AppRun

# Ensure RSCPlus is executable for all users
chmod 755 RSCPlus

popd

# Download appimagetool
if ! [ -f appimagetool-x86_64.AppImage ] ; then
  curl -Lo appimagetool-x86_64.AppImage \
    https://github.com/AppImage/AppImageKit/releases/download/$APPIMAGE_VERSION/appimagetool-x86_64.AppImage
  chmod +x appimagetool-x86_64.AppImage
fi

# Execute appimagetool
./appimagetool-x86_64.AppImage \
	native-linux-x86_64/RSCPlus.AppDir/ \
	RSCPlus.AppImage \
