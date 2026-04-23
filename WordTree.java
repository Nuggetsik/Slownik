import java.util.*;
import java.nio.charset.StandardCharsets;
import java.io.*;

/**
 * Klasa reprezentująca pojedynczy węzeł w naszym drzewie.
 * Drzewo nie jest klasycznym Trie - celowo używamy List<Node> dla dzieci
 */
class Node {
    // Wartość litery w tym węźle (np. 'D', 'O', 'M')
    char value;

    // Klucz: litera, Wartość: lista wszystkich dzieci zaczynających się na tę literę.
    // Dzięki List<Node> możemy mieć wiele równoległych gałęzi dla tej samej litery.
    Map<Character, List<Node>> children = new LinkedHashMap<>();

    boolean isEndOfWord = false;   // Czy ten węzeł kończy jakieś słowo?
    List<String> translations = new ArrayList<>();  // Lista tłumaczeń na angielski (może być kilka)

    Node(char value) {
        this.value = value;
    }

    /**
     * Metoda dodająca dziecko do listy dzieci dla danej litery.
     */
    void addChild(Node child) {
        // computeIfAbsent - jeśli nie ma jeszcze listy dla tej litery, tworzy nową pustą listę
        children.computeIfAbsent(child.value, k -> new ArrayList<>()).add(child);
    }
}

/**
 * Główna klasa drzewa słów (słownik polsko-angielski).
 */
class WordTree {

    public final Node root = new Node('*');   // Korzeń drzewa - specjalny węzeł startowy

    /**
     * Dodaje słowo polskie wraz z jego tłumaczeniem angielskim.
     * Dla każdej litery tworzy nowy węzeł, ale stara się reutilizować istniejący,
     * jeśli taka litera już istnieje na tym poziomie (żeby nie mnożyć niepotrzebnie identycznych gałęzi).
     */
    public void addWord(String polish, String english) {
        Node current = root;

        // Przechodzimy przez każdą literę polskiego słowa
        for (char c : polish.toCharArray()) {

            // Sprawdzamy, czy na tym poziomie już istnieje choć jeden węzeł z tą literą
            List<Node> existing = current.children.get(c);

            Node nextNode;
            if (existing == null || existing.isEmpty()) {
                // Nie ma jeszcze takiej litery - tworzymy zupełnie nowy węzeł
                nextNode = new Node(c);
                current.addChild(nextNode);
            } else {
                // Jest już co najmniej jeden - idziemy do pierwszego
                nextNode = existing.get(0);
            }

            current = nextNode;   // Przechodzimy do następnego poziomu
        }

        // Po dodaniu wszystkich liter oznaczamy koniec słowa i dodajemy tłumaczenie
        current.isEndOfWord = true;
        current.translations.add(english);
    }

    /**
     * Wyświetla drzewo poziom po poziomie.
     */
    public void printByLevels() {
        if (root == null) return;

        Queue<Node> queue = new LinkedList<>();   // Kolejka do przetwarzania poziomów
        queue.add(root);
        int level = 0;

        while (!queue.isEmpty()) {
            int nodesInThisLevel = queue.size();      // Ile węzłów jest na bieżącym poziomie
            List<String> levelValues = new ArrayList<>();

            for (int i = 0; i < nodesInThisLevel; i++) {
                Node node = queue.poll();

                String display = String.valueOf(node.value);
                if (node.isEndOfWord) display += "*";

                levelValues.add(display);

                for (List<Node> childList : node.children.values()) {
                    for (Node child : childList) {
                        queue.add(child);
                    }
                }
            }

            System.out.println("Level " + level + " : " + levelValues);
            level++;
        }
    }

    /**
     * Klasyczny rekurencyjny wydruk wszystkich słów i ich tłumaczeń.
     */
    public void printWords() {
        printWordsHelper(root, "");
    }

    private void printWordsHelper(Node node, String currentWord) {
        if (node.isEndOfWord && !node.translations.isEmpty()) {
            System.out.println("[" + currentWord + "] → " + node.translations);
        }

        for (List<Node> childList : node.children.values()) {
            for (Node child : childList) {
                printWordsHelper(child, currentWord + child.value);
            }
        }
    }

    /**
     * Szuka wszystkich tłumaczeń dla podanego polskiego słowa.
     */
    public List<String> findTranslations(String polish) {
        Node target = findNode(polish);
        if (target != null && target.isEndOfWord) {
            return new ArrayList<>(target.translations);
        }
        return Collections.emptyList();
    }

    /**
     * Usuwa konkretne tłumaczenie dla danego słowa.
     */
    public void deleteTranslation(String polish, String englishToRemove) {
        deleteRecursive(root, polish, 0, englishToRemove);
    }

    /**
     * Prywatna metoda pomocnicza do znalezienia węzła odpowiadającego całemu słowu.
     */
    private Node findNode(String word) {
        Node current = root;
        for (char c : word.toCharArray()) {
            List<Node> nodes = current.children.get(c);
            if (nodes == null || nodes.isEmpty()) return null;
            // Zgodnie z logiką addWord, idziemy wzdłuż pierwszej dostępnej gałęzi
            current = nodes.get(0);
        }
        return current;
    }

    /**
     * Rekurencyjne usuwanie tłumaczenia oraz czyszczenie nieużywanych węzłów drzewa.
     */
    private boolean deleteRecursive(Node current, String word, int index, String translation) {
        // Przypadek bazowy: dotarliśmy do końca słowa
        if (index == word.length()) {
            if (!current.isEndOfWord) return false;

            // Usuwamy wybrane tłumaczenie z listy
            current.translations.remove(translation);

            // Jeśli słowo nie ma już żadnych tłumaczeń, przestaje być końcem słowa
            if (current.translations.isEmpty()) {
                current.isEndOfWord = false;
            }

            // Węzeł można usunąć tylko jeśli nie jest końcem innego słowa i nie ma dzieci
            return !current.isEndOfWord && current.children.isEmpty();
        }

        char c = word.charAt(index);
        List<Node> nodes = current.children.get(c);
        if (nodes == null || nodes.isEmpty()) return false;

        // Kontynuujemy usuwanie w głąb drzewa
        Node child = nodes.get(0);
        boolean canDeleteChild = deleteRecursive(child, word, index + 1, translation);

        // Jeśli dziecko zwróciło true, usuwamy je z listy dzieci obecnego węzła
        if (canDeleteChild) {
            nodes.remove(child);
            if (nodes.isEmpty()) {
                current.children.remove(c);
            }
        }

        // Sprawdzamy, czy obecny węzeł również stał się bezużyteczny i może być usunięty
        return !current.isEndOfWord && current.children.isEmpty();
    }

    public List<String> findByEnglish(String englishFilter) {
        List<String> results = new ArrayList<>();
        findByEnglishRecursive(root, "", englishFilter.toUpperCase(), results);
        return results;
    }

    private void findByEnglishRecursive(Node node, String currentWord, String filter, List<String> results) {
        String word = (node.value == '*') ? "" : currentWord;

        if (node.isEndOfWord) {
            for (String trans : node.translations) {
                if (trans.toUpperCase().contains(filter)) {
                    results.add(word + ";" + trans);
                }
            }
        }

        for (List<Node> childList : node.children.values()) {
            for (Node child : childList) {
                findByEnglishRecursive(child, word + child.value, filter, results);
            }
        }
    }

    /**
     * Zapisuje całą zawartość drzewa do pliku tekstowego w formacie UTF-8.
     */
    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            saveRecursive(root, "", writer);
            System.out.println("Słownik został zapisany w " + filename);
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisywania: " + e.getMessage());
        }
    }

    /**
     * Rekurencyjnie przechodzi przez drzewo i zapisuje słowa wraz z tłumaczeniami (format SŁOWO;T1,T2).
     */
    private void saveRecursive(Node node, String currentWord, PrintWriter writer) {
        String wordToSave = (node.value == '*') ? "" : currentWord;

        if (node.isEndOfWord && !node.translations.isEmpty()) {
            // Zapisujemy słowo polskie i listę tłumaczeń oddzieloną przecinkami
            writer.println(wordToSave + ";" + String.join(",", node.translations));
        }

        // Przechodzimy przez wszystkie gałęzie dzieci
        for (List<Node> childList : node.children.values()) {
            for (Node child : childList) {
                saveRecursive(child, wordToSave + child.value, writer);
            }
        }
    }

    /**
     * Wczytuje dane słownika z pliku i buduje strukturę drzewa.
     */
    public void loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }


                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String polish = parts[0].toUpperCase();
                    String[] translations = parts[1].split(",");

                    for (String eng : translations) {
                        addWord(polish, eng.trim().toUpperCase());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}