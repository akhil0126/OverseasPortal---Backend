package com.overseas.portal.dto;

import com.overseas.portal.entity.Document;

import java.time.LocalDateTime;

public record DocumentDto(
    Long id,
    Long userId,
    Long applicationId,
    String documentType,
    String fileName,
    String fileUrl,
    Long fileSize,
    String contentType,
    boolean verified,
    String verificationNotes,
    LocalDateTime createdAt
) {
    public static DocumentDto from(Document document) {
        return new DocumentDto(
            document.getId(),
            document.getUser().getId(),
            document.getApplication() == null ? null : document.getApplication().getId(),
            document.getDocumentType(),
            document.getFileName(),
            document.getFileUrl(),
            document.getFileSize(),
            document.getContentType(),
            document.isVerified(),
            document.getVerificationNotes(),
            document.getCreatedAt()
        );
    }
}
