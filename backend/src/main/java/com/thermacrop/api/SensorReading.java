package com.thermacrop.api;

import java.time.Instant;

public record SensorReading(double temperature, double humidity, Instant timestamp, String source) {}
