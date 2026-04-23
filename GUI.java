import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class GUI extends JFrame {
    // --- Paleta kolorów (Slate/Gray) ---
    private final Color BG_COLOR = new Color(40, 40, 40);      // Tło główne
    private final Color PANEL_COLOR = new Color(50, 50, 50);   // Tło list i pól
    private final Color TEXT_COLOR = new Color(210, 210, 210); // Jasnoszary tekst
    private final Color ACCENT_COLOR = new Color(75, 75, 75);  // Kolor obramowań i zaznaczenia
    private final Color HEADER_TEXT = new Color(130, 130, 130); // Kolor nagłówków

    private DefaultListModel<String> polishModel = new DefaultListModel<>();
    private DefaultListModel<String> englishModel = new DefaultListModel<>();
    private JList<String> polishList;
    private JList<String> englishList;
    private JTextField searchField;

    private WordTree tree = new WordTree();

    private boolean isReversed = false; // false: PL -> EN, true: EN -> PL
    public GUI() {
        // Wczytanie słownika z pliku przy starcie
        tree.loadFromFile("dictionary.txt");

        // Konfiguracja motywu graficznego
        setupTheme();
        customizeScrollbar();

        setTitle("Słownik Polsko-Angielski");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Główny panel z marginesami
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        // --- Pasek wyszukiwania (Góra) ---
        searchField = new JTextField();
        styleComponent(searchField);
        searchField.setCaretColor(Color.WHITE);

        // Nasłuchiwanie zmian w polu tekstowym (wyszukiwanie "na żywo")
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() { refreshLists(searchField.getText().trim()); }
        });

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("SZUKAJ:");
        searchLabel.setForeground(HEADER_TEXT);
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        topPanel.add(searchLabel, BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- Listy słów (Środek) ---
        polishList = createFormalList(polishModel);
        englishList = createFormalList(englishModel);

        // Synchronizacja zaznaczenia między listami
        polishList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) englishList.setSelectedIndex(polishList.getSelectedIndex());
        });
        englishList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) polishList.setSelectedIndex(englishList.getSelectedIndex());
        });

        // Panel dzielony (SplitPane) dla równego rozkładu list
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createSection("Polskie Słowa", polishList),
                createSection("Tłumaczenie Angielskie", englishList));

        // Synchronizacja przewijania (Scroll) między listami
        JScrollPane leftScroll = (JScrollPane) ((JPanel) splitPane.getLeftComponent()).getComponent(1);
        JScrollPane rightScroll = (JScrollPane) ((JPanel) splitPane.getRightComponent()).getComponent(1);
        rightScroll.getVerticalScrollBar().setModel(leftScroll.getVerticalScrollBar().getModel());

        splitPane.setDividerSize(2);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // --- Przyciski (Dół) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);


        JLabel authorLabel = new JLabel("3ID12A Mykyta Lytvyn, Konrad Prokop");
        authorLabel.setForeground(HEADER_TEXT);
        authorLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        authorLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton addButton = createFormalButton("+ DODAJ SŁOWO");
        JButton deleteButton = createFormalButton("- USUŃ WYBRANE");


        JButton reverseButton = createFormalButton("⇄ REVERS");
        reverseButton.addActionListener(e -> {
            isReversed = !isReversed;

            JLabel leftHeader = (JLabel) ((JPanel) splitPane.getLeftComponent()).getComponent(0);
            JLabel rightHeader = (JLabel) ((JPanel) splitPane.getRightComponent()).getComponent(0);

            if (isReversed) {
                leftHeader.setText("TŁUMACZENIE ANGIELSKIE");
                rightHeader.setText("POLSKIE SŁOWA");
            } else {
                leftHeader.setText("POLSKIE SŁOWA");
                rightHeader.setText("TŁUMACZENIE ANGIELSKIE");
            }

            refreshLists(searchField.getText());
        });

        buttonPanel.add(reverseButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(addButton);

        bottomPanel.add(authorLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Powiązanie przycisków z logiką WordTree
        addButton.addActionListener(e -> openAddDialog());
        deleteButton.addActionListener(e -> deleteSelectedEntry());

        // Ustawienie podziału na środku po zainicjowaniu komponentów
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.5));

        // Pierwsze odświeżenie listy (wyświetlenie wszystkich słów)
        refreshLists("");
    }

    /**
     * Ustawia ogólne kolory i właściwości komponentów UI.
     */
    private void setupTheme() {
        UIManager.put("SplitPane.background", BG_COLOR);
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
        UIManager.put("OptionPane.background", BG_COLOR);
        UIManager.put("Panel.background", BG_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        UIManager.put("Button.background", PANEL_COLOR);
        UIManager.put("Button.foreground", TEXT_COLOR);
    }

    /**
     * Otwiera okno dialogowe do dodawania nowego słowa i tłumaczenia.
     */
    private void openAddDialog() {
        JTextField pField = new JTextField();
        JTextField eField = new JTextField();
        styleComponent(pField);
        styleComponent(eField);

        Object[] message = { "Polskie Słowo:", pField, "Angielskie Tłumaczenie:", eField };

        int option = JOptionPane.showConfirmDialog(this, message, "Dodaj nowy wpis",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String p = pField.getText().trim().toUpperCase();
            String e = eField.getText().trim().toUpperCase();
            if (!p.isEmpty() && !e.isEmpty()) {
                tree.addWord(p, e);
                tree.saveToFile("dictionary.txt"); // Zapis do pliku po dodaniu
                refreshLists(searchField.getText());
            }
        }
    }

    /**
     * Usuwa wybraną parę słowo-tłumaczenie z drzewa i odświeża widok.
     */
    private void deleteSelectedEntry() {
        int index = polishList.getSelectedIndex();
        if (index != -1) {
            String word1 = polishModel.getElementAt(index);
            String word2 = englishModel.getElementAt(index);


            String polishWord = isReversed ? word2 : word1;
            String englishWord = isReversed ? word1 : word2;

            tree.deleteTranslation(polishWord, englishWord);
            tree.saveToFile("dictionary.txt");
            refreshLists(searchField.getText());
        } else {
            JOptionPane.showMessageDialog(this, "Wybierz słowo z listy do usunięcia.");
        }
    }

    /**
     * Czyści modele i ponownie wypełnia je danymi z drzewa na podstawie filtra.
     */
    private void refreshLists(String filter) {
        polishModel.clear();
        englishModel.clear();

        String search = filter.trim().toUpperCase();

        if (search.isEmpty()) {
            fillModelsFromTree(tree.root, "", "");
        } else {

            Node startNode = findPrefixNode(search);
            if (startNode != null) {

                fillModelsFromTree(startNode, search, "");
            }


            for (String res : tree.findByEnglish(search)) {
                String[] parts = res.split(";");
                if (isReversed) {
                    polishModel.addElement(parts[1]); // English in left column
                    englishModel.addElement(parts[0]); // Polish in right column
                } else {
                    polishModel.addElement(parts[0]);
                    englishModel.addElement(parts[1]);
                }
            }
        }
    }




    private Node findPrefixNode(String prefix) {
        Node current = tree.root;
        for (char c : prefix.toCharArray()) {
            java.util.List<Node> nodes = current.children.get(c);
            if (nodes == null || nodes.isEmpty()) return null;
            current = nodes.get(0);
        }
        return current;
    }
    /**
     * Rekurencyjnie przeszukuje drzewo w celu znalezienia słów pasujących do filtra.
     */
    private void fillModelsFromTree(Node node, String currentWord, String filter) {
        String wordToDisplay = (node.value == '*') ? "" : currentWord;

        if (node.isEndOfWord) {
            if (filter.isEmpty() || wordToDisplay.contains(filter.toUpperCase())) {
                for (String trans : node.translations) {
                    if (isReversed) {

                        polishModel.addElement(trans);
                        englishModel.addElement(wordToDisplay);
                    } else {

                        polishModel.addElement(wordToDisplay);
                        englishModel.addElement(trans);
                    }
                }
            }
        }

        for (java.util.List<Node> childList : node.children.values()) {
            for (Node child : childList) {
                fillModelsFromTree(child, wordToDisplay + child.value, filter);
            }
        }
    }

    // --- Metody pomocnicze i stylizacja ---

    private JList<String> createFormalList(DefaultListModel<String> model) {
        JList<String> list = new JList<>(model);
        list.setBackground(PANEL_COLOR);
        list.setForeground(TEXT_COLOR);
        list.setSelectionBackground(ACCENT_COLOR);
        list.setSelectionForeground(Color.WHITE);
        list.setFont(new Font("SansSerif", Font.PLAIN, 14));
        list.setFixedCellHeight(30);
        return list;
    }

    private JPanel createSection(String title, JList<String> list) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JLabel header = new JLabel(title.toUpperCase());
        header.setForeground(HEADER_TEXT);
        header.setFont(new Font("SansSerif", Font.BOLD, 10));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(new LineBorder(ACCENT_COLOR, 1));
        scroll.getViewport().setBackground(PANEL_COLOR);

        // --- Ciemny styl paska przewijania ---
        JScrollBar vertical = scroll.getVerticalScrollBar();
        vertical.setPreferredSize(new Dimension(10, 0));
        vertical.setBackground(BG_COLOR);

        // Usuwanie standardowych przycisków strzałek dla lepszego wyglądu
        vertical.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ACCENT_COLOR;
                this.trackColor = BG_COLOR;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void styleComponent(JComponent comp) {
        comp.setBackground(PANEL_COLOR);
        comp.setForeground(TEXT_COLOR);
        comp.setFont(new Font("SansSerif", Font.PLAIN, 14));
        comp.setBorder(new CompoundBorder(new LineBorder(ACCENT_COLOR), new EmptyBorder(8, 10, 8, 10)));
    }

    private JButton createFormalButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PANEL_COLOR);
        btn.setForeground(TEXT_COLOR);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(ACCENT_COLOR), new EmptyBorder(8, 20, 8, 20)));
        return btn;
    }

    /**
     * Dodatkowa konfiguracja kolorów paska przewijania w UIManager.
     */
    private void customizeScrollbar() {
        UIManager.put("ScrollBar.background", BG_COLOR);
        UIManager.put("ScrollBar.foreground", PANEL_COLOR);
        UIManager.put("ScrollBar.track", BG_COLOR);
        UIManager.put("ScrollBar.thumb", ACCENT_COLOR);
        UIManager.put("ScrollBar.thumbDarkShadow", ACCENT_COLOR);
        UIManager.put("ScrollBar.thumbHighlight", ACCENT_COLOR);
        UIManager.put("ScrollBar.thumbShadow", ACCENT_COLOR);
        UIManager.put("ScrollBar.width", 12);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}