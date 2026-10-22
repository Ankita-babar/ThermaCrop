package com.thermacrop.api;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sensors")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5500", "http://127.0.0.1:5500"})
public class SensorController {
    private final AtomicReference<SensorReading> latest = new AtomicReference<>();
    private final CopyOnWriteArrayList<SensorReading> history = new CopyOnWriteArrayList<>();
    @Value("${thermacrop.sensor.dummy.enabled:true}")
    private boolean dummyEnabled;

    public SensorController() {
        addReading(new SensorReading(12.0, 88.0, Instant.now(), "DUMMY"));
    }

    @Scheduled(fixedRateString = "${thermacrop.sensor.dummy.interval-ms:5000}")
    public void generateDummyReading() {
        if (!dummyEnabled) return;
        SensorReading previous = latest.get();
        double oldT = previous == null ? 12.0 : previous.temperature();
        double oldH = previous == null ? 88.0 : previous.humidity();
        double t = clamp(oldT + ThreadLocalRandom.current().nextDouble(-0.5, 0.51), 6.0, 19.0);
        double h = clamp(oldH + ThreadLocalRandom.current().nextDouble(-1.5, 1.51), 70.0, 98.0);
        addReading(new SensorReading(round(t, 1), round(h, 1), Instant.now(), "DUMMY"));
    }

    @GetMapping("/latest")
    public SensorReading latest() { return latest.get(); }

    @GetMapping("/history")
    public List<SensorReading> history() { return List.copyOf(this.history); }

    @PostMapping
    public SensorReading receiveEsp32Reading(@RequestBody SensorPayload payload) {
        if (payload.temperature() == null || payload.humidity() == null
                || !Double.isFinite(payload.temperature()) || !Double.isFinite(payload.humidity())
                || payload.temperature() < -40 || payload.temperature() > 80
                || payload.humidity() < 0 || payload.humidity() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Temperature or humidity is outside the accepted range");
        }
        SensorReading reading = new SensorReading(round(payload.temperature(), 1), round(payload.humidity(), 1), Instant.now(), "ESP32");
        addReading(reading);
        return reading;
    }

    private void addReading(SensorReading reading) {
        latest.set(reading);
        history.add(reading);
        while (history.size() > 60) history.remove(0);
    }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
    private static double round(double value, int places) { double scale = Math.pow(10, places); return Math.round(value * scale) / scale; }
    public record SensorPayload(Double temperature, Double humidity) {}
}
