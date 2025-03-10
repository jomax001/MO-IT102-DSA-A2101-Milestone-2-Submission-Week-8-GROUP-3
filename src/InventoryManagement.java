/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jomax
 */
import java.io.*;
import java.util.*;

public class InventoryManagement {

    private static final String CSV_FILE_PATH = "C:\\Users\\Jomax\\OneDrive\\Documents\\NetBeansProjects\\InventoryManagement\\src\\MotorPH Inventory Data.csv";

    private static class InventoryItem {
        String dateEntered;
        String stockLabel;
        String brand;
        String engineNumber;
        String status;

        public InventoryItem(String dateEntered, String stockLabel, String brand, String engineNumber, String status) {
            this.dateEntered = dateEntered;
            this.stockLabel = stockLabel;
            this.brand = brand;
            this.engineNumber = engineNumber;
            this.status = status;
        }

        @Override
        public String toString() {
            return dateEntered + "," + stockLabel + "," + brand + "," + engineNumber + "," + status;
        }
    }

    private static class TreeNode {
        InventoryItem item;
        TreeNode left;
        TreeNode right;

        public TreeNode(InventoryItem item) {
            this.item = item;
            this.left = null;
            this.right = null;
        }
    }

    private static TreeNode root = null;
    private static HashMap<String, List<InventoryItem>> inventoryMap = new HashMap<>();

    public static void main(String[] args) {
        loadInventoryFromCSV();

        Scanner scanner = new Scanner(System.in);
        String choice;

        do {
            System.out.println("\nInventory Management System");
            System.out.println("1. Add Item");
            System.out.println("2. Delete Item");
            System.out.println("3. Search Item by Engine Number");
            System.out.println("4. Search Item by Brand");
            System.out.println("5. Display Sorted Inventory");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextLine();

            if (confirmChoice(scanner, choice)) {
                switch (choice) {
                    case "1":
                        addItem(scanner);
                        break;
                    case "2":
                        deleteItem(scanner);
                        break;
                    case "3":
                        searchItemByEngineNumber(scanner);
                        break;
                    case "4":
                        searchItemByBrand(scanner);
                        break;
                    case "5":
                        displaySortedInventory();
                        break;
                    case "6":
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } else {
                System.out.println("Operation cancelled.");
            }
        } while (!choice.equals("6"));

        scanner.close();
    }

    private static boolean confirmChoice(Scanner scanner, String choice) {
        System.out.print("Confirm selection '" + choice + "'? (Yes/No): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        return confirmation.equals("yes");
    }

    private static void loadInventoryFromCSV() {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) {
                    InventoryItem item = new InventoryItem(data[0], data[1], data[2], data[3], data[4]);
                    insertIntoBST(item);
                    addItemToInventoryMap(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveInventoryToCSV() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
            bw.write("Date Entered,Stock Label,Brand,Engine Number,Status");
            bw.newLine();

            List<InventoryItem> sortedList = getSortedInventory();

            for (InventoryItem item : sortedList) {
                bw.write(item.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addItem(Scanner scanner) {
        System.out.println("Enter details for the new item:");
        System.out.print("Date Entered: ");
        String dateEntered = scanner.nextLine();
        System.out.print("Stock Label: ");
        String stockLabel = scanner.nextLine();
        System.out.print("Brand: ");
        String brand = scanner.nextLine();
        System.out.print("Engine Number: ");
        String engineNumber = scanner.nextLine();
        System.out.print("Status: ");
        String status = scanner.nextLine();

        InventoryItem newItem = new InventoryItem(dateEntered, stockLabel, brand, engineNumber, status);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH, true))) {
            writer.newLine();
            writer.write(newItem.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        insertIntoBST(newItem);
        addItemToInventoryMap(newItem);

        System.out.println("Item added successfully!\n");
        displayInventoryTable();
    }

    private static void deleteItem(Scanner scanner) {
        System.out.print("Enter Engine Number to delete: ");
        String engineNumber = scanner.nextLine();

        if (!inventoryMap.containsKey(engineNumber)) {
            System.out.println("Item not found!");
            return;
        }

        System.out.print("Confirm delete for engine " + engineNumber + "? (Yes/No): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (confirmation.equals("yes")) {
            deleteFromBST(engineNumber);
            removeItemFromInventoryMap(engineNumber);
            saveInventoryToCSV();
            System.out.println("Item deleted and CSV updated!\n");
            displayInventoryTable();
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void searchItemByEngineNumber(Scanner scanner) {
        System.out.print("Enter Engine Number to search: ");
        String engineNumber = scanner.nextLine();

        List<InventoryItem> items = inventoryMap.get(engineNumber);
        if (items != null && !items.isEmpty()) {
            System.out.println("Items found with Engine Number " + engineNumber + ":");
            System.out.println("-----------------------------------------------------------------------------------");
            System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
            System.out.println("-----------------------------------------------------------------------------------");
            for (InventoryItem item : items) {
                System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
            }
            System.out.println("-----------------------------------------------------------------------------------");
        } else {
            System.out.println("Item not found!");
        }
    }

    private static void searchItemByBrand(Scanner scanner) {
        System.out.print("Enter Brand to search: ");
        String brand = scanner.nextLine();
        boolean found = false;

        System.out.println("Items found with Brand " + brand + ":");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
        System.out.println("-----------------------------------------------------------------------------------");

        for (List<InventoryItem> items : inventoryMap.values()) {
            for (InventoryItem item : items) {
                if (item.brand.equalsIgnoreCase(brand)) {
                    System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("No items found with that brand.");
        }

        System.out.println("-----------------------------------------------------------------------------------");
    }

    private static void displaySortedInventory() {
        List<InventoryItem> sortedList = getSortedInventory();

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
        System.out.println("-----------------------------------------------------------------------------------");

        for (InventoryItem item : sortedList) {
            System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
        }

        System.out.println("-----------------------------------------------------------------------------------");
    }
    
    private static void displayInventoryTable() {
        List<InventoryItem> inventoryList = new ArrayList<>();
        inOrderTraversal(root, inventoryList);

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
        System.out.println("-----------------------------------------------------------------------------------");

        for (InventoryItem item : inventoryList) {
            System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
        }

        System.out.println("-----------------------------------------------------------------------------------");
    }

    private static void addItemToInventoryMap(InventoryItem item) {
        if (!inventoryMap.containsKey(item.engineNumber)) {
            inventoryMap.put(item.engineNumber, new ArrayList<>());
        }
        inventoryMap.get(item.engineNumber).add(item);
    }

    private static void removeItemFromInventoryMap(String engineNumber) {
        inventoryMap.remove(engineNumber);
    }

    private static void insertIntoBST(InventoryItem item) {
        root = insertIntoBSTRecursive(root, item);
    }

    private static TreeNode insertIntoBSTRecursive(TreeNode root, InventoryItem item) {
        if (root == null) {
            return new TreeNode(item);
        }

        int compareResult = item.engineNumber.compareTo(root.item.engineNumber);
        if (compareResult < 0) {
            root.left = insertIntoBSTRecursive(root.left, item);
        } else if (compareResult > 0) {
            root.right = insertIntoBSTRecursive(root.right, item);
        }
        return root;
    }

    private static void deleteFromBST(String engineNumber) {
        root = deleteFromBSTRecursive(root, engineNumber);
    }

    private static TreeNode deleteFromBSTRecursive(TreeNode root, String engineNumber) {
        if (root == null) {
            return null;
        }

        int compareResult = engineNumber.compareTo(root.item.engineNumber);
        if (compareResult < 0) {
            root.left = deleteFromBSTRecursive(root.left, engineNumber);
        } else if (compareResult > 0) {
            root.right = deleteFromBSTRecursive(root.right, engineNumber);
        } else {
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }

            root.item = minValue(root.right);
            root.right = deleteFromBSTRecursive(root.right, root.item.engineNumber);
        }

        return root;
    }

    private static InventoryItem minValue(TreeNode root) {
        InventoryItem minv = root.item;
        while (root.left != null) {
            minv = root.left.item;
            root = root.left;
        }
        return minv;
    }

    private static void inOrderTraversal(TreeNode node, List<InventoryItem> list) {
        if (node != null) {
            inOrderTraversal(node.left, list);
            list.add(node.item);
            inOrderTraversal(node.right, list);
        }
    }

    private static List<InventoryItem> getSortedInventory() {
        List<InventoryItem> sortedList = new ArrayList<>();
        inOrderTraversal(root, sortedList);
        return sortedList;
    }
}
