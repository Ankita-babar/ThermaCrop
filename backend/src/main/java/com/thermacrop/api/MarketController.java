package com.thermacrop.api;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5500", "http://127.0.0.1:5500"})
public class MarketController {
    private final AtomicLong listingIds = new AtomicLong(1000);
    private final AtomicLong enquiryIds = new AtomicLong(5000);
    private final ConcurrentHashMap<Long, ProduceListing> listings = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<BuyerEnquiry> enquiries = new CopyOnWriteArrayList<>();

    public MarketController() {
        seed("Tomato", "Hybrid", 500, 28, "Grade A", "Nashik, Maharashtra", "Ramesh Patil", "Freshly harvested tomatoes.", "https://images.unsplash.com/photo-1546094096-0df4bcaaa337?auto=format&fit=crop&w=900&q=80");
        seed("Apple", "Kashmir Apple", 300, 95, "Grade A", "Pune, Maharashtra", "Fresh Valley Farm", "Packed in crates; pickup by arrangement.", "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=900&q=80");
        seed("Onion", "Red Onion", 800, 22, "Grade B", "Satara, Maharashtra", "Sunita Jadhav", "Bulk produce listing for retailers.", "https://images.unsplash.com/photo-1508747703725-719777637510?auto=format&fit=crop&w=900&q=80");
    }

    @GetMapping("/listings")
    public List<ProduceListing> getListings() {
        return listings.values().stream().sorted(Comparator.comparing(ProduceListing::createdAt).reversed()).toList();
    }

    @PostMapping("/listings")
    @ResponseStatus(HttpStatus.CREATED)
    public ProduceListing createListing(@RequestBody ListingRequest request) {
        if (request.crop() == null || request.crop().isBlank() || request.location() == null || request.location().isBlank()
                || request.sellerName() == null || request.sellerName().isBlank()
                || request.quantityKg() == null || request.quantityKg() < 1 || request.quantityKg() > 100000
                || request.pricePerKg() == null || request.pricePerKg() <= 0 || request.pricePerKg() > 100000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a crop, seller, location, quantity (1–100000 kg) and positive price");
        }
        long id = listingIds.incrementAndGet();
        ProduceListing listing = new ProduceListing(id, request.crop().trim(), safe(request.variety()), request.quantityKg(), request.pricePerKg(),
                safeOr(request.grade(), "Needs review"), request.location().trim(), request.sellerName().trim(), safe(request.description()),
                safe(request.imageUrl()), "Available", Instant.now());
        listings.put(id, listing);
        return listing;
    }

    @PostMapping("/enquiries")
    @ResponseStatus(HttpStatus.CREATED)
    public BuyerEnquiry createEnquiry(@RequestBody EnquiryRequest request) {
        ProduceListing listing = listings.get(request.listingId());
        if (listing == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
        if (request.buyerName() == null || request.buyerName().isBlank() || request.contact() == null || request.contact().isBlank()
                || request.quantityKg() == null || request.quantityKg() <= 0 || request.quantityKg() > listing.quantityKg()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide buyer name, contact and a quantity not exceeding available stock");
        }
        BuyerEnquiry enquiry = new BuyerEnquiry(enquiryIds.incrementAndGet(), listing.id(), listing.crop(), request.buyerName().trim(), request.contact().trim(), request.quantityKg(), safe(request.message()), "New", Instant.now());
        enquiries.add(enquiry);
        return enquiry;
    }

    @GetMapping("/enquiries")
    public List<BuyerEnquiry> getEnquiries() { return List.copyOf(enquiries); }

    private void seed(String crop, String variety, double qty, double price, String grade, String location, String seller, String description, String image) {
        long id = listingIds.incrementAndGet();
        listings.put(id, new ProduceListing(id, crop, variety, qty, price, grade, location, seller, description, image, "Available", Instant.now()));
    }
    private static String safe(String value) { return value == null ? "" : value.trim(); }
    private static String safeOr(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim(); }

    public record ProduceListing(Long id, String crop, String variety, double quantityKg, double pricePerKg, String grade, String location, String sellerName, String description, String imageUrl, String status, Instant createdAt) {}
    public record ListingRequest(String crop, String variety, Double quantityKg, Double pricePerKg, String grade, String location, String sellerName, String description, String imageUrl) {}
    public record EnquiryRequest(Long listingId, String buyerName, String contact, Double quantityKg, String message) {}
    public record BuyerEnquiry(Long id, Long listingId, String crop, String buyerName, String contact, double quantityKg, String message, String status, Instant createdAt) {}
}
