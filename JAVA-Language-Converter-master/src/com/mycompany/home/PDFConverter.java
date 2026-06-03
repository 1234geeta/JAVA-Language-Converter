package com.mycompany.home;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JOptionPane;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * PDF Language Converter
 * 1. Extracts text from input PDF.
 * 2. Translates to target language using Typing.convertText().
 * 3. Generates a new PDF using script-specific Noto fonts.
 */
public class PDFConverter {

    public static String convertPDF(String filePath, String sourceLanguage, String targetLanguage) {
        try {
            File inputFile = new File(filePath);
            if (!inputFile.exists()) return "Error: File not found";
            if (!inputFile.getName().toLowerCase().endsWith(".pdf")) return "Error: File must be a PDF";

            JOptionPane.showMessageDialog(null, "Extracting text from PDF...", "Processing", JOptionPane.INFORMATION_MESSAGE);
            String extractedText = extractTextFromPDF(inputFile);
            if (extractedText == null || extractedText.trim().isEmpty())
                return "Error: No extractable text found in PDF";

            JOptionPane.showMessageDialog(null,
                    String.format("Extracted %d characters. Translating...", extractedText.length()),
                    "Info", JOptionPane.INFORMATION_MESSAGE);

            String translatedText = Typing.convertText(extractedText, sourceLanguage, targetLanguage);
            if (translatedText == null || translatedText.startsWith("Error"))
                return "Translation failed or API error.";

            String defaultFileName = inputFile.getName().replaceAll("(?i)\\.pdf$", "") + "_" + targetLanguage + ".pdf";
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setSelectedFile(new File(defaultFileName));
            int sel = chooser.showSaveDialog(null);
            if (sel != javax.swing.JFileChooser.APPROVE_OPTION) return "Cancelled by user";

            String outPath = chooser.getSelectedFile().getAbsolutePath();
            JOptionPane.showMessageDialog(null, "Creating PDF with translated text...", "Processing", JOptionPane.INFORMATION_MESSAGE);

            createPDFWithText(translatedText, outPath);

            int open = JOptionPane.showConfirmDialog(null, "Translation completed. Open the PDF?", "Done", JOptionPane.YES_NO_OPTION);
            if (open == JOptionPane.YES_OPTION) openPDF(outPath);

            return "✅ Successfully converted and saved: " + outPath;
        } catch (Exception e) {
            return "Error converting PDF: " + e.getMessage();
        }
    }

    private static String extractTextFromPDF(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            return stripper.getText(document).trim();
        }
    }

    private static void createPDFWithText(String text, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDType0Font font = loadFontForText(document, text);
            if (font == null) throw new IOException("No suitable font found for PDF generation");

            float margin = 50f;
            float fontSize = 12f;
            float leading = 14.5f;
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float maxWidth = pageWidth - (2 * margin);
            int maxLinesPerPage = (int) ((pageHeight - 2 * margin) / leading);

            PDPageContentStream cs = new PDPageContentStream(document, page);
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.newLineAtOffset(margin, pageHeight - margin);
            cs.setLeading(leading);

            int linesPrinted = 0;
            for (String line : text.split("\n")) {
                if (font.getStringWidth(line) / 1000 * fontSize > maxWidth) {
                    while (line.length() > 0) {
                        int cut = line.length();
                        while (cut > 0 && font.getStringWidth(line.substring(0, cut)) / 1000 * fontSize > maxWidth) {
                            int lastSpace = line.substring(0, cut).lastIndexOf(' ');
                            cut = lastSpace > 0 ? lastSpace : cut - 1;
                        }
                        cs.showText(line.substring(0, cut));
                        cs.newLine();
                        line = line.substring(cut).trim();
                        if (++linesPrinted >= maxLinesPerPage) {
                            cs.endText(); cs.close();
                            page = new PDPage(); document.addPage(page);
                            cs = new PDPageContentStream(document, page);
                            cs.beginText(); cs.setFont(font, fontSize);
                            cs.newLineAtOffset(margin, pageHeight - margin);
                            cs.setLeading(leading); linesPrinted = 0;
                        }
                    }
                } else {
                    cs.showText(line);
                    cs.newLine();
                    if (++linesPrinted >= maxLinesPerPage) {
                        cs.endText(); cs.close();
                        page = new PDPage(); document.addPage(page);
                        cs = new PDPageContentStream(document, page);
                        cs.beginText(); cs.setFont(font, fontSize);
                        cs.newLineAtOffset(margin, pageHeight - margin);
                        cs.setLeading(leading); linesPrinted = 0;
                    }
                }
            }

            cs.endText();
            cs.close();
            document.save(outputPath);
        }
    }

    private static PDType0Font loadFontForText(PDDocument doc, String text) {
        String fontFile;
        switch (detectScript(text)) {
            case "deva":
                fontFile = "/fonts/NotoSansDevanagari-Regular.ttf";
                break;
            case "beng":
                fontFile = "/fonts/NotoSansBengali-Regular.ttf";
                break;
            case "taml":
                fontFile = "/fonts/NotoSansTamil-Regular.ttf";
                break;
            case "telu":
                fontFile = "/fonts/NotoSansTelugu-Regular.ttf";
                break;
            case "knda":
                fontFile = "/fonts/NotoSansKannada-Regular.ttf";
                break;
            case "mlym":
                fontFile = "/fonts/NotoSansMalayalam-Regular.ttf";
                break;
            default:
                fontFile = "/fonts/NotoSans-Regular.ttf";
                break;
        }
        try (InputStream is = PDFConverter.class.getResourceAsStream(fontFile)) {
            if (is != null) return PDType0Font.load(doc, is);
        } catch (Exception e) {
            System.err.println("Font load failed: " + e.getMessage());
        }

        // Fallback: try Windows fonts
        String[] paths = {
                "C:/Windows/Fonts/Nirmala.ttf",
                "C:/Windows/Fonts/mangal.ttf",
                "C:/Windows/Fonts/arialuni.ttf",
                "C:/Windows/Fonts/arial.ttf"
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                try { return PDType0Font.load(doc, f); }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static String detectScript(String text) {
        if (text.codePoints().anyMatch(cp -> cp >= 0x0900 && cp <= 0x097F)) return "deva";
        if (text.codePoints().anyMatch(cp -> cp >= 0x0980 && cp <= 0x09FF)) return "beng";
        if (text.codePoints().anyMatch(cp -> cp >= 0x0B80 && cp <= 0x0BFF)) return "taml";
        if (text.codePoints().anyMatch(cp -> cp >= 0x0C00 && cp <= 0x0C7F)) return "telu";
        if (text.codePoints().anyMatch(cp -> cp >= 0x0C80 && cp <= 0x0CFF)) return "knda";
        if (text.codePoints().anyMatch(cp -> cp >= 0x0D00 && cp <= 0x0D7F)) return "mlym";
        return "latin";
    }

    private static void openPDF(String path) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows"))
                new ProcessBuilder("cmd", "/c", "start", path).start();
            else new ProcessBuilder("xdg-open", path).start();
        } catch (Exception ignored) {}
    }
}
