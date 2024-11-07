import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class TagExtractor extends JFrame {
    private JTextArea outputArea;
    private JButton selectFileBtn, selectStopWordsBtn, extractBtn, saveBtn;
    private JLabel fileNameLabel, stopWordsLabel;
    private File inputFile, stopWordsFile;
    private Map<String, Integer> tagFrequency;

    public TagExtractor() {
        setTitle("Tag/Keyword Extractor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize components
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        selectFileBtn = new JButton("Select Input File");
        selectStopWordsBtn = new JButton("Select Stop Words File");
        extractBtn = new JButton("Extract Tags");
        saveBtn = new JButton("Save Tags");

        fileNameLabel = new JLabel("No file selected");
        stopWordsLabel = new JLabel("No stop words file selected");

        // Create panels
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        topPanel.add(selectFileBtn);
        topPanel.add(fileNameLabel);
        topPanel.add(selectStopWordsBtn);
        topPanel.add(stopWordsLabel);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(extractBtn);
        bottomPanel.add(saveBtn);

        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Add action listeners
        selectFileBtn.addActionListener(e -> selectFile());
        selectStopWordsBtn.addActionListener(e -> selectStopWordsFile());
        extractBtn.addActionListener(e -> extractTags());
        saveBtn.addActionListener(e -> saveTags());

        // Initially disable buttons
        extractBtn.setEnabled(false);
        saveBtn.setEnabled(false);
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            inputFile = fileChooser.getSelectedFile();
            fileNameLabel.setText("Selected file: " + inputFile.getName());
            checkExtractReady();
        }
    }

    private void selectStopWordsFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            stopWordsFile = fileChooser.getSelectedFile();
            stopWordsLabel.setText("Stop words file: " + stopWordsFile.getName());
            checkExtractReady();
        }
    }

    private void checkExtractReady() {
        extractBtn.setEnabled(inputFile != null && stopWordsFile != null);
    }

    private void extractTags() {
        try {
            Set<String> stopWords = readStopWords(stopWordsFile);
            tagFrequency = extractTagsFromFile(inputFile, stopWords);
            displayTags(tagFrequency);
            saveBtn.setEnabled(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error extracting tags: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Set<String> readStopWords(File file) throws IOException {
        Set<String> stopWords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.toLowerCase().trim());
            }
        }
        return stopWords;
    }

    private Map<String, Integer> extractTagsFromFile(File inputFile, Set<String> stopWords) throws IOException {
        Map<String, Integer> tagFrequency = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    if (!word.isEmpty() && !stopWords.contains(word)) {
                        tagFrequency.put(word, tagFrequency.getOrDefault(word, 0) + 1);
                    }
                }
            }
        }
        return tagFrequency;
    }

    private void displayTags(Map<String, Integer> tagFrequency) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : tagFrequency.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        outputArea.setText(sb.toString());
    }

    private void saveTags() {
        if (tagFrequency == null || tagFrequency.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tags to save.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                for (Map.Entry<String, Integer> entry : tagFrequency.entrySet()) {
                    writer.println(entry.getKey() + ": " + entry.getValue());
                }
                JOptionPane.showMessageDialog(this, "Tags saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving tags: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TagExtractor().setVisible(true));
    }
}