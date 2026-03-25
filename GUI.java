import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class GUI extends JFrame {
    private WordTree tree = new WordTree();

    private JTextField searchField;
    private DefaultListModel<String> polishModel;
    private DefaultListModel<String> englishModel;

    private JList<String> polishList;
    private JList<String> englishList;

    private Map<String, String> dictionary = new LinkedHashMap<>();

    public GUI() {

        setTitle("Polish - English Dictionary");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Pasek wyszukiwania
        searchField = new JTextField();
        add(searchField, BorderLayout.NORTH);

        // Lista modeli
        polishModel = new DefaultListModel<>();
        englishModel = new DefaultListModel<>();

        polishList = new JList<>(polishModel);
        englishList = new JList<>(englishModel);

        // Tworzenie paneli z etykietami
        JPanel polishPanel = new JPanel();
        polishPanel.setLayout(new BorderLayout());
        polishPanel.add(new JLabel("Polish Words", SwingConstants.CENTER), BorderLayout.NORTH);
        polishPanel.add(new JScrollPane(polishList), BorderLayout.CENTER);

        JPanel englishPanel = new JPanel();
        englishPanel.setLayout(new BorderLayout());
        englishPanel.add(new JLabel("English Words", SwingConstants.CENTER), BorderLayout.NORTH);
        englishPanel.add(new JScrollPane(englishList), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, polishPanel, englishPanel);
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);

        // Przyciski
        JButton addButton = new JButton("+");
        JButton deleteButton = new JButton("-");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Akcja dodawania słowa
        addButton.addActionListener(e -> openAddDialog());


    }

    private void openAddDialog() {

        JTextField polishField = new JTextField();
        JTextField englishField = new JTextField();

        Object[] message = {
                "Polish word:", polishField,
                "English translation:", englishField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Add new word",
                JOptionPane.OK_CANCEL_OPTION
        );

        if(option == JOptionPane.OK_OPTION){

            String polish = polishField.getText();
            String english = englishField.getText();


        }
    }





    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GUI().setVisible(true);
        });
    }
}
