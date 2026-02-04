# WeatherWidget (Kotlin + Compose for Desktop)

Um widget simples que mostra o clima em tempo real usando OpenWeatherMap.

## Requisitos
- JDK 11 ou superior
- `OPENWEATHER_API_KEY` exportada no ambiente (obtenha em https://openweathermap.org)

## Variáveis de ambiente
- `OPENWEATHER_API_KEY` (obrigatória)
- `WEATHER_CITY` (opcional; default: São Paulo)
- `WEATHER_UPDATE_INTERVAL` (opcional; segundos; default: 60)

## Rodando em modo desenvolvimento
1. Configure a API key:

```bash
export OPENWEATHER_API_KEY="sua_api_key"
export WEATHER_CITY="São Paulo"
```

2. Execute:

```bash
./gradlew run
```

## UI e configurações
Clique no botão ⚙ para abrir as configurações e alterar a cidade ou o intervalo de atualização. As configurações são salvas automaticamente e persistem entre execuções.

## Autostart (macOS)

Após empacotar o app e movê-lo para `/Applications`, você pode instalar um LaunchAgent para iniciar o app no login:

```bash
./scripts/install_launch_agent.sh /Applications/WeatherWidget.app
```

Para remover o LaunchAgent:

```bash
./scripts/uninstall_launch_agent.sh
```

Também há um template em `launch-agent/com.example.weatherwidget.plist.template` que você pode editar substituindo `{{APP_PATH}}`.

## Empacotar para macOS (.app)

```bash
./gradlew packageDistributions
```

O `.app` estará disponível em `build/compose/bundles/`.

## Observações
- A janela é pequena, sem decoração e fica sempre-on-top.
- Substitua o ícone placeholder em `src/main/resources/icon.png` por um ícone real.
