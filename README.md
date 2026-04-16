# SuperBus

SuperBus (temporary name) is a mobile app designed to be a better alternative to the official "Ginko Mobilités" app. It provides real-time waiting times for bus and tram services.

[![Screenshots of SuperBus](./wiki/preview.png)](./wiki/preview.png)

**[WARNING] This project is still under development and many bugs remain unresolved. As this is a proof-of-concept project, the final result may be entirely different.**

## Features
- ⏲️ **Display waiting times**:
    - 🔍 with larger font size (easily readable)
    - ↕️ full-screen view (swipe cards)
    - 🖼️ landscape and tablet support
    - 💡 option to keep the screen on
    - 🌙 dark mode support
- 📣 **Text-to-speech countdown**: announcement of waiting times (inspired by the [PANAM/SIEL screens of the Paris metro](https://youtu.be/M3j0xNkYBy0?si=6MJ926puqFzxaYgx&t=5))
    - 1️⃣ for a single transit line
    - *️⃣ or for several lines simultaneously (with auto-swiping cards in full-screen mode)
    - 💤 countdown disabled in background
- ❤️ **Favorites with preview**: favorites page with an overview of bus/tram lines
    - 🛠️ editable grid: wobbly tiles like iOS
    - ✏️ rename favorite items
- 🚲 **Ginko Vélocité included**

[![Sketches and concepts for SuperBus](./wiki/sketches.png)](./wiki/sketches.png)

## Upcoming features

Here is a list of features I would like to add:

- 🚧 Traffic alerts
- ⚙️ Settings page
- 🗂️ Tabbed and/or split view
- 🕐 History support: clear history, private browsing
- 🌍 Internationalisation: English, Spanish
- 👥 Crowd level on buses/trams
- 🚌 Vehicle info display (air conditioning, accessible ramp, USB ports, etc.)
- 🔊 Background text-to-speech countdown
- 💾 Favorites backup: import/export data
- 🚲 Extended support: Mobigo, SNCF, Citiz
- 🗺️ Map view with nearby station detection

[![Drawing the tram of Ginko Besançon with Inkscape](./wiki/making-tramway-tile.gif)](./wiki/inkscape.png)

## Build this app

### Environment

- **Android Studio** : (works fine with v.2025.3.3, may work with older versions)
- **Java** : 11

### Variables

This project need an API key to interact with Ginko API and JCDecaux API.

1. **Ginko Mobilités**: You have to [ask Ginko](https://api.ginko.voyage/#prez) in order to receive your key. You can get a temporary key if you don't want to ask.

2. **Ginko Vélocité (JCDecaux)**: Go to [JCDecaux Developer](https://developer.jcdecaux.com/), then create an account to get your API key.

Then, edit `local.properties` to add these lines :
```ini
apikey.ginko_mobilites=PUT_YOUR_GINKO_API_KEY_RIGHT_THERE
apikey.ginko_velocite=PUT_YOUR_VELOCITE_API_KEY_RIGHT_THERE
```