#!/bin/bash

# Common functions and variables

echo "Using common build definitions..."

# Validate binary version formatting
validate_binary_version() {
  echo "Using binary version: [$BINARY_VERSION]"
  # Validate binary version formatting
  if [ ${#BINARY_VERSION} != 15 ] ||
     [ "${BINARY_VERSION:8:1}" != "." ]; then
    echo "Error: Invalid binary version provided"
    exit 1
  fi
}

# Download RSCPlus JAR
download_rscplus_jar() {
  if ! [ -f rscplus.jar ] ; then
    echo "Downloading RSCPlus JAR..."
    curl -LO https://github.com/RSCPlus/rscplus/releases/download/Latest/rscplus.jar

    # Ensure the downloaded jar wasn't junk (at least 100kb)
    if [ "$(wc -c <"rscplus.jar")" -le 100000 ]; then
      exit 1
    fi
  fi
}

# Download Packr JAR
download_packr() {
  if ! [ -f packr.jar ] ; then
    echo "Downloading packr JAR..."
    curl -LO https://github.com/RSCPlus/packr/releases/download/Archive/packr.jar
  fi
}

# Packr Variables
export PACKR_EXE="RSCPlus"
export PACKR_CLASSPATH="rscplus.jar"
export PACKR_MAIN_CLASS="Client.Launcher"
export PACKR_VM_ARGS="Xmx1500m DusingBinary=rscplus DbinaryVersion=$BINARY_VERSION"
