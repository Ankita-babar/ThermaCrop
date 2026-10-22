package com.thermacrop.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/sorting")
@CrossOrigin(origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
public class SortingController {

    private final ConcurrentLinkedDeque<SortingEvent> events =
            new ConcurrentLinkedDeque<>();

    private final AtomicInteger gradeA = new AtomicInteger();
    private final AtomicInteger gradeB = new AtomicInteger();
    private final AtomicInteger gradeC = new AtomicInteger();

    @PostMapping("/events")
    public SortingEvent recordEvent(@RequestBody SortingEvent request) {
        String grade = request.grade() == null
                ? "" : request.grade().trim().toUpperCase();

        String status = request.sortingStatus() == null
                ? "" : request.sortingStatus().trim().toUpperCase();

        if (!List.of("A", "B", "C").contains(grade)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Grade must be A, B or C");
        }

        if (!List.of("SORTED", "FAILED").contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sorting status must be SORTED or FAILED");
        }

        if (!Double.isFinite(request.confidence())
                || request.confidence() < 0
                || request.confidence() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Confidence must be between 0 and 100");
        }

        SortingEvent event = new SortingEvent(
                grade, request.confidence(), status, Instant.now());

        events.addFirst(event);

        while (events.size() > 200) {
            events.pollLast();
        }

        if (status.equals("SORTED")) {
            switch (grade) {
                case "A" -> gradeA.incrementAndGet();
                case "B" -> gradeB.incrementAndGet();
                case "C" -> gradeC.incrementAndGet();
            }
        }

        return event;
    }

    @GetMapping("/summary")
    public Map<String, Integer> getSummary() {
        return Map.of(
                "gradeA", gradeA.get(),
                "gradeB", gradeB.get(),
                "gradeC", gradeC.get(),
                "totalSorted",
                gradeA.get() + gradeB.get() + gradeC.get()
        );
    }

    @GetMapping("/events")
    public List<SortingEvent> getEvents() {
        return List.copyOf(events);
    }
}
