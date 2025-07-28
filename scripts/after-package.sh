#!/bin/bash
set -e

OUTPUT_DIR="$1"
PROJECT_NAME="$2"
JAR_PATH="$3"

# ======== read md5 file generated in "before-package" phase ========
PROPS_FILE="${OUTPUT_DIR}/${PROJECT_NAME}.md5.properties"
if [ ! -f "$PROPS_FILE" ]; then
  echo "Properties file not found: $PROPS_FILE"
  exit 1
fi
# concat md5.short to the final jar name
MD5_SHORT=$(grep '^md5\.short=' "$PROPS_FILE" | cut -d'=' -f2)
EXT="${JAR_PATH##*.}"
BASE="${JAR_PATH%.*}"
NEW_NAME="${BASE}-${MD5_SHORT}.${EXT}"
cp "$JAR_PATH" "$JAR_PATH.copy"
mv "$JAR_PATH" "$NEW_NAME"
mv "$JAR_PATH.copy" "$JAR_PATH"
echo "Renamed $JAR_PATH → $NEW_NAME"

# ======== delete temp md5 file to avoid being involved into next md5 calculation ========
TARGET_FILE="${OUTPUT_DIR}/${PROJECT_NAME}.md5.properties"
if [ -f "$TARGET_FILE" ]; then
  rm "$TARGET_FILE"
  echo "Removed $TARGET_FILE"
else
  echo "No $TARGET_FILE to remove"
fi
