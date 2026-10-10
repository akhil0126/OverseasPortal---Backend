package com.overseas.portal.controller;

import com.overseas.portal.dto.NotificationDto;
import com.overseas.portal.entity.User;
import com.overseas.portal.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Authenticated user notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    @Operation(summary = "Get the authenticated user's notifications")
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMyNotifications(user.getId(), pageable));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get the authenticated user's notification counts")
    public ResponseEntity<Map<String, Object>> getSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getSummary(user.getId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark one of the authenticated user's notifications as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all of the authenticated user's notifications as read")
    public ResponseEntity<Map<String, String>> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("message", "Notifications marked as read"));
    }
}
