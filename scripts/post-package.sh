#!/bin/bash
set -e

JAR="$1"
DIR="$(dirname "$JAR")"
BASENAME="$(basename "$JAR" .jar)"

# 计算 MD5
MD5_FULL=$(md5sum "$JAR" | awk '{print $1}')
MD5_PREFIX=${MD5_FULL:0:6}

# 构造目标文件名
COPY="$DIR/${BASENAME}-${MD5_PREFIX}.jar"
MD5_FILE="${COPY}.md5"

# 复制并生成 md5 文件
cp "$JAR" "$COPY"
echo "$MD5_FULL  $(basename "$COPY")" > "$MD5_FILE"

echo "Copied to $COPY"
echo "MD5 written to $MD5_FILE"
