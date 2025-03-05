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

    private static final String CSV_FILE_PATH = "C:\\Users\\Jomax\\OneDrive\\Documents\\NetBeansProjects\\InventoryManagement\\MotorPH Inventory Data.csv"; // Path to your CSV file

    //Inner class representing an Inventory Item
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

    private static TreeNode root = null; // Root of the Binary Search Tree

    private static HashMap<String, InventoryItem> inventoryMap = new HashMap<>(); // Hash Map for inventory

    public static void main(String[] args) {
        loadInventoryFromCSV();

        Scanner scanner = new Scanner(System.in);
        String choice;

        do {
            System.out.println("\nInventory Management System");
            System.out.println("1. Add Item");
            System.out.println("2. Delete Item");
            System.out.println("3. Search Item");
            System.out.println("4. Display Sorted Inventory");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addItem(scanner);
                    break;
                case "2":
                    deleteItem(scanner);
                    break;
                case "3":
                    searchItem(scanner);
                    break;
                case "4":
                    displaySortedInventory();
                    break;
                case "5":
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (!choice.equals("5"));

        saveInventoryToCSV(); // Save changes to CSV before exiting
        scanner.close();
    }

    //Load Inventory Data from CSV file
    private static void loadInventoryFromCSV() {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) {
                    InventoryItem item = new InventoryItem(data[0], data[1], data[2], data[3], data[4]);
                    // Insert into BST
                    root = insertIntoBST(root, item);
                    // Put into HashMap
                    inventoryMap.put(item.engineNumber, item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Save inventory data to CSV file
    private static void saveInventoryToCSV() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
            bw.write("Date Entered,Stock Label,Brand,Engine Number,Status"); // Header
            bw.newLine();

            List<InventoryItem> sortedList = new ArrayList<>();
            inOrderTraversal(root, sortedList);

            for (InventoryItem item : sortedList) {
                bw.write(item.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Adding a new item to inventory
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

        // Insert into BST
        root = insertIntoBST(root, newItem);
        // Put into HashMap
        inventoryMap.put(newItem.engineNumber, newItem);

        System.out.println("Item added successfully!");
    }

    //Deleting an item from inventory
    private static void deleteItem(Scanner scanner) {
        System.out.print("Enter Engine Number to delete: ");
        String engineNumber = scanner.nextLine();

        // Delete from BST
        root = deleteFromBST(root, engineNumber);
        // Delete from HashMap
        inventoryMap.remove(engineNumber);

        System.out.println("Item deleted successfully!");
    }

    //Searching for an item in the inventory
    private static void searchItem(Scanner scanner) {
        System.out.print("Enter Engine Number to search: ");
        String engineNumber = scanner.nextLine();

        InventoryItem item = inventoryMap.get(engineNumber);
        if (item != null) {
            System.out.println("Item found:\n" + item);
        } else {
            System.out.println("Item not found!");
        }
    }

    //Displaying sorted inventory in a tabular format
    private static void displaySortedInventory() {
        List<InventoryItem> sortedList = new ArrayList<>();
        inOrderTraversal(root, sortedList);

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
        System.out.println("-----------------------------------------------------------------------------------");

        for (InventoryItem item : sortedList) {
            System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
        }

        System.out.println("-----------------------------------------------------------------------------------");
    }

    //Binary Search Tree Insertion
    private static TreeNode insertIntoBST(TreeNode root, InventoryItem item) {
        if (root == null) {
            return new TreeNode(item);
        }

        int compareResult = item.engineNumber.compareTo(root.item.engineNumber);
        if (compareResult < 0) {
            root.left = insertIntoBST(root.left, item);
        } else if (compareResult > 0) {
            root.right = insertIntoBST(root.right, item);
        }

        return root;
    }

    //Binary Search Tree Deletion
    private static TreeNode deleteFromBST(TreeNode root, String engineNumber) {
        if (root == null) {
            return null;
        }

        int compareResult = engineNumber.compareTo(root.item.engineNumber);
        if (compareResult < 0) {
            root.left = deleteFromBST(root.left, engineNumber);
        } else if (compareResult > 0) {
            root.right = deleteFromBST(root.right, engineNumber);
        } else {
            // Node to be deleted found
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }

            // Node with two children: Get the inorder successor (smallest in the right subtree)
            root.item = minValue(root.right);

            // Delete the inorder successor
            root.right = deleteFromBST(root.right, root.item.engineNumber);
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

    //Inorder Traversal to get sorted list from BST
    private static void inOrderTraversal(TreeNode node, List<InventoryItem> list) {
        if (node != null) {
            inOrderTraversal(node.left, list);
            list.add(node.item);
            inOrderTraversal(node.right, list);
        }
    }
}
