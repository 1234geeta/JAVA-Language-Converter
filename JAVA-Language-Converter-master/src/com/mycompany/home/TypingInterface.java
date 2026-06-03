package com.mycompany.home;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * TypingInterface class - Interface for text conversion
 */
public class TypingInterface extends JFrame {
    private static final Color DARK_TEAL = new Color(0, 102, 102); // #006666
    private static final Color TEAL = new Color(0, 153, 153);      // #009999
    
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JComboBox<String> sourceLanguageComboBox;
    private JComboBox<String> targetLanguageComboBox;
    
    /**
     * Constructor - Initializes the typing interface
     */
    public TypingInterface() {
        initComponents();
    }
    
    /**
     * Initializes the GUI components
     */
    private void initComponents() {
        setTitle("Typing Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        getContentPane().setBackground(DARK_TEAL);
        setLayout(new BorderLayout(10, 10));
        
        // Create the main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Back button
        JButton backButton = createStyledButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnToMain();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(backButton, gbc);
        
        // Language selection panel
        JPanel languagePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        languagePanel.setOpaque(false);

        // Source language
        JLabel sourceLabel = new JLabel("Source Language:");
        sourceLabel.setForeground(Color.WHITE);
        String[] languages = {
            "English", "Hindi", "Odia", "Bengali", "Tamil", "Telugu", "Kannada", "Malayalam",
            "Spanish", "French", "German", "Italian", "Portuguese", "Russian",
            "Chinese", "Japanese", "Korean", "Arabic", "Urdu", "Persian",
            "Thai", "Vietnamese", "Indonesian", "Malay"
        };
        sourceLanguageComboBox = new JComboBox<>(languages);
        sourceLanguageComboBox.setBackground(TEAL);
        sourceLanguageComboBox.setForeground(Color.WHITE);

        // Target language
        JLabel targetLabel = new JLabel("Target Language:");
        targetLabel.setForeground(Color.WHITE);
        targetLanguageComboBox = new JComboBox<>(languages);
        targetLanguageComboBox.setBackground(TEAL);
        targetLanguageComboBox.setForeground(Color.WHITE);
        
        // Add components to language panel
        languagePanel.add(sourceLabel);
        languagePanel.add(sourceLanguageComboBox);
        languagePanel.add(targetLabel);
        languagePanel.add(targetLanguageComboBox);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(languagePanel, gbc);
        
        // Input text area
        inputTextArea = new JTextArea(10, 40);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        // Try to set a Unicode-capable font for better Indic script rendering
        Font preferred = loadPreferredFont(16f);
        if (preferred != null) {
            inputTextArea.setFont(preferred);
        }
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputScrollPane.setBorder(BorderFactory.createTitledBorder("Input Text"));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(inputScrollPane, gbc);
        
        // Convert button
        JButton convertButton = createStyledButton("Convert");
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertText();
            }
        });
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(convertButton, gbc);
        
        // Output text area
        outputTextArea = new JTextArea(10, 40);
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setEditable(false);
        if (preferred != null) {
            outputTextArea.setFont(preferred);
        }
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("Converted Text"));
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(outputScrollPane, gbc);
        
        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
        
        // Pack and center the frame
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Creates a styled button with consistent appearance
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(TEAL);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }
    
    /**
     * Converts the input text using the Typing utility class
     */
    private void convertText() {
        String input = inputTextArea.getText();
        String sourceLanguage = (String) sourceLanguageComboBox.getSelectedItem();
        String targetLanguage = (String) targetLanguageComboBox.getSelectedItem();
        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some text to convert", "Empty input", JOptionPane.WARNING_MESSAGE);
            inputTextArea.requestFocusInWindow();
            return;
        }

        // If user typed English but source language isn't set to English, try to auto-detect by simple heuristic
        if (sourceLanguage == null || !sourceLanguage.equalsIgnoreCase("English")) {
            String asciiOnly = input.replaceAll("\\s+", "");
            if (asciiOnly.matches("[A-Za-z0-9,\\.\\?\\!'-]+")) {
                sourceLanguage = "English"; // override to English for typical English input
            }
        }

        String result = Typing.convertText(input, sourceLanguage, targetLanguage);
        outputTextArea.setText(result);
        // If the result contains Indic scripts, switch to an appropriate font to avoid tofu (missing glyph boxes)
        if (containsDevanagari(result)) {
            Font f = findAvailableFont(new String[]{"Noto Sans Devanagari", "Nirmala UI", "Mangal", "Lohit Devanagari", "NotoSans-Regular"}, 16);
            if (f != null) outputTextArea.setFont(f);
        } else if (containsBengali(result)) {
            Font f = findAvailableFont(new String[]{"Noto Sans Bengali", "Nirmala UI", "Lohit Bengali", "NotoSans-Regular"}, 16);
            if (f != null) outputTextArea.setFont(f);
        } else if (containsTamil(result)) {
            Font f = findAvailableFont(new String[]{"Lohit Tamil", "Noto Sans Tamil", "Nirmala UI", "NotoSans-Regular"}, 16);
            if (f != null) outputTextArea.setFont(f);
        } else if (containsTelugu(result)) {
            Font f = findAvailableFont(new String[]{"Noto Sans Telugu", "Nirmala UI", "Lohit Telugu", "NotoSans-Regular"}, 16);
            if (f != null) outputTextArea.setFont(f);
        } else if (containsKannada(result)) {
            Font f = findAvailableFont(new String[]{"Noto Sans Kannada", "Nirmala UI", "Lohit Kannada", "NotoSans-Regular"}, 16);
            if (f != null) outputTextArea.setFont(f);
        } else if (containsMalayalam(result)) {
            Font f = findAvailableFont(new String[]{"Noto Sans Malayalam", "Nirmala UI", "Lohit Malayalam", "NotoSans-Regular"}, 16);
            if (f != null) outputTextArea.setFont(f);
        }
    }

    // Try to load bundled font from resources (src/main/resources/fonts/NotoSans-Regular.ttf) as a generic fallback
    private Font loadPreferredFont(float size) {
        try (InputStream is = getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf")) {
            if (is != null) {
                Font base = Font.createFont(Font.TRUETYPE_FONT, is);
                return base.deriveFont(size);
            }
        } catch (Exception ignored) {
        }
        // Try common system fonts known to support Indic scripts
        Font f = findAvailableFont(new String[]{"Nirmala UI", "Mangal", "Arial Unicode MS", "Segoe UI"}, (int) size);
        return f;
    }

    private Font findAvailableFont(String[] names, int size) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        for (String candidate : names) {
            for (String installed : available) {
                if (installed.equalsIgnoreCase(candidate) || installed.toLowerCase().contains(candidate.toLowerCase())) {
                    return new Font(installed, Font.PLAIN, size);
                }
            }
        }
        return null;
    }

    private boolean containsDevanagari(String s) {
        return s != null && s.codePoints().anyMatch(cp -> cp >= 0x0900 && cp <= 0x097F);
    }

    private boolean containsBengali(String s) {
        return s != null && s.codePoints().anyMatch(cp -> cp >= 0x0980 && cp <= 0x09FF);
    }

    private boolean containsTamil(String s) {
        return s != null && s.codePoints().anyMatch(cp -> cp >= 0x0B80 && cp <= 0x0BFF);
    }

    private boolean containsTelugu(String s) {
        return s != null && s.codePoints().anyMatch(cp -> cp >= 0x0C00 && cp <= 0x0C7F);
    }

    private boolean containsKannada(String s) {
        return s != null && s.codePoints().anyMatch(cp -> cp >= 0x0C80 && cp <= 0x0CFF);
    }

    private boolean containsMalayalam(String s) {
        return s != null && s.codePoints().anyMatch(cp -> cp >= 0x0D00 && cp <= 0x0D7F);
    }
    
    /**
     * Returns to the main interface
     */
    private void returnToMain() {
        MainInterface mainInterface = new MainInterface();
        mainInterface.setVisible(true);
        this.dispose();
    }
}