package com.pablords.command.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Util {
	public static String canonicalJson(Object dto) {
		try {
			ObjectMapper m = new ObjectMapper();
			m.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
			return m.writeValueAsString(dto); // estável se usar DTOs
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String sha256(String s) throws NoSuchAlgorithmException {
		var md = MessageDigest.getInstance("SHA-256");
		byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (byte b : d)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
