#!/bin/bash

set -e

source binaries/.jdk-versions.sh
source binaries/build-scripts/common.sh

# Validate binary version formatting
validate_binary_version

# Download RSCPlus jar
download_rscplus_jar

# Download JRE
if ! [ -f win64_jre.zip ] ; then
  curl -Lo win64_jre.zip "$WIN64_LINK"
fi

# packr requires a "jdk" and pulls the jre from it - so we have to place it inside the jdk folder at jre/
if ! [ -d win64-jdk ] ; then
  unzip win64_jre.zip
  mkdir win64-jdk
  mv jdk"$WIN64_VERSION"-jre win64-jdk/jre
fi

# Download packr
download_packr

# Run packr
java -jar packr.jar \
  --platform windows64 \
  --jdk win64-jdk \
  --executable $PACKR_EXE \
  --classpath $PACKR_CLASSPATH \
  --mainclass $PACKR_MAIN_CLASS \
  --vmargs $PACKR_VM_ARGS \
  --output native-win64 \
  --resources binaries/win/rscplus_console.ico binaries/win/rscplus.ico

# Update the exe
binaries/win/tools/rcedit-x64 native-win64/RSCPlus.exe \
  --application-manifest binaries/win/rscplus.manifest \
  --set-icon binaries/win/rscplus.ico

# Run innosetup
iscc binaries/win/innosetup/rscplus.iss