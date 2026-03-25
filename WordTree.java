import java.util.*;

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
     * Wyświetla drzewo poziom po poziomie
     * Level 0: korzeń [*]
     * Level 1: pierwsze litery wszystkich słów itd.
     */
    public void printByLevels() {
        if (root == null) return;

        Queue<Node> queue = new LinkedList<>();   // Kolejka do przetwarzania poziomów
        queue.add(root);
        int level = 0;

        while (!queue.isEmpty()) {
            int nodesInThisLevel = queue.size();      // Ile węzłów jest na bieżącym poziomie
            List<String> levelValues = new ArrayList<>();

            // Przetwarzamy wszystkie węzły z bieżącego poziomu
            for (int i = 0; i < nodesInThisLevel; i++) {
                Node node = queue.poll();

                // Jeśli to koniec słowa, dodajemy gwiazdkę dla czytelności
                String display = String.valueOf(node.value);
                if (node.isEndOfWord) display += "*";

                levelValues.add(display);

                // Dodajemy do kolejki WSZYSTKIE dzieci
                for (List<Node> childList : node.children.values()) {
                    for (Node child : childList) {
                        queue.add(child);
                    }
                }
            }

            // Wyświetlamy bieżący poziom
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
        // Jeśli dotarliśmy do końca słowa - wypisujemy je razem z tłumaczeniami
        if (node.isEndOfWord && !node.translations.isEmpty()) {
            System.out.println("[" + currentWord + "] → " + node.translations);
        }

        // Rekurencyjnie idziemy do wszystkich dzieci
        for (List<Node> childList : node.children.values()) {
            for (Node child : childList) {
                printWordsHelper(child, currentWord + child.value);
            }
        }
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        WordTree tree = new WordTree();

        tree.addWord("DOM", "HOUSE");
        tree.addWord("DOM", "HOME");
        tree.addWord("DACH", "ROOF");
        tree.addWord("KOT", "CAT");

        System.out.println("=== WYDRUK POZIOMAMI ===");
        tree.printByLevels();

        System.out.println("\n=== Pełna lista słów i tłumaczeń ===");
        tree.printWords();
    }
}