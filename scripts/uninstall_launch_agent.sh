#!/usr/bin/env bash
set -e
LAUNCH_AGENT="$HOME/Library/LaunchAgents/com.example.weatherwidget.plist"
if [ -f "$LAUNCH_AGENT" ]; then
  launchctl unload "$LAUNCH_AGENT" 2>/dev/null || true
  rm "$LAUNCH_AGENT"
  echo "Uninstalled $LAUNCH_AGENT"
else
  echo "No launch agent found at $LAUNCH_AGENT"
fi