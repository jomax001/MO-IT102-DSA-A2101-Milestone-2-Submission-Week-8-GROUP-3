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

    //  This is where our inventory data is stored on the computer. It's the address of our file cabinet!
    private static final String CSV_FILE_PATH = "C:\\Users\\Jomax\\OneDrive\\Documents\\NetBeansProjects\\InventoryManagement\\src\\MotorPH Inventory Data.csv";

    //  Okay, so this is like a form we fill out for each item in our inventory.
    private static class InventoryItem {
        String dateEntered;   // The date the item was added
        String stockLabel;    // A unique label for the item (like a name tag)
        String brand;         // The brand of the item (Honda, Yamaha, etc.)
        String engineNumber;  // The engine number - super important, it's like the item's fingerprint!
        String status;        // What's happening with the item? (Sold, On-hand, etc.)

        //  This is how we fill out the form when we get a new item.
        public InventoryItem(String dateEntered, String stockLabel, String brand, String engineNumber, String status) {
            this.dateEntered = dateEntered;
            this.stockLabel = stockLabel;
            this.brand = brand;
            this.engineNumber = engineNumber;
            this.status = status;
        }

        //  This is how we turn all the info on the form into a single line of text so we can save it in our file.
        @Override
        public String toString() {
            return dateEntered + "," + stockLabel + "," + brand + "," + engineNumber + "," + status;
        }
    }

    // This is a way to organize our items for quick searching. Think of it like a family tree for inventory!
    private static class TreeNode {
        InventoryItem item;      // The actual inventory item stored in this "branch"
        TreeNode left;         // "Branch" for items that come *before* this one (engine number)
        TreeNode right;        // "Branch" for items that come *after* this one (engine number)

        //  When we create a new "branch," we need to put an item in it.
        public TreeNode(InventoryItem item) {
            this.item = item;
            this.left = null;   // No items before yet
            this.right = null;  // No items after yet
        }
    }

    //  This is the very top of our inventory "family tree." It starts empty.
    private static TreeNode root = null;

   // This is like our quick-lookup table. If we know the engine number, we can find items fast!
    private static HashMap<String, List<InventoryItem>> inventoryMap = new HashMap<>();

    // Let's get started! This is the main part of our program.
    public static void main(String[] args) {
        // First, load all the items from our file into our system.
        loadInventoryFromCSV();

        Scanner scanner = new Scanner(System.in);
        String choice;

        // Main menu loop: keeps running until the user wants to exit.
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

            // Para siguradong tama yung pinili natin, magtatanong muna tayo.
            if (confirmChoice(scanner, choice)) {
                // What to do based on user input.
                switch (choice) {
                    case "1":
                        addItem(scanner);        // Add a new item
                        break;
                    case "2":
                        deleteItem(scanner);     // Remove an item
                        break;
                    case "3":
                        searchItemByEngineNumber(scanner);     // Search for an item
                        break;
                    case "4":
                        searchItemByBrand(scanner);
                        break;
                    case "5":
                        displaySortedInventory();  // Show the sorted inventory
                        break;
                    case "6":
                        System.out.println("Exiting...");  // Goodbye message
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again."); // If the user enters something wrong.
                }
            } else {
                System.out.println("Operation cancelled."); // Kinansela natin yung pinili natin.
            }
        } while (!choice.equals("6")); // Keep going until the user types "6."

        // Before exiting, save the changes we made back to our file.
        scanner.close();
    }

     // Para magtanong kung sigurado ba tayo sa pinili natin.
    private static boolean confirmChoice(Scanner scanner, String choice) {
        System.out.print("Confirm selection '" + choice + "'? (Yes/No): ");
        String confirmation = scanner.nextLine().trim().toLowerCase(); // Kunin yung sagot natin
        return confirmation.equals("yes"); // Ibalik kung "yes" yung sagot
    }

    // Load inventory from CSV file.
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

    // Save our inventory data back to the CSV file.
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

    // Add a new item to the inventory
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

        System.out.println("Item added successfully!");
    }

    // Remove an item from the inventory.
    private static void deleteItem(Scanner scanner) {
        System.out.print("Enter Engine Number to delete: ");
        String engineNumber = scanner.nextLine();

        if (!inventoryMap.containsKey(engineNumber)) {
            System.out.println("Item not found!");
            return;
        }

        if (confirmDelete(scanner, engineNumber)) {
            deleteFromBST(engineNumber);
            removeItemFromInventoryMap(engineNumber);
            saveInventoryToCSV();  // Save the updated inventory to CSV

            System.out.println("Item deleted successfully!");
            displayInventoryTable(); // Display the updated inventory table
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // Confirm Delete
    private static boolean confirmDelete(Scanner scanner, String engineNumber) {
        System.out.print("Are you sure you want to delete item with Engine Number " + engineNumber + "? (Yes/No): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        return confirmation.equals("yes");
    }

    // Search for an item by engine number.
    private static void searchItemByEngineNumber(Scanner scanner) {
        System.out.print("Enter Engine Number to search: ");
        String engineNumber = scanner.nextLine();

        //Get all Items from the inventory map.
        List<InventoryItem> items = inventoryMap.get(engineNumber);
        //Make sure there is an item for that Engine Number
        if(items != null && !items.isEmpty()){
            //Print out all the items that has the same engine number.
            System.out.println("Items found with Engine Number " + engineNumber + ":");
            System.out.println("-----------------------------------------------------------------------------------");
            System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
            System.out.println("-----------------------------------------------------------------------------------");
            for (InventoryItem item : items) {
                System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
            }
            System.out.println("-----------------------------------------------------------------------------------");
        }
        else{
            //Inform the user that the item they search for does not exist.
            System.out.println("Item not found!");
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
            System.out.println("No items found with that brand.");
        }

        System.out.println("-----------------------------------------------------------------------------------");
    }

    // Display the inventory, sorted by category and brand.
    private static void displaySortedInventory() {
        // Sort the inventory items first.
        List<InventoryItem> sortedList = getSortedInventory();

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
        System.out.println("-----------------------------------------------------------------------------------");

        for (InventoryItem item : sortedList) {
            System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
        }

        System.out.println("-----------------------------------------------------------------------------------");
    }

    //Ito yung method na ginagamit natin para ipakita yung inventory list
    private static void displayInventoryTable() {
        List<InventoryItem> inventoryList = new ArrayList<>(); // Gumawa tayo ng listahan
        inOrderTraversal(root, inventoryList); // Kunin natin yung items sa "family tree"

        //Ipakita natin yung header
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", "Date Entered", "Stock Label", "Brand", "Engine Number", "Status");
        System.out.println("-----------------------------------------------------------------------------------");

        //Ipakita natin yung bawat item sa listahan
        for (InventoryItem item : inventoryList) {
            System.out.printf("%-12s %-12s %-10s %-15s %-10s\n", item.dateEntered, item.stockLabel, item.brand, item.engineNumber, item.status);
        }

        System.out.println("-----------------------------------------------------------------------------------");
    }

    // --------------------------------------------------------------
    //  Helper Methods For Hash Map Implementation
    // --------------------------------------------------------------
    private static void addItemToInventoryMap(InventoryItem item) {
        //Check if the map already contains the engine number.
        if (!inventoryMap.containsKey(item.engineNumber)) {
            //If it doesnt, create a new list for that particular engine number.
            inventoryMap.put(item.engineNumber, new ArrayList<>());
        }
        //Get the list for the engine number, and add the item to the list.
        inventoryMap.get(item.engineNumber).add(item);
    }

    private static void removeItemFromInventoryMap(String engineNumber){
        //Remove Item from InventoryMap using the engineNumber.
        inventoryMap.remove(engineNumber);
    }

    // --------------------------------------------------------------
    //   Methods for BST Implemenation
    // --------------------------------------------------------------

    // Inserts a new item in our Binary Search Tree.
    private static void insertIntoBST(InventoryItem item) {
        root = insertIntoBSTRecursive(root, item);
    }

    // Implementation of insertion into BST, using recursion.
    private static TreeNode insertIntoBSTRecursive(TreeNode root, InventoryItem item) {
        //If the root is empty, then this becomes the root of our BST.
        if (root == null) {
            return new TreeNode(item);
        }

        //If the root is not empty, then get the comparision result using the engineNumber.
        int compareResult = item.engineNumber.compareTo(root.item.engineNumber);

        //If the new item is less than the current Root, then put the item on the left.
        if (compareResult < 0) {
            root.left = insertIntoBSTRecursive(root.left, item);
        }
        //If the new item is more than the current Root, then put the item on the right.
        else if (compareResult > 0) {
            root.right = insertIntoBSTRecursive(root.right, item);
        }
        return root;
    }

    // Deletes an item from the BST.
    private static void deleteFromBST(String engineNumber) {
        root = deleteFromBSTRecursive(root, engineNumber);
    }

    // Impelementation for deleteing from the BST. Uses recursion.
    private static TreeNode deleteFromBSTRecursive(TreeNode root, String engineNumber) {
        //Base Case, if the root is empty. Then we just return it.
        if (root == null) {
            return null;
        }

        //Find the item that we want to delete using comparision.
        int compareResult = engineNumber.compareTo(root.item.engineNumber);
        if (compareResult < 0) {
            root.left = deleteFromBSTRecursive(root.left, engineNumber);
        } else if (compareResult > 0) {
            root.right = deleteFromBSTRecursive(root.right, engineNumber);
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
            root.right = deleteFromBSTRecursive(root.right, root.item.engineNumber);
        }

        return root;
    }

    //Find the min Value in the Tree.
    private static InventoryItem minValue(TreeNode root) {
        InventoryItem minv = root.item;
        while (root.left != null) {
            minv = root.left.item;
            root = root.left;
        }
        return minv;
    }

    // Used for testing
    private static void inOrderTraversal(TreeNode node, List<InventoryItem> list) {
        if (node != null) {
            inOrderTraversal(node.left, list);
            list.add(node.item);
            inOrderTraversal(node.right, list);
        }
    }

    // --------------------------------------------------------------
    //   Methods for Merge Sort Implementation
    // --------------------------------------------------------------

    //Get Items from the HashMap,
    private static List<InventoryItem> getInventoryList() {
        //Create a new ArrayList of Inventory Items
        List<InventoryItem> inventoryList = new ArrayList<>();

        //Iterate through the values of inventory map and add to the arrayList.
        for (List<InventoryItem> items : inventoryMap.values()) {
            inventoryList.addAll(items);
        }

        //Return the Inventory List.
        return inventoryList;
    }

    //Use Merge Sort Algorithm for sorting Items
    private static List<InventoryItem> getSortedInventory() {
        //Get List of Inventory Items
        List<InventoryItem> inventoryList = getInventoryList();

        //Sort the array List and return it.
        mergeSort(inventoryList, 0, inventoryList.size() - 1);
        return inventoryList;
    }

    //Implementation of Merge Function
    private static void merge(List<InventoryItem> inventoryList, int left, int middle, int right) {
        //Get the sizes of the 2 sub arrayList.
        int n1 = middle - left + 1;
        int n2 = right - middle;

        //Create two sub ArrayLists
        List<InventoryItem> leftList = new ArrayList<>();
        List<InventoryItem> rightList = new ArrayList<>();

        //Add elements to the left list.
        for (int i = 0; i < n1; ++i) {
            leftList.add(inventoryList.get(left + i));
        }

        //Add elements to the right list.
        for (int j = 0; j < n2; ++j) {
            rightList.add(inventoryList.get(middle + 1 + j));
        }

        //Initial indexes of the sub arrayList
        int i = 0, j = 0;

        //The initial index of the merged sub arrayList
        int k = left;
        while (i < n1 && j < n2) {
            //Compare the engine numbers for sorting purposes.
            if (leftList.get(i).engineNumber.compareTo(rightList.get(j).engineNumber) <= 0) {
                inventoryList.set(k, leftList.get(i));
                i++;
            } else {
                inventoryList.set(k, rightList.get(j));
                j++;
            }
            k++;
        }

        //If there are any remaining elements, put it in the arrayList.
        while (i < n1) {
            inventoryList.set(k, leftList.get(i));
            i++;
            k++;
        }

        //If there are any remaining elements, put it in the arrayList.
        while (j < n2) {
            inventoryList.set(k, rightList.get(j));
            j++;
            k++;
        }
    }

    //Implementation of Merge Sort, using recursion.
    private static void mergeSort(List<InventoryItem> inventoryList, int left, int right) {
        //If the left index is less than the right, then sort the array.
        if (left < right) {
            // Find the middle point
            int middle = (left + right) / 2;

            // Sort first and second halves
            mergeSort(inventoryList, left, middle);
            mergeSort(inventoryList, middle + 1, right);

            // Merge the sorted halves
            merge(inventoryList, left, middle, right);
        }
    }
}
