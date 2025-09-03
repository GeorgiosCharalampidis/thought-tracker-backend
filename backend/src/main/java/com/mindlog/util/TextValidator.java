package com.mindlog.util;

import java.util.regex.Pattern;

public class TextValidator {
    
    // Much more permissive settings since AI does the real validation
    private static final int MIN_MEANINGFUL_LENGTH = 3; // Very short minimum
    private static final int MIN_WORD_COUNT = 3; // At least 3 words to form a thought
    
    // Pattern for mostly numbers or special characters
    private static final Pattern MOSTLY_NON_ALPHA = Pattern.compile("^[^a-zA-Z]*$|^[a-zA-Z]{1,2}[^a-zA-Z]*$");
    
    // Pattern for repeated characters (e.g., "aaaaaaa", "111111")
    private static final Pattern REPEATED_CHARS = Pattern.compile("^(..)\\1{4,}$|^(.)\\1{7,}$");
    
    // Pattern for keyboard mashing (adjacent keys) - more permissive
    private static final Pattern KEYBOARD_MASHING = Pattern.compile("(qwerty|asdfgh|zxcvbn|qweasd|asdqwe)");

    public static boolean isMeaningfulThought(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String cleanedText = text.trim().toLowerCase();
        
        // Check minimum length (very permissive)
        if (cleanedText.length() < MIN_MEANINGFUL_LENGTH) {
            return false;
        }

        // Check for mostly numbers or special characters
        if (MOSTLY_NON_ALPHA.matcher(cleanedText).matches()) {
            return false;
        }
        
        // Check for repeated characters (more permissive)
        if (REPEATED_CHARS.matcher(cleanedText).matches()) {
            return false;
        }
        
        // Check for obvious keyboard mashing (only very obvious patterns)
        if (KEYBOARD_MASHING.matcher(cleanedText.toLowerCase()).find()) {
            return false;
        }
        
        // Check for minimum word count (very permissive)
        String[] words = cleanedText.split("\\s+");
        if (words.length < MIN_WORD_COUNT) {
            return false;
        }

        return true;
    }
    
    public static String getValidationMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Please share a meaningful thought or reflection.";
        }
        
        String cleanedText = text.trim();
        
        if (cleanedText.length() < MIN_MEANINGFUL_LENGTH) {
            return "Please write a bit more - share what's on your mind.";
        }

        if (MOSTLY_NON_ALPHA.matcher(cleanedText).matches()) {
            return "Please use words to express your thoughts.";
        }
        
        return "Please share a meaningful thought or reflection.";
    }
}
