package com.non.rental.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for normalizing property addresses to prevent redundant API calls.
 * Handles common address variations like abbreviations, case differences, and spacing.
 */
@Component
public class AddressNormalizer {

    private static final Map<String, String> STREET_ABBREVIATIONS = new HashMap<>();
    private static final Map<String, String> DIRECTIONAL_ABBREVIATIONS = new HashMap<>();
    private static final Map<String, String> UNIT_ABBREVIATIONS = new HashMap<>();

    static {
        // Street type abbreviations
        STREET_ABBREVIATIONS.put("street", "st");
        STREET_ABBREVIATIONS.put("avenue", "ave");
        STREET_ABBREVIATIONS.put("road", "rd");
        STREET_ABBREVIATIONS.put("drive", "dr");
        STREET_ABBREVIATIONS.put("lane", "ln");
        STREET_ABBREVIATIONS.put("court", "ct");
        STREET_ABBREVIATIONS.put("circle", "cir");
        STREET_ABBREVIATIONS.put("boulevard", "blvd");
        STREET_ABBREVIATIONS.put("parkway", "pkwy");
        STREET_ABBREVIATIONS.put("highway", "hwy");
        STREET_ABBREVIATIONS.put("place", "pl");
        STREET_ABBREVIATIONS.put("terrace", "ter");
        STREET_ABBREVIATIONS.put("way", "way");
        STREET_ABBREVIATIONS.put("trail", "trl");
        STREET_ABBREVIATIONS.put("plaza", "plz");

        // Directional abbreviations
        DIRECTIONAL_ABBREVIATIONS.put("north", "n");
        DIRECTIONAL_ABBREVIATIONS.put("south", "s");
        DIRECTIONAL_ABBREVIATIONS.put("east", "e");
        DIRECTIONAL_ABBREVIATIONS.put("west", "w");
        DIRECTIONAL_ABBREVIATIONS.put("northeast", "ne");
        DIRECTIONAL_ABBREVIATIONS.put("northwest", "nw");
        DIRECTIONAL_ABBREVIATIONS.put("southeast", "se");
        DIRECTIONAL_ABBREVIATIONS.put("southwest", "sw");

        // Unit type abbreviations
        UNIT_ABBREVIATIONS.put("apartment", "apt");
        UNIT_ABBREVIATIONS.put("suite", "ste");
        UNIT_ABBREVIATIONS.put("unit", "unit");
        UNIT_ABBREVIATIONS.put("building", "bldg");
        UNIT_ABBREVIATIONS.put("floor", "fl");
    }

    /**
     * Normalizes an address string to a standard format.
     * 
     * @param address the raw address string
     * @return normalized address string
     */
    public String normalize(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }

        String normalized = address.trim();

        // Convert to lowercase
        normalized = normalized.toLowerCase();

        // Remove extra whitespace
        normalized = normalized.replaceAll("\\s+", " ");

        // Remove periods
        normalized = normalized.replaceAll("\\.", "");

        // Remove commas
        normalized = normalized.replaceAll(",", "");

        // Normalize street type abbreviations
        for (Map.Entry<String, String> entry : STREET_ABBREVIATIONS.entrySet()) {
            normalized = replaceWord(normalized, entry.getKey(), entry.getValue());
            normalized = replaceWord(normalized, entry.getValue(), entry.getValue());
        }

        // Normalize directional abbreviations
        for (Map.Entry<String, String> entry : DIRECTIONAL_ABBREVIATIONS.entrySet()) {
            normalized = replaceWord(normalized, entry.getKey(), entry.getValue());
            normalized = replaceWord(normalized, entry.getValue(), entry.getValue());
        }

        // Normalize unit type abbreviations
        for (Map.Entry<String, String> entry : UNIT_ABBREVIATIONS.entrySet()) {
            normalized = replaceWord(normalized, entry.getKey(), entry.getValue());
            normalized = replaceWord(normalized, entry.getValue(), entry.getValue());
        }

        // Remove special characters except spaces and hyphens
        normalized = normalized.replaceAll("[^a-z0-9\\s-]", "");

        // Normalize multiple spaces to single space
        normalized = normalized.replaceAll("\\s+", " ");

        return normalized.trim();
    }

    /**
     * Replaces a whole word in a string.
     * 
     * @param text the text to search in
     * @param word the word to replace
     * @param replacement the replacement word
     * @return the text with replacements
     */
    private String replaceWord(String text, String word, String replacement) {
        String pattern = "\\b" + Pattern.quote(word) + "\\b";
        return text.replaceAll(pattern, replacement);
    }

    /**
     * Generates a cache key from a normalized address.
     * 
     * @param address the address to generate a key for
     * @return cache key string
     */
    public String generateCacheKey(String address) {
        return normalize(address);
    }

    /**
     * Checks if two addresses are equivalent after normalization.
     * 
     * @param address1 first address
     * @param address2 second address
     * @return true if addresses are equivalent
     */
    public boolean areEquivalent(String address1, String address2) {
        return normalize(address1).equals(normalize(address2));
    }
}