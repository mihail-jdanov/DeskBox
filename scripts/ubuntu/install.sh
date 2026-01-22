#!/usr/bin/env bash

set -e

# --- Variables ---
USER_HOME="$HOME"
INSTALL_DIR="$USER_HOME/.local/opt/DeskBox"
DESKTOP_FILE_DIR="$USER_HOME/.local/share/applications"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BIN_FILE="$INSTALL_DIR/sing-box"
DESKTOP_FILE="$DESKTOP_FILE_DIR/DeskBox.desktop"

echo "Installing DeskBox to $INSTALL_DIR..."
mkdir -p "$INSTALL_DIR"
rm -rf "$INSTALL_DIR/bin" "$INSTALL_DIR/lib"
cp -r "$SCRIPT_DIR/DeskBox/"* "$INSTALL_DIR"
echo "DeskBox has been installed."

echo "Creating .desktop file at $DESKTOP_FILE..."
mkdir -p "$DESKTOP_FILE_DIR"
cat > "$DESKTOP_FILE" <<EOF
[Desktop Entry]
Name=DeskBox
Exec=$INSTALL_DIR/bin/DeskBox %u
Path=$INSTALL_DIR/
Icon=$INSTALL_DIR/lib/DeskBox.png
Terminal=false
Categories=Network;
Type=Application
StartupNotify=false
StartupWMClass=org-mikhailzhdanov-deskbox-MainKt
MimeType=x-scheme-handler/sing-box;
X-GNOME-Autostart-enabled=true
EOF
echo ".desktop file created."

echo "Copying sing-box to $INSTALL_DIR..."
rm -f "$BIN_FILE"
cp "$SCRIPT_DIR/sing-box" "$BIN_FILE"
echo "sing-box has been copied."

echo "Enter your administrator password to give sing-box the necessary permissions..."
if sudo chown root:root "$BIN_FILE" && sudo chmod u+s "$BIN_FILE"; then
    echo "sing-box has been granted necessary permissions. Installation and setup of all components is complete."
else
    echo "Failed to grant permissions to sing-box. Please try installing again."
fi

echo "Press Enter to exit..."
read -r

