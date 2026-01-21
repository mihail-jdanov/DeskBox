#!/usr/bin/env bash

set -e

# Директория, где лежит скрипт
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Ищем папку рядом со скриптом, начинающуюся с DeskBox
RELEASE_DIR=$(find "$SCRIPT_DIR" -maxdepth 1 -mindepth 1 -type d -name "DeskBox*" | head -n 1)

if [ -z "$RELEASE_DIR" ]; then
    echo "No folder starting with 'DeskBox' found next to the script!"
    exit 1
fi

RELEASE_NAME=$(basename "$RELEASE_DIR")
ARCHIVE_NAME="${RELEASE_NAME}.tar.gz"

echo "Creating archive $ARCHIVE_NAME from folder $RELEASE_NAME ..."

# Переходим в директорию скрипта, чтобы tar использовал относительные пути
cd "$SCRIPT_DIR"

# Создаём архив
tar czf "$ARCHIVE_NAME" \
    --mode=755 "$RELEASE_NAME/install.sh" \
    "$RELEASE_NAME/DeskBox"

echo "Archive $ARCHIVE_NAME created successfully."

echo "Press Enter to exit..."
read -r

