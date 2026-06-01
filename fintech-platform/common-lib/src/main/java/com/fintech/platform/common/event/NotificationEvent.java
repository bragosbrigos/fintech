package com.fintech.platform.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String eventId;
    private Long userId;
    private String type;
    private String subject;
    private String message;
    private String channel;
    private boolean read;
    private LocalDateTime createdAt;
}
