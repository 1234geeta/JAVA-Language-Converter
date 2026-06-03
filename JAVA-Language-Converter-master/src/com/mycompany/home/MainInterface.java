package com.mycompany.home;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * MainInterface class - The main welcome screen of the Language Converter application
 */
public class MainInterface extends JFrame {
    private static final Color DARK_TEAL = new Color(0, 102, 102); // #006666
    private static final Color TEAL = new Color(0, 153, 153);      // #009999
    
    /**
     * Constructor - Initializes the main interface
     */
    public MainInterface() {
        initComponents();
    }
    
    /**
     * Initializes the GUI components
     */
    private void initComponents() {
        // Set up the frame
        setTitle("MainInterface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        getContentPane().setBackground(DARK_TEAL);
        setLayout(new BorderLayout());
        
        // Create welcome label
        JLabel welcomeLabel = new JLabel("WELCOME");
        welcomeLabel.setFont(new Font("Cambria", Font.BOLD, 36));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);
        
        // Create Typing button
        JButton typingButton = createStyledButton("Typing");
        typingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openTypingInterface();
            }
        });
        
        // Create PDFConverter button
        JButton pdfButton = createStyledButton("PDFConverter");
        pdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPDFConverterInterface();
            }
        });
        
        // Add buttons to panel
        buttonPanel.add(typingButton);
        buttonPanel.add(pdfButton);
        
        // Add components to frame
        add(welcomeLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        
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
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(200, 50));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }
    
    /**
     * Opens the Typing Interface
     */
    private void openTypingInterface() {
        TypingInterface typingInterface = new TypingInterface();
        typingInterface.setVisible(true);
        this.dispose();
    }
    
    /**
     * Opens the PDF Converter Interface
     */
    private void openPDFConverterInterface() {
        PDFConverterInterface pdfInterface = new PDFConverterInterface();
        pdfInterface.setVisible(true);
        this.dispose();
    }
    
    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        // Use SwingUtilities to ensure thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainInterface().setVisible(true);
            }
        });
    }
}