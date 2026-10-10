package com.overseas.portal.dto;

import com.overseas.portal.entity.Notification;

import java.time.LocalDateTime;

public record NotificationDto(
    Long id,
    String type,
    String title,
    String message,
    Long referenceId,
    String referenceType,
    boolean read,
    LocalDateTime createdAt
) {
    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
            notification.getId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getReferenceId(),
            notification.getReferenceType(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
