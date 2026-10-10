package com.overseas.portal.controller;

import com.overseas.portal.entity.ServiceCategory;
import com.overseas.portal.entity.ServiceListing;
import com.overseas.portal.entity.User;
import com.overseas.portal.exception.ResourceNotFoundException;
import com.overseas.portal.exception.UnauthorizedException;
import com.overseas.portal.repository.ProviderProfileRepository;
import com.overseas.portal.repository.BookingRepository;
import com.overseas.portal.repository.ServiceCategoryRepository;
import com.overseas.portal.repository.ServiceListingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Services", description = "Service listings and categories")
public class ServiceController {

    private final ServiceListingRepository serviceListingRepository;
    private final BookingRepository bookingRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ProviderProfileRepository providerProfileRepository;

    // Public

    @GetMapping("/categories")
    @Operation(summary = "List all active service categories (public)")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> result = serviceCategoryRepository.findByActiveTrue()
            .stream()
            .map(this::toCategoryMap)
            .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Search service listings (public)")
    public ResponseEntity<Map<String, Object>> searchServices(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @PageableDefault(size = 12) Pageable pageable) {
        Page<ServiceListing> page = serviceListingRepository.searchServices(categoryId, keyword, minPrice, maxPrice, pageable);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", page.getContent().stream().map(this::toListingMap).toList());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("number", page.getNumber());
        response.put("size", page.getSize());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a service listing by ID (public)")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        ServiceListing service = serviceListingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service", id));
        return ResponseEntity.ok(toListingMap(service));
    }

    // Provider-only

    @GetMapping("/my-listings")
    @Operation(summary = "Get authenticated provider listings (all, including inactive)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<Map<String, Object>>> getMyListings(@AuthenticationPrincipal User user) {
        var profile = providerProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));
        List<Map<String, Object>> listings = serviceListingRepository
            .findByProviderId(profile.getId())
            .stream()
            .map(this::toListingMap)
            .toList();
        return ResponseEntity.ok(listings);
    }

    @PostMapping
    @Operation(summary = "Create a service listing")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> create(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {
        var profile = providerProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));

        ServiceListing listing = new ServiceListing();
        listing.setProvider(profile);
        listing.setTitle((String) body.get("title"));
        listing.setDescription((String) body.get("description"));
        if (body.get("price") != null) listing.setPrice(((Number) body.get("price")).doubleValue());
        if (body.get("priceCurrency") != null) listing.setPriceCurrency((String) body.get("priceCurrency"));
        if (body.get("priceType") != null) listing.setPriceType((String) body.get("priceType"));
        if (body.get("durationDays") != null) listing.setDurationDays(((Number) body.get("durationDays")).intValue());

        Object catObj = body.get("category");
        if (catObj instanceof Map) {
            Object catId = ((Map<?, ?>) catObj).get("id");
            if (catId != null) {
                serviceCategoryRepository.findById(((Number) catId).longValue()).ifPresent(listing::setCategory);
            }
        }

        @SuppressWarnings("unchecked")
        List<String> features = (List<String>) body.get("features");
        if (features != null) listing.setFeatures(features);

        ServiceListing saved = serviceListingRepository.save(listing);
        return ResponseEntity.status(HttpStatus.CREATED).body(toListingMap(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a service listing")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PROVIDER')")
    @Transactional
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {
        ServiceListing existing = serviceListingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service", id));

        if (!existing.getProvider().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not own this listing");
        }

        if (body.get("title") != null) existing.setTitle((String) body.get("title"));
        if (body.get("description") != null) existing.setDescription((String) body.get("description"));
        if (body.get("price") != null) existing.setPrice(((Number) body.get("price")).doubleValue());
        if (body.get("durationDays") != null) existing.setDurationDays(((Number) body.get("durationDays")).intValue());
        if (body.get("active") instanceof Boolean active) existing.setActive(active);
        @SuppressWarnings("unchecked")
        List<String> features = (List<String>) body.get("features");
        if (features != null) existing.setFeatures(features);

        return ResponseEntity.ok(toListingMap(existing));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a service listing with no bookings")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PROVIDER')")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        ServiceListing listing = serviceListingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service", id));

        if (!listing.getProvider().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not own this listing");
        }

        if (bookingRepository.countByServiceId(id) > 0) {
            throw new IllegalStateException(
                "This service has bookings and cannot be deleted. Turn it off instead.");
        }

        serviceListingRepository.delete(listing);
        return ResponseEntity.noContent().build();
    }

    // Helpers

    private Map<String, Object> toCategoryMap(ServiceCategory cat) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", cat.getId());
        m.put("name", cat.getName());
        m.put("description", cat.getDescription());
        m.put("icon", cat.getIcon());
        m.put("active", cat.isActive());
        return m;
    }

    private Map<String, Object> toListingMap(ServiceListing sl) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", sl.getId());
        m.put("title", sl.getTitle());
        m.put("description", sl.getDescription());
        m.put("price", sl.getPrice());
        m.put("priceCurrency", sl.getPriceCurrency());
        m.put("priceType", sl.getPriceType());
        m.put("durationDays", sl.getDurationDays());
        m.put("countriesServed", sl.getCountriesServed());
        m.put("features", sl.getFeatures());
        m.put("imageUrl", sl.getImageUrl());
        m.put("rating", sl.getRating());
        m.put("totalReviews", sl.getTotalReviews());
        m.put("totalBookings", sl.getTotalBookings());
        m.put("active", sl.isActive());
        m.put("createdAt", sl.getCreatedAt());
        m.put("updatedAt", sl.getUpdatedAt());
        if (sl.getCategory() != null) {
            m.put("category", toCategoryMap(sl.getCategory()));
        }
        if (sl.getProvider() != null) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", sl.getProvider().getId());
            p.put("companyName", sl.getProvider().getCompanyName());
            p.put("rating", sl.getProvider().getRating());
            p.put("verified", sl.getProvider().isVerified());
            m.put("provider", p);
        }
        return m;
    }
}
