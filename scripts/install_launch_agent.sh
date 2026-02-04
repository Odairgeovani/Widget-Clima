#!/usr/bin/env bash
set -e

if [ -z "$1" ]; then
  echo "Usage: $0 /path/to/WeatherWidget.app"
  exit 1
fi

APP_PATH="$1"
LAUNCH_AGENT="$HOME/Library/LaunchAgents/com.example.weatherwidget.plist"

cat > "$LAUNCH_AGENT" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>Label</key>
  <string>com.example.weatherwidget</string>
  <key>ProgramArguments</key>
  <array>
    <string>$APP_PATH/Contents/MacOS/WeatherWidget</string>
  </array>
  <key>RunAtLoad</key>
  <true/>
</dict>
</plist>
EOF

# Unload if already loaded
launchctl unload "$LAUNCH_AGENT" 2>/dev/null || true
# Load the new agent
launchctl load -w "$LAUNCH_AGENT"

echo "Installed and loaded $LAUNCH_AGENT"