#!/bin/bash

set -e

source binaries/.jdk-versions.sh
source binaries/build-scripts/common.sh

# Validate binary version formatting
validate_binary_version

# Download RSCPlus jar
download_rscplus_jar

# Download JRE
if ! [ -f mac_aarch64_jre.tar.gz ] ; then
  echo "Downloading JRE..."
  curl -Lo mac_aarch64_jre.tar.gz "$MAC_AARCH64_LINK"
fi

# packr requires a "jdk" and pulls the jre from it - so we have to place it inside the jdk folder at jre/
if ! [ -d osx-aarch64-jdk ] ; then
  tar zxf mac_aarch64_jre.tar.gz
  mkdir osx-aarch64-jdk
  mv zulu"$MAC_AARCH64_VERSION"-macosx_aarch64/zulu-8.jre osx-aarch64-jdk/jre

  pushd osx-aarch64-jdk/jre
  # Move JRE out of Contents/Home/
  mv Contents/Home/* .
  # Remove unused leftover folders
  rm -rf Contents
  popd
fi

# Download packr
download_packr

# Run packr
java -jar packr.jar \
  --platform macaarch64 \
  --jdk osx-aarch64-jdk \
  --executable $PACKR_EXE \
  --classpath $PACKR_CLASSPATH \
  --mainclass $PACKR_MAIN_CLASS \
  --vmargs $PACKR_VM_ARGS \
  --icon binaries/osx/rscplus.icns \
  --output native-osx-aarch64/RSCPlus.app

cp binaries/osx/Info.plist native-osx-aarch64/RSCPlus.app/Contents

echo Setting world execute permissions on RSCPlus
pushd native-osx-aarch64/RSCPlus.app
chmod g+x,o+x Contents/MacOS/RSCPlus
popd

codesign -s - -a arm64 --entitlements binaries/osx/signing.entitlements --options runtime native-osx-aarch64/RSCPlus.app || true

# create-dmg exits with an error code due to no code signing, but is still okay
# note we use Adam-/create-dmg as upstream does not support UDBZ
create-dmg --format UDBZ native-osx-aarch64/RSCPlus.app native-osx-aarch64/ || true

mv native-osx-aarch64/RSCPlus\ *.dmg RSCPlus-aarch64.dmg

# Notarize app
# Note: This simply allows it to be built, there is no actual code signing
if xcrun notarytool submit RSCPlus-aarch64.dmg --wait --keychain-profile "AC_PASSWORD" ; then
  xcrun stapler staple RSCPlus-aarch64.dmg
fi
