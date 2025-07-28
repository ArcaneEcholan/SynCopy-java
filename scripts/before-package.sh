#!/bin/bash
set -e

# ======== md5sum all classes ========

TARGET_DIR="$1"
OUTPUT_DIR="$2"
PROJECT_NAME="$3"

TMP_HASHES=$(mktemp)
TMP_CONCAT=$(mktemp)

ls -l $TARGET_DIR

# Step 1: 对所有小于 1MB 的文件分别 md5sum，按文件路径排序
find "$TARGET_DIR" -type f -size -1048576c -print0 \
  | sort -z \
  | xargs -0 md5sum > "$TMP_HASHES"

cat $TMP_HASHES

# Step 2: 提取每一行的 hash 值（不含文件名），拼接成串
awk '{print $1}' "$TMP_HASHES" | tr -d '\n' > "$TMP_CONCAT"

# Step 3: 对拼接后的 md5 字符串再做一次 md5
MD5_FULL=$(md5sum "$TMP_CONCAT" | awk '{print $1}')
MD5_SHORT=${MD5_FULL:0:6}

# 清理
rm "$TMP_HASHES" "$TMP_CONCAT"

# Step 4: 写入 properties 文件
mkdir -p "$OUTPUT_DIR"
TARGET_FILE="${OUTPUT_DIR}/${PROJECT_NAME}.md5.properties"
echo "md5.short=${MD5_SHORT}" > "$TARGET_FILE"
echo "md5.long=${MD5_FULL}" >> "$TARGET_FILE"

echo "MD5 fingerprint written to $TARGET_FILE"
