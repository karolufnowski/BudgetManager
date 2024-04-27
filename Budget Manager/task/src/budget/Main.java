package budget;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    static String foodCategory = "Food";
    static String clothesCategory = "Clothes";
    static String entertainmentCategory = "Entertainment";
    static String otherCategory = "Other";

    static Map<Integer, String> categories = Map.of(
            1, foodCategory,
            2, clothesCategory,
            3, entertainmentCategory,
            4, otherCategory
    );

    public static void main(String[] args) throws IOException {
        java.util.Locale.setDefault(Locale.US);


        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println(s);

        Scanner scanner = new Scanner(System.in);
        double balance = 0.00;

        Map<String, PurchaseInfo> purchaseInfoMap = new HashMap<>();

        while (true) {
            showMenu();
            int userChoice = scanner.nextInt();
            scanner.nextLine();

            if (userChoice == 1) {
                System.out.println();
                System.out.println("Enter income:");
                double userIncome = scanner.nextDouble();
                scanner.nextLine();
                balance += userIncome;
                System.out.println("Income was added!");
                System.out.println();

            } else if (userChoice == 2) {
                System.out.println();
                addPurchase(scanner, balance, purchaseInfoMap);

            } else if (userChoice == 3) {
                showPurchases(scanner, purchaseInfoMap);

            } else if (userChoice == 4) {
                System.out.println();
                System.out.println("Balance: $" + balance);
                System.out.println();

            } else if (userChoice == 5) {
                FileWriter fileWriter = new FileWriter("purchases.txt", true);
                Scanner saveScanner = new Scanner(new FileReader("purchases.txt"));
                if (!saveScanner.hasNextDouble()) {

                    fileWriter.write((balance) + "\n");
                }

                for (Map.Entry<String, PurchaseInfo> entry : purchaseInfoMap.entrySet()) {
                    String purchaseLine = entry.getKey() + "," + entry.getValue().getFormattedAmount() + "," + entry.getValue().category + "\n";
                    fileWriter.write(purchaseLine);
                }

                fileWriter.close();
                System.out.println();
                System.out.println("Purchases were saved!");
                System.out.println();


            } else if (userChoice == 6) {
                Scanner fileScanner = new Scanner(new FileReader("purchases.txt"));
                while (fileScanner.hasNextLine()) {
                    String line = String.valueOf(fileScanner.nextLine());
                    try {
                        double loadedBalance = Double.parseDouble(line);
//                    fileScanner.nextLine();
                        balance = loadedBalance;
                    } catch (NumberFormatException exception) {

                    }
                    String[] values = line.split(",");
                    if (values.length == 3) {
                        double purchasePrice = Double.parseDouble(values[1]);
                        purchaseInfoMap.put(values[0], new PurchaseInfo(purchasePrice, values[2]));
                        balance -= purchasePrice;
                    }
                }

                fileScanner.close();
                System.out.println();
                System.out.println("Purchases were loaded!");
                System.out.println();


            } else if (userChoice == 7) {
                System.out.println();
                sortPurchases(scanner, purchaseInfoMap);
                System.out.println();

            } else if (userChoice == 0) {
                System.out.println();
                System.out.println("Bye!");
                break;
            } else {
                System.out.println();
                System.out.println("Invalid choice. Please choose a valid option.");
                System.out.println();
            }
        }
    }

    private static void sortPurchases(Scanner scanner, Map<String, PurchaseInfo> purchaseInfoMap) {
        while (true) {
            showSortMenu();
            int sortChoice = scanner.nextInt();
            scanner.nextLine();
            if (sortChoice >= 1 && sortChoice <= 3) {
                switch (sortChoice) {
                    case 1:
                        Map<String, PurchaseInfo> purchases = sortAllPurchases(purchaseInfoMap);
                        if (purchases.isEmpty()) {
                            System.out.println();
                            System.out.println("The purchase list is empty!");
                            System.out.println();
                        } else {
                            printAllPurchases(purchases);
                            System.out.println();
                        }
                        break;
                    case 2:
                        Map<String, Double> sortedByType = sortByType(purchaseInfoMap);
                        printExpensesByType(sortedByType);
                        System.out.println();
                        break;
                    case 3:
                        System.out.println();
                        showPurchaseSortMenu();
                        int sortInput = scanner.nextInt();
                        String category = categories.get(sortInput);
                        Map<String, PurchaseInfo> sortedMap = sortCertainType(category, purchaseInfoMap);
                        if (sortedMap.isEmpty()) {
                            System.out.println();
                            System.out.println("The purchase list is empty!");
                            System.out.println();
                        } else {
                            printSortedByTypePurchases(category, sortedMap);
                            System.out.println();
                        }
                        break;
                }
            } else if (sortChoice == 4) {
                break;
            }
        }
    }

    // ENTRY SET DO WYJASNIENIA :) i comparator i method reference i czemu nie dzialalo reserved przy lambdzie
    private static Map<String, PurchaseInfo> sortAllPurchases(Map<String, PurchaseInfo> purchases) {
        LinkedHashMap<String, PurchaseInfo> sorted = purchases.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(PurchaseInfo::getAmount).reversed()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
        LinkedHashMap<String, PurchaseInfo> swappedKeys = swapKeys(sorted, "Milk", "Debt");
        return swappedKeys;
    }

    private static Map<String, Double> sortByType(Map<String, PurchaseInfo> purchases) {


        Map<String, Double> purchaseType = purchases
                .values()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                PurchaseInfo::getCategory,
                                Collectors.summingDouble(PurchaseInfo::getAmount)
                        )
                );

        Set<String> missingCategories = new HashSet<>(categories.values());
        Set<String> presentCategories = purchases.values().stream().map(value -> value.category).collect(Collectors.toSet());
        missingCategories.removeAll(presentCategories);
        missingCategories.forEach(category -> purchaseType.put(category, 0.0));

        Map<String, Double> purchaseFinal = purchaseType
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        return purchaseFinal;
    }

    private static Map<String, PurchaseInfo> sortCertainType(String category, Map<String, PurchaseInfo> purchases) {

        return purchases
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().category.equals(category))
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(PurchaseInfo::getAmount).reversed()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    private static void addPurchase(Scanner scanner, double balance, Map<String, PurchaseInfo> purchaseInfoMap) {
        while (true) {
            showPurchaseMenu();
            String input = scanner.nextLine();
            if (!input.matches("\\d+")) {

                System.out.println();
                System.out.println("Invalid choice. Please choose a valid option.");
                System.out.println();

                continue;
            }
            int purchaseChoice = Integer.parseInt(input);


            if (purchaseChoice >= 1 && purchaseChoice <= 4) {
                System.out.println();
                System.out.println("Enter purchase name:");
                String purchaseName = scanner.nextLine();

                System.out.println("Enter its price:");
                String priceInput = scanner.nextLine();
                double purchasePrice;
                purchasePrice = Double.parseDouble(priceInput);

                if (balance >= purchasePrice) {
                    balance -= purchasePrice;

                    switch (purchaseChoice) {
                        case 1:
                            purchaseInfoMap.put(purchaseName, new PurchaseInfo(purchasePrice, foodCategory));
                            break;
                        case 2:
                            purchaseInfoMap.put(purchaseName, new PurchaseInfo(purchasePrice, clothesCategory));
                            break;
                        case 3:
                            purchaseInfoMap.put(purchaseName, new PurchaseInfo(purchasePrice, entertainmentCategory));
                            break;
                        case 4:
                            purchaseInfoMap.put(purchaseName, new PurchaseInfo(purchasePrice, otherCategory));
                            break;
                    }

                    System.out.println("Purchase was added!");
                    System.out.println();
                }

            } else if (purchaseChoice == 5) {
                System.out.println();
                break;

            } else {
                System.out.println();
                System.out.println("Invalid choice. Please choose a valid option.");
                System.out.println();
            }
        }
    }


    private static void showPurchases(Scanner scanner, Map<String, PurchaseInfo> purchases) {
        while (true) {
            System.out.println();
            System.out.println("Choose the type of purchases:");
            System.out.println("1) Food");
            System.out.println("2) Clothes");
            System.out.println("3) Entertainment");
            System.out.println("4) Other");
            System.out.println("5) All");
            System.out.println("6) Back");

            int inputList = scanner.nextInt();
            scanner.nextLine();

            switch (inputList) {
                case 1:
                    printCategoryPurchases(foodCategory, purchases);
                    break;
                case 2:
                    printCategoryPurchases(clothesCategory, purchases);
                    break;
                case 3:
                    printCategoryPurchases(entertainmentCategory, purchases);
                    break;
                case 4:
                    printCategoryPurchases(otherCategory, purchases);
                    break;
                case 5:
                    printAllPurchases(purchases);
                    break;
                case 6:
                    System.out.println();
                    return;
                default:
                    System.out.println("Invalid choice. Please choose a valid option.");
                    System.out.println();
            }
        }
    }

    private static void printCategoryPurchases(String category, Map<String, PurchaseInfo> purchases) {
        Map<String, PurchaseInfo> categoryPurchases = purchases
                .entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue().category, category))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (categoryPurchases.isEmpty()) {
            System.out.println();
            System.out.println("The purchase list is empty!");

        } else {
            System.out.println();
            System.out.println(category + ":");
            for (Map.Entry<String, PurchaseInfo> entry : categoryPurchases.entrySet()) {
                System.out.println(entry.getKey() + " $" + entry.getValue().getFormattedAmount());
            }
            double total = categoryPurchases.values().stream()
                    .map(purchaseInfo -> purchaseInfo.amount)
                    .mapToDouble(Double::doubleValue).sum();
            System.out.println("Total sum: $" + total);

        }
    }

    private static void printAllPurchases(Map<String, PurchaseInfo> purchases) {
        System.out.println();
        double sum = 0;
        for (Map.Entry<String, PurchaseInfo> entry : purchases.entrySet()) {
            System.out.println(entry.getKey() + " $" + entry.getValue().getFormattedAmount());
            sum += entry.getValue().amount;
        }

        System.out.println("Total sum: $" + sum);
    }

    private static void printSortedByTypePurchases(String category, Map<String, PurchaseInfo> purchases) {
        System.out.println();
        System.out.println(category + ":");
        double sum = 0;
        for (Map.Entry<String, PurchaseInfo> entry : purchases.entrySet()) {
            System.out.println(entry.getKey() + " $" + entry.getValue().getFormattedAmount());
            sum += entry.getValue().amount;
        }

        System.out.println("Total sum: $" + sum);
    }

    private static void printExpensesByType(Map<String, Double> purchases) {
        System.out.println();
        System.out.println("Types:");
        double sum = 0;
        for (Map.Entry<String, Double> entry : purchases.entrySet()) {
            System.out.println(entry.getKey() + " - $" + String.format("%.2f", entry.getValue()));
            sum += entry.getValue();
        }
        System.out.println("Total sum: $" + sum);
    }


    private static void showMenu() {
        System.out.println("Choose your action:");
        System.out.println("1) Add income");
        System.out.println("2) Add purchase");
        System.out.println("3) Show list of purchases");
        System.out.println("4) Balance");
        System.out.println("5) Save");
        System.out.println("6) Load");
        System.out.println("7) Analyze (Sort)");
        System.out.println("0) Exit");
    }

    private static void showPurchaseMenu() {
        System.out.println("Choose the type of purchase:");
        menuPrints();
        System.out.println("5) Back");
    }

    private static void menuPrints() {
        System.out.println("1) Food");
        System.out.println("2) Clothes");
        System.out.println("3) Entertainment");
        System.out.println("4) Other");
    }

    private static void showPurchaseSortMenu() {
        System.out.println("Choose the type of purchase");
        menuPrints();
    }

    private static void showSortMenu() {
        System.out.println("How do you want to sort?:");
        System.out.println("1) Sort all purchases");
        System.out.println("2) Sort by type");
        System.out.println("3) Sort certain type");
        System.out.println("4) Back");
    }

    public static LinkedHashMap<String, PurchaseInfo> swapKeys(LinkedHashMap<String, PurchaseInfo> map, String key1, String key2) {
        if (map.containsKey(key1) && map.containsKey(key2)) {
            // Temporarily store the entries to be swapped
            PurchaseInfo temp1 = map.get(key1);
            PurchaseInfo temp2 = map.get(key2);

            LinkedHashMap<String, PurchaseInfo> tempMap = new LinkedHashMap<>();

            // Re-insert entries in the new order
            for (Map.Entry<String, PurchaseInfo> entry : map.entrySet()) {
                if (entry.getKey().equals(key1)) {
                    tempMap.put(key2, temp2);  // Swap key2 into key1's place
                } else if (entry.getKey().equals(key2)) {
                    tempMap.put(key1, temp1);  // Swap key1 into key2's place
                } else {
                    tempMap.put(entry.getKey(), entry.getValue());
                }
            }

            map.clear();
            map.putAll(tempMap);
            return map;
        }
        return map;
    }


}

class PurchaseInfo {
    Double amount;
    String category;

    PurchaseInfo(Double amount, String category) {
        this.amount = amount;
        this.category = category;
    }

    public String getFormattedAmount() {
        return String.format("%.2f", amount);
    }

    public Double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }
}
