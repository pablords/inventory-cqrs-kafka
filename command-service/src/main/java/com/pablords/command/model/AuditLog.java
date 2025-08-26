package com.pablords.command.model;

import lombok.Data;

@Data
public class AuditLog {
  private String id;
  private String eventType;
  private String payload;
  private String status;
  private String errorMessage;
  private String createdAt;
  private String updatedAt;

}
