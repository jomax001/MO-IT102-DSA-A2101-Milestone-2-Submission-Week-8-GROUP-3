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

    // This is the file path where our inventory is saved.
    // Make sure the file path is correct to avoid problems.
    private static final String CSV_FILE_PATH = "C:\\Users\\Jomax\\OneDrive\\Documents\\NetBeansProjects\\InventoryManagement\\src\\MotorPH Inventory Data.csv";

    // This is the class for each item in our inventory.
    private static class InventoryItem {
        String dateEntered; // When the item was added to the inventory
        String stockLabel; // Label of the stock
        String brand; // Brand of the item
        String engineNumber; // Engine number of the item, it's unique!
        String status; // The status of the item

        // Constructor to create a new item
        public InventoryItem(String dateEntered, String stockLabel, String brand, String engineNumber, String status) {
            this.dateEntered = dateEntered;
            this.stockLabel = stockLabel;
            this.brand = brand;
            this.engineNumber = engineNumber;
            this.status = status;
        }

        // To print the item in a nice format
        @Override
        public String toString() {
            return dateEntered + "," + stockLabel + "," + brand + "," + engineNumber + "," + status;
        }
    }

    // This is the class for our tree data structure.
    // Like a family tree, but for our inventory items
    private static class TreeNode {
        InventoryItem item; // The item itself
        TreeNode left, right; // The "children" on the left and right

        // Constructor to create a new "family member"
        public TreeNode(InventoryItem item) {
            this.item = item;
            this.left = null;
            this.right = null;
        }
    }

    // This is the root of our tree. Where we start.
    private static TreeNode root = null;
    // This is for quickly finding an item based on its engine number
    private static HashMap<String, List<InventoryItem>> inventoryMap = new HashMap<>();

    // This is our main method. The program starts here.
    public static void main(String[] args) {
        loadInventoryFromCSV(); // Load our inventory from the CSV file

        Scanner scanner = new Scanner(System.in); // To get input from the user
        String choice = ""; // Initialize choice

        do {
            // Let's show the menu
            System.out.println("\nInventory Management System");
            System.out.println("1. Add Item");
            System.out.println("2. Delete Item");
            System.out.println("3. Search Item by Engine Number");
            System.out.println("4. Search Item by Brand");
            System.out.println("5. Display Sorted Inventory");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextLine(); // Let's get the user's choice

            String finalChoice = choice;  // Store choice for confirmChoice method
            if (confirmChoice(scanner, finalChoice)) {  // Pass stored choice to confirmChoice
                switch (finalChoice) {
                    case "1":
                        addItem(scanner); // Let's add an item
                        break;
                    case "2":
                        deleteItem(scanner); // Let's delete an item
                        break;
                    case "3":
                        searchItemByEngineNumber(scanner); // Let's search for an item by engine number
                        break;
                    case "4":
                        searchItemByBrand(scanner); // Let's search for an item by brand
                        break;
                    case "5":
                        displaySortedInventory(); // Let's show the inventory, sorted
                        break;
                    case "6":
                        System.out.println("Exiting..."); // Let's exit
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again."); // Wrong choice!
                }
            } else {
                System.out.println("Operation cancelled.");
                if (finalChoice.equals("6")) {  // Check the stored choice here
                    choice = "";  // Reset choice to re-display main menu
                }
            }
        } while (!choice.equals("6")); // Repeat until the user exits

        scanner.close(); // Let's close the scanner
    }

    // This method asks the user to confirm their choice
    private static boolean confirmChoice(Scanner scanner, String choice) {
        System.out.print("Confirm selection '" + choice + "'? (Yes/No): ");
        String confirmation = scanner.nextLine().trim().toLowerCase(); // Get the user's answer
        return confirmation.equals("yes"); // If yes, then true!
    }

    // This method loads the inventory from the CSV file
    private static void loadInventoryFromCSV() {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            br.readLine(); // Skip the header line (column titles)
            while ((line = br.readLine()) != null) { // Read the file line by line
                String[] data = line.split(","); // Split each line into parts (separated by commas)
                if (data.length == 5) { // Make sure we have all the data we expect
                    InventoryItem item = new InventoryItem(data[0], data[1], data[2], data[3], data[4]); // Create a new item
                    insertIntoBST(item);
                    addItemToInventoryMap(item);  //Add it to our hashMap!
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // If something goes wrong, tell us!
        }
    }

    // This method saves the inventory to the CSV file
    private static void saveInventoryToCSV() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
            bw.write("Date Entered,Stock Label,Brand,Engine Number,Status"); // Write the header row
            bw.newLine(); // Start a new line

            //  Get the inventory list, sorted by category and brand.
            List<InventoryItem> sortedList = getSortedInventory();

            //  Write each inventory item to a new line in the CSV file.
            for (InventoryItem item : sortedList) {
                bw.write(item.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Report any errors.
        }
    }

    // This method adds an item to the inventory
    private static void addItem(Scanner scanner) {
        System.out.println("Enter details for the new item:");
        System.out.print("Date Entered: ");
        String dateEntered = scanner.nextLine(); // Let's get the date entered
        System.out.print("Stock Label: ");
        String stockLabel = scanner.nextLine(); // Let's get the stock label
        System.out.print("Brand: ");
        String brand = scanner.nextLine(); // Let's get the brand
        System.out.print("Engine Number: ");
        String engineNumber = scanner.nextLine(); // Let's get the engine number
        System.out.print("Status: ");
        String status = scanner.nextLine(); // Let's get the status

        InventoryItem newItem = new InventoryItem(dateEntered, stockLabel, brand, engineNumber, status); // Create the new item

        //Append directly to CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH, true))) {
            writer.newLine();
            writer.write(newItem.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        insertIntoBST(newItem);
        addItemToInventoryMap(newItem);

        System.out.println("Item added successfully!"); // Yay! Success!
    }

    // This method deletes an item from the inventory
    private static void deleteItem(Scanner scanner) {
        System.out.print("Enter Engine Number to delete: ");
        String engineNumber = scanner.nextLine(); // Let's get the engine number to delete

        if (!inventoryMap.containsKey(engineNumber)) {
            System.out.println("Item not found!"); // We can't find the item
            return;
        }

        if (confirmDelete(scanner, engineNumber)) {
            deleteFromBST(engineNumber);
            removeItemFromInventoryMap(engineNumber);
            saveInventoryToCSV();  // Save the updated inventory to CSV

            System.out.println("Item deleted successfully!"); // Yay! Success!
            displaySortedInventory(); // Display the updated inventory table
        } else {
            System.out.println("Deletion cancelled."); // Cancelled it turns out
        }
    }

    // This method confirms if the user is sure they want to delete the item
    private static boolean confirmDelete(Scanner scanner, String engineNumber) {
        System.out.print("Are you sure you want to delete item with Engine Number " + engineNumber + "? (Yes/No): ");
        String confirmation = scanner.nextLine().trim().toLowerCase(); // Get the user's answer
        return confirmation.equals("yes"); // If yes, then true!
    }

    // Search for an item by engine number.
    private static void searchItemByEngineNumber(Scanner scanner) {
        System.out.print("Enter Engine Number to search: ");
        String engineNumber = scanner.nextLine();
        List<InventoryItem> items = inventoryMap.get(engineNumber);

        if(items != null && !items.isEmpty()){
            System.out.println("Items found with Engine Number " + engineNumber + ":");
            System.out.println("-----------------------------------------------------------------------------------");
            System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
            System.out.println("-----------------------------------------------------------------------------------");
            for (InventoryItem item : items) {
                System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
            }
            System.out.println("-----------------------------------------------------------------------------------");
        } else{
            System.out.println("Item not found!"); // We can't find the item
        }
    }

    //New Method for Search by Brand
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
            System.out.println("No items found with that brand."); // We can't find the item
        }

        System.out.println("-----------------------------------------------------------------------------------");
    }

    // This method shows the inventory, sorted
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

    //This is the method we use to display the inventory list
    private static void displayInventoryTable() {
        List<InventoryItem> inventoryList = new ArrayList<>(); // Let's make a list
        inOrderTraversal(root, inventoryList); // Let's get the items from the "family tree"

        //Let's show the header
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
        System.out.println("-----------------------------------------------------------------------------------");

        //Let's show each item in the list
        for (InventoryItem item : inventoryList) {
            System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
        }

        System.out.println("-----------------------------------------------------------------------------------");
    }

    // --------------------------------------------------------------
    //  Helper Methods For Hash Map Implementation
    // --------------------------------------------------------------
    private static void addItemToInventoryMap(InventoryItem item) {
        if (!inventoryMap.containsKey(item.engineNumber)) {
            inventoryMap.put(item.engineNumber, new ArrayList<>());
        }
        inventoryMap.get(item.engineNumber).add(item);
    }

    private static void removeItemFromInventoryMap(String engineNumber){
        inventoryMap.remove(engineNumber);
    }

    // --------------------------------------------------------------
    //   Methods for BST Implementation
    // --------------------------------------------------------------

    private static void insertIntoBST(InventoryItem item) {
        root = insertIntoBSTRecursive(root, item);
    }

    private static TreeNode insertIntoBSTRecursive(TreeNode root, InventoryItem item) {
        if (root == null) {
            return new TreeNode(item);
        }

        if (item.engineNumber.compareTo(root.item.engineNumber) < 0) {
            root.left = insertIntoBSTRecursive(root.left, item);
        } else if (item.engineNumber.compareTo(root.item.engineNumber) > 0) {
            root.right = insertIntoBSTRecursive(root.right, item);
        }
        return root;
    }

    private static void deleteFromBST(String engineNumber) {
        root = deleteFromBSTRecursive(root, engineNumber);
    }

    private static TreeNode deleteFromBSTRecursive(TreeNode root, String engineNumber) {
        if (root == null) {
            return root;
        }

        if (engineNumber.compareTo(root.item.engineNumber) < 0) {
            root.left = deleteFromBSTRecursive(root.left, engineNumber);
        } else if (engineNumber.compareTo(root.item.engineNumber) > 0) {
            root.right = deleteFromBSTRecursive(root.right, engineNumber);
        } else {
            // Node with the engine number found
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }

            // Node with two children: Get the inorder successor
            root.item = minValue(root.right);

            // Delete the inorder successor
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

     private static void inOrderTraversal(TreeNode root, List<InventoryItem> inventoryList) {
        if (root != null) {
            inOrderTraversal(root.left, inventoryList);
            inventoryList.add(root.item);
            inOrderTraversal(root.right, inventoryList);
        }
    }

    // Helper method to get sorted inventory list
    private static List<InventoryItem> getSortedInventory() {
        List<InventoryItem> inventoryList = new ArrayList<>();
        inOrderTraversal(root, inventoryList); // Use inOrderTraversal to get items in sorted order
        return inventoryList;
    }
}
