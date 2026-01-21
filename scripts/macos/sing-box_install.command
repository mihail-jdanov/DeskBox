#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

SOURCE="$SCRIPT_DIR/sing-box"
TARGET_DIR="$HOME/Library/Application Support/DeskBox"
TARGET="$TARGET_DIR/sing-box"

echo "📦 Installing sing-box"
echo "Source: $SOURCE"
echo "Target: $TARGET"
echo

if [[ ! -f "$SOURCE" ]]; then
  echo "❌ sing-box not found"
  exit 1
fi

mkdir -p "$TARGET_DIR"

echo "🔐 Requesting administrator privileges..."

sudo cp "$SOURCE" "$TARGET"
sudo chown root:wheel "$TARGET"
sudo chmod u+s "$TARGET"

echo
echo "✅ sing-box successfully installed!"