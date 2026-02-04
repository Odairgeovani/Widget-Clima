#!/usr/bin/env bash
set -euo pipefail

# Usage:
#  ./run.sh <OPENWEATHER_API_KEY>
# or
#  export OPENWEATHER_API_KEY="..." && ./run.sh

APP_PATH="./build/compose/binaries/main/app/WeatherWidget.app"

if [ ! -d "$APP_PATH" ]; then
  echo "Erro: app não encontrado em $APP_PATH. Execute 'gradle createDistributable' primeiro."
  exit 1
fi

API_KEY="${1:-${OPENWEATHER_API_KEY:-}}"
if [ -z "$API_KEY" ]; then
  echo "Uso: $0 <OPENWEATHER_API_KEY>" >&2
  echo "Ou exporte OPENWEATHER_API_KEY no seu ambiente e execute ./run.sh" >&2
  exit 2
fi

# Set environment for GUI apps and open the .app
echo "Configurando OPENWEATHER_API_KEY e abrindo WeatherWidget.app (a chave não será exibida por completo)"
# hide most of the key when printing
echo "OPENWEATHER_API_KEY=${API_KEY:0:6}..."

# Export for GUI processes
launchctl setenv OPENWEATHER_API_KEY "$API_KEY"

# Open the .app
open "$APP_PATH"

# Note: to unset the env for GUI sessions later:
# launchctl unsetenv OPENWEATHER_API_KEY
