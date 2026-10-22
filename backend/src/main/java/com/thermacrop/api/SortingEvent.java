package com.thermacrop.api;

import java.time.Instant;

public record SortingEvent(
        String grade,
        double confidence,
        String sortingStatus,
        Instant timestamp
) {}
