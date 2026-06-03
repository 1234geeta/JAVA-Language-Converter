package com.mycompany.home;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.SwingUtilities;

public class Typing {

    private static String getApiKey() {
        // Prefer API key from environment variable for security and flexibility
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }

        // Fallback default API key (kept for backward compatibility in this repo).
        // It's better to set GEMINI_API_KEY in your environment or update this method.
        return "AIzaSyAvgDiQlmmytvghAaNK7kVoa9ao84-zbUw";
    }

    public static String convertText(String input, String sourceLanguage, String targetLanguage) {
        if (input == null || input.trim().isEmpty()) {
            return "Please enter some text to convert";
        }

        if (sourceLanguage != null && sourceLanguage.equals(targetLanguage)) {
            return input;
        }

        return translateWithGeminiAPI(input, sourceLanguage, targetLanguage);
    }

    private static String translateWithGeminiAPI(String input, String sourceLanguage, String targetLanguage) {
        try {
            String apiKey = getApiKey();
            if (apiKey.isEmpty()) {
                return "Translation service error: API key not configured. Set GEMINI_API_KEY environment variable.";
            }

            if (!isNetworkAvailable()) {
                return "Unable to translate due to network issues";
            }

             String urlStr = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";
            //  String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

            URL url = new URL(urlStr + "?key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);

            // Map language names to ISO codes when possible for clearer prompts
            String sourceCode = mapLanguageToCode(sourceLanguage);
            String targetCode = mapLanguageToCode(targetLanguage);

            // Build a clear prompt containing both the human-readable name and the ISO code
            String prompt = String.format(
                "Translate the following text from %s (%s) to %s (%s). Provide only the translation without any explanations or additional text: %s",
                safeName(sourceLanguage),
                safeName(sourceCode),
                safeName(targetLanguage),
                safeName(targetCode),
                input.replace("\"", "\\\"")
            );

            String jsonBody = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                prompt.replace("\"", "\\\"")
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] requestBody = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(requestBody);
            }

            StringBuilder response = new StringBuilder();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine);
                    }
                    String responseStr = response.toString();
                    if (responseStr.contains("\"text\":")) {
                        int startIndex = responseStr.indexOf("\"text\":");
                        startIndex = responseStr.indexOf("\"", startIndex + 7) + 1;
                        int endIndex = responseStr.indexOf("\"", startIndex);
                        String translatedText = responseStr.substring(startIndex, endIndex);
                        translatedText = translatedText.replaceAll("^\"|\"$", "").trim();
                        translatedText = translatedText.replaceAll("(?i)^(Translation:|Translated text:)\\s*", "");
                        translatedText = org.apache.commons.text.StringEscapeUtils.unescapeJava(translatedText);
                        translatedText = java.text.Normalizer.normalize(translatedText, java.text.Normalizer.Form.NFKC);

                        if (isRTLLanguage(targetLanguage)) {
                            translatedText = "\u200F" + translatedText;
                        }

                        SwingUtilities.invokeLater(() -> {
                            JOptionPaneProvider.showInfo(String.format("Successfully translated from %s to %s!", sourceLanguage, targetLanguage));
                        });

                        return new String(translatedText.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    } else {
                        return "Translation error: Invalid response format";
                    }
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                String err = response.toString();
                if (err.contains("quota exceeded") || err.contains("API key not valid")) {
                    return "Translation service error: Please check your API key configuration";
                }
                return "Unable to translate. Please try again";
            }
        } catch (Exception e) {
            return "Unable to translate due to network issues: " + e.getMessage();
        }
    }

    // Helper: return a safe string for prompt building
    private static String safeName(String s) {
        return (s == null) ? "" : s;
    }

    // Map friendly language names to short ISO codes where available.
    // This helps make the translation prompt explicit (e.g., Hindi -> hi).
    public static String mapLanguageToCode(String language) {
        if (language == null) return "";
        String key = language.trim().toLowerCase();
        switch (key) {
            case "english": return "en";
            case "hindi": return "hi";
            case "odia": return "or"; // Odia (Oriya) code
            case "bengali": return "bn";
            case "tamil": return "ta";
            case "telugu": return "te";
            case "kannada": return "kn";
            case "malayalam": return "ml";
            case "spanish": return "es";
            case "french": return "fr";
            case "german": return "de";
            case "italian": return "it";
            case "portuguese": return "pt";
            case "russian": return "ru";
            case "chinese": return "zh";
            case "japanese": return "ja";
            case "korean": return "ko";
            case "arabic": return "ar";
            case "urdu": return "ur";
            case "persian": case "farsi": return "fa";
            case "thai": return "th";
            case "vietnamese": return "vi";
            case "indonesian": return "id";
            case "malay": return "ms";
            default: return "";
        }
    }

    private static boolean isNetworkAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ping", "-n", "1", "google.com");
            Process process = processBuilder.start();
            int returnVal = process.waitFor();
            return returnVal == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isRTLLanguage(String language) {
        language = (language == null) ? "" : language.toLowerCase().trim();
        return language.contains("arabic") || language.contains("hebrew") || language.contains("persian") || language.contains("farsi") || language.contains("urdu");
    }
}

// Small provider to avoid direct Swing imports throughout the codebase and keep UI calls isolated
class JOptionPaneProvider {
    static void showInfo(String message) {
        try {
            javax.swing.JOptionPane.showMessageDialog(null, message, "Info", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ignored) {
            // Running in headless mode or caller doesn't want UI; ignore
        }
    }
}