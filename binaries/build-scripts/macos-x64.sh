#!/bin/bash

set -e

SIGNING_IDENTITY="Developer ID Application"

source binaries/.jdk-versions.sh
source binaries/build-scripts/common.sh

# Validate binary version formatting
validate_binary_version

# Download RSCPlus jar
download_rscplus_jar

# Download JRE
if ! [ -f mac64_jre.tar.gz ] ; then
  echo "Downloading JRE..."
  curl -Lo mac64_jre.tar.gz "$MAC_AMD64_LINK"
fi

# packr requires a "jdk" and pulls the jre from it - so we have to place it inside the jdk folder at jre/
if ! [ -d osx-jdk ] ; then
  tar zxf mac64_jre.tar.gz
  mkdir osx-jdk
  mv zulu"$MAC_AMD64_VERSION"-macosx_x64/zulu-8.jre osx-jdk/jre

  pushd osx-jdk/jre
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
  --platform mac64 \
  --jdk osx-jdk \
  --executable $PACKR_EXE \
  --classpath $PACKR_CLASSPATH \
  --mainclass $PACKR_MAIN_CLASS \
  --vmargs $PACKR_VM_ARGS \
  --icon binaries/osx/rscplus.icns \
  --output native-osx/RSCPlus.app

cp binaries/osx/Info.plist native-osx/RSCPlus.app/Contents

echo Setting world execute permissions on RSCPlus
pushd native-osx/RSCPlus.app
chmod g+x,o+x Contents/MacOS/RSCPlus
popd

codesign -f -s "${SIGNING_IDENTITY}" --entitlements binaries/osx/signing.entitlements --options runtime native-osx/RSCPlus.app || true

# create-dmg exits with an error code due to no code signing, but is still okay
# note we use Adam-/create-dmg as upstream does not support UDBZ
create-dmg --format UDBZ native-osx/RSCPlus.app native-osx/ || true

mv native-osx/RSCPlus\ *.dmg RSCPlus-x64.dmg

if ! hdiutil imageinfo RSCPlus-x64.dmg | grep -q "Format: UDBZ" ; then
  echo "Format of resulting dmg was not UDBZ, make sure your create-dmg has support for --format"
  exit 1
fi

# Notarize app
# Note: This simply allows it to be built, there is no actual code signing
if xcrun notarytool submit RSCPlus-x64.dmg --wait --keychain-profile "AC_PASSWORD" ; then
  xcrun stapler staple RSCPlus-x64.dmg
fi
