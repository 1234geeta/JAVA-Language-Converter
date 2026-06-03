package com.mycompany.home;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * PDFConverterInterface class - Interface for PDF conversion
 */
public class PDFConverterInterface extends JFrame {
    private static final Color DARK_TEAL = new Color(0, 102, 102); // #006666
    private static final Color TEAL = new Color(0, 153, 153);      // #009999
    
    private JLabel dropZoneLabel;
    private JComboBox<String> sourceLanguageComboBox;
    private JComboBox<String> targetLanguageComboBox;
    private JTextArea statusArea;
    private String currentFilePath;
    
    /**
     * Constructor - Initializes the PDF converter interface
     */
    public PDFConverterInterface() {
        initComponents();
        setupDropZone();
    }
    
    /**
     * Initializes the GUI components
     */
    private void initComponents() {
        setTitle("PDF Converter Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        getContentPane().setBackground(DARK_TEAL);
        setLayout(new BorderLayout(10, 10));
        
        // Create main panel
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
        
        // Drop zone panel
        JPanel dropZonePanel = new JPanel(new BorderLayout());
        dropZonePanel.setBackground(TEAL);
        dropZonePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            "Drop PDF File Here",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            Color.WHITE
        ));
        
        dropZoneLabel = new JLabel("Drag and drop PDF file here", SwingConstants.CENTER);
        dropZoneLabel.setForeground(Color.WHITE);
        dropZoneLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dropZonePanel.add(dropZoneLabel, BorderLayout.CENTER);

        // Add browse button
        JButton browseButton = createStyledButton("Browse PDF");
        browseButton.addActionListener(e -> browseForPDF());
        JPanel browsePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        browsePanel.setOpaque(false);
        browsePanel.add(browseButton);
        dropZonePanel.add(browsePanel, BorderLayout.SOUTH);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(dropZonePanel, gbc);
        
        // Convert button
        JButton convertButton = createStyledButton("Convert");
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertPDF();
            }
        });
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(convertButton, gbc);
        
        // Status area
        statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createTitledBorder("Conversion Status"));
        gbc.gridy = 3;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(statusScrollPane, gbc);
        
        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
        
        // Pack and center the frame
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Sets up the drag and drop functionality for the PDF files
     */
    private void setupDropZone() {
        new DropTarget(this, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (isDragOk(dtde)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                if (isDragOk(dtde)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    Transferable tr = dtde.getTransferable();
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        @SuppressWarnings("unchecked")
                        List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!fileList.isEmpty()) {
                            File file = fileList.get(0);
                            if (file.getName().toLowerCase().endsWith(".pdf")) {
                                handlePDFFile(file);
                            } else {
                                updateStatus("Error: Please drop a valid PDF file");
                            }
                        }
                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    dtde.rejectDrop();
                    updateStatus("Error: " + e.getMessage());
                }
            }
            
            private boolean isDragOk(DropTargetDragEvent dtde) {
                return dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
        });
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
     * Handles the dropped PDF file
     */
    private void handlePDFFile(File file) {
        if (!file.exists()) {
            updateStatus("Error: File does not exist");
            return;
        }

        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            updateStatus("Error: Only PDF files are supported");
            return;
        }

        currentFilePath = file.getAbsolutePath();
        dropZoneLabel.setText(file.getName());
        updateStatus("File loaded: " + file.getName() + "\nReady to translate");
    }
    
    /**
     * Updates the status area with a message
     */
    private void updateStatus(String message) {
        statusArea.setText(message);
    }
    
    /**
     * Opens a file chooser for PDF selection
     */
    private void browseForPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".pdf") || f.isDirectory();
            }
            public String getDescription() {
                return "PDF Files (*.pdf)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            handlePDFFile(file);
        }
    }

    /**
     * Converts the loaded PDF file
     */
    private void convertPDF() {
        if (currentFilePath == null) {
            updateStatus("Error: No PDF file loaded");
            return;
        }
        
        String sourceLanguage = (String) sourceLanguageComboBox.getSelectedItem();
        String targetLanguage = (String) targetLanguageComboBox.getSelectedItem();
        
        // Show progress dialog
        JDialog progressDialog = new JDialog(this, "Converting PDF", true);
        progressDialog.setLayout(new BorderLayout(10, 10));
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        JLabel statusLabel = new JLabel("Converting PDF, please wait...", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        progressDialog.add(statusLabel, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(this);
        
        // Run conversion in background thread
        new Thread(() -> {
            try {
                String result = PDFConverter.convertPDF(currentFilePath, sourceLanguage, targetLanguage);
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    updateStatus(result);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    updateStatus("Error converting PDF: " + e.getMessage());
                });
            }
        }).start();
        
        // Show progress dialog
        progressDialog.setVisible(true);
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