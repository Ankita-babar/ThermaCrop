# ThermaCrop upgraded prototype

This version keeps the existing ThermaCrop login/registration demo, farmer dashboard, tomato grade display, weight input/output and charts. It brings in selected AgriNexus features: separate Buy Produce and Sell Produce pages, listing cards with crop pictures, search/filtering, seller enquiries, and a batch of demo listings. The Java Spring Boot service provides a dummy sensor feed and marketplace APIs.

## Technology
- Frontend: existing ThermaCrop HTML/CSS/Bootstrap/jQuery/Chart.js prototype
- Backend: Java 21, Spring Boot 3.5.6, Maven
- Sensor: generated dummy temperature/humidity every 5 seconds (no ESP32 needed yet)
- Marketplace: in-memory demo data (not persistent across backend restarts)

## Run it on macOS

### 1. Start Spring Boot
Open Terminal:
```bash
cd path/to/ThermaCrop_Upgraded/backend
mvn spring-boot:run
```
Backend endpoints:
- `GET http://localhost:8080/api/sensors/latest`
- `GET http://localhost:8080/api/sensors/history`
- `POST http://localhost:8080/api/sensors` (for future ESP32 readings)
- `GET http://localhost:8080/api/market/listings`
- `POST http://localhost:8080/api/market/listings`
- `POST http://localhost:8080/api/market/enquiries`
- `GET http://localhost:8080/api/market/enquiries`

Quick test:
```bash
curl http://localhost:8080/api/sensors/latest
curl http://localhost:8080/api/market/listings
```

### 2. Open the frontend
For the simplest demo, open `frontend/index.html` in your browser. If your browser blocks local-file fetch requests, serve it with VS Code Live Server on `http://localhost:5500` or run:
```bash
cd path/to/ThermaCrop_Upgraded/frontend
python3 -m http.server 5500
```
Then open `http://localhost:5500`. Spring Boot CORS is configured for ports 5173 and 5500.

### 3. Demo login
- Farmer: `farmer@demo.com` / `Farmer@123`
- Retailer: `retailer@demo.com` / `Retail@123`

After login use the navbar to open Dashboard, Buy Produce or Sell Produce. The farmer dashboard reads the dummy sensor API. Use Sell Produce to create a listing; Buy Produce to filter listings and send enquiries.

## Future ESP32 integration
The ESP32 should send JSON to `POST http://<MAC-LAN-IP>:8080/api/sensors`:
```json
{"temperature":12.4,"humidity":86.0}
```
Use your Mac's LAN IP rather than `localhost` from the ESP32. Once the hardware is ready, set `thermacrop.sensor.dummy.enabled=false` in `application.properties` so scheduled dummy readings stop overwriting real readings. Both devices must be on the same reachable network. Keep the API restricted to a trusted network; do not expose it publicly without authentication and HTTPS.

## Prototype limitations
- Login/registration is still the original browser-local demo, not secure backend authentication.
- Weight entry remains in the existing dashboard's browser-side demo data.
- Sensor readings, listings and enquiries are stored in memory; restarting Spring Boot resets them.
- Demo sensor values and marketplace entries are illustrative, not verified real-world data. Add a database and authentication before production use.
- Crop grading shown by this prototype is a simple demonstration calculation, not a validated food-quality certification.
