package com.pablords.command.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonConverter implements AttributeConverter<Object, String> {
  static ObjectMapper objectMapper = new ObjectMapper();

  public static String convertToJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert object to JSON", e);
    }
  }

  @Override
  public String convertToDatabaseColumn(Object attribute) {
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert object to JSON", e);
    }
  }

  @Override
  public Object convertToEntityAttribute(String dbData) {
    try {
      return objectMapper.readValue(dbData, Object.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert JSON to object", e);
    }
  }

}