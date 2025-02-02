#!/bin/bash

set -e

source binaries/.jdk-versions.sh
source binaries/build-scripts/common.sh

# Validate binary version formatting
validate_binary_version

# Download RSCPlus jar
download_rscplus_jar

# Download JRE
if ! [ -f win32_jdk.zip ] ; then
  curl -Lo win32_jdk.zip "$WIN32_LINK"
fi

mkdir native-win32

# Extract JRE
echo "Unzipping Windows XP compatible JRE"
unzip win32_jdk.zip
mv java-1.8.0-openjdk-1.8.0.171-1.b10.ojdkbuild.windows.x86/jre native-win32

# Copy files needed for Launch4J execution
echo "Copying files for exe creation"
cp rscplus.jar native-win32
cp binaries/win/winxp.xml native-win32
cp binaries/win/winxp_console.xml native-win32
cp binaries/win/rscplus.ico native-win32
cp binaries/win/rscplus_console.ico native-win32

# Download Launch4J
if ! [ -f launch4j-3.14-win32.zip ] ; then
  echo "Downloading launch4j 3.14"
  curl -Lo launch4j-3.14-win32.zip https://github.com/RSCPlus/ojdkbuild/releases/download/Archive/launch4j-3.14-win32.zip
fi
unzip launch4j-3.14-win32.zip

# Template in the binary version to the launch4J XMLs
sed -i -e "s/BIN_VER/$BINARY_VERSION/g" native-win32/winxp.xml
sed -i -e "s/BIN_VER/$BINARY_VERSION/g" native-win32/winxp_console.xml

# Run launch4j
echo "Executing launch4j"
java -jar launch4j/launch4j.jar native-win32/winxp.xml

# Run launch4j (console)
echo "Executing launch4j (console)"
java -jar launch4j/launch4j.jar native-win32/winxp_console.xml

# Run innosetup
iscc binaries/win/innosetup/rscplus32.iss
