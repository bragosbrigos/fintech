package com.fintech.notification.entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    private UUID id;
    
    private String transactionId;
    
    private String userId;
    
    private String type;
    
    private String subject;
    
    private String message;
    
    private boolean sent;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime sentAt;
}
