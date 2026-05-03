package Service_Interfaces;
import java.util.ArrayList;
import java.util.List;

import Class_model.Item;
import Class_model.Admin;
/**
 * InventoryServiceInterface defines the contract for managing inventory operations.
 * It provides methods to add, remove, update, and retrieve items, as well as manage stock levels.
 */
abstract interface InventoryServiceInterface {

    /**
     * Adds a new item to the inventory.
     * @param item The item to be added.
     * @return 0 if the item is added successfully, -1 if the item already exists or is not authorized.
     */
    int AddNewItem(Item item);

    /**
     * Removes an item from the inventory by its name.
     * @param Itemname The name of the item to be removed.
     * @return 0 if the item is removed successfully, -1 if the item does not exist.
     */
    int RemoveItemByName(String Itemname);

    /**
     * Updates the details of an existing item in the inventory.
     * @param itemName The item with updated details.
     * @param value The new price of the item.(Positive only)
     * @return 0 if the item is updated successfully, -1 if the item does not exist.
     */
    int UpdateItemPrice(String itemName, float value);

    /**
     * Retrieves an item from the inventory by its name.
     * @param ItemName The name of the item to retrieve.
     * @return The item with the specified name, or null if not found.
     */
    Item GetItemByName(String ItemName);

    /**
     * Retrieves a list of items belonging to a specific category.
     * @param category The category of items to retrieve.
     * @return A list of items in the specified category.or null if not found.
     */
    List<Item> GetItemsByCategory(String category);

    /**
     * Updates the stock quantity of a specific item.
     * @param item The name of the item to update.
     * @param quantityChange The change in quantity (positive only).
     * @return 0 if the stock is updated successfully, -1 if the item does not exist.
     */
    int updateStock(String item, int quantityChange);

    /**
     * Retrieves a list of items that are low in stock.
     * @return A list of items with low stock levels.
     */
    List<String> getLowStockItems();

    /**
     * Retrieves a list of items that have expired.
     * @return A list of expired items.
     */
    //void RemoveExpiredItems(); to be added later.
    /**
     * Retrieves all items from the repository.
     * @return A list of all items.
     */
    List<Item> GetAllItems();
}

public class Inventory_service implements InventoryServiceInterface{

    /**
     * singleton design for less memory usage, only 1 object is needed.
     */
    private static Inventory_service instance;

    private Inventory_service() {
        // private constructor to prevent instantiation
    }

    public static Inventory_service getInstance() {
        if (instance == null) {
            instance = new Inventory_service();
        }
        return instance;
    }
    //works fine
    @Override
    public int AddNewItem(Item item){
        try{
            if(/*Admin.authorizeItem(item) &&*/ Items_Repository.GetInstance().GetItemByName(item.getMedicName()) == null){
                Items_Repository.GetInstance().AddNewItem(item);
                return 0;
            }
            return -1; // Item already exists or not authorized.
        }catch (Exception e){
            System.out.println("error adding item: " + e.getMessage());
            return -1;
        }

    }
    //works fine
    @Override
    public int RemoveItemByName(String Itemname){
        try{
            if(Items_Repository.GetInstance().GetItemByName(Itemname) != null){
                Items_Repository.GetInstance().RemoveItemByName(Itemname);
                //other functionalites to be added.
                return 0;
            }else{
                return -1; // Item not found.
            }
        } catch (Exception e) {
            System.out.println("error removing item: " + e.getMessage());
            return -1;
        }

    }
    //works fine 
    @Override
    public int UpdateItemPrice(String itemName, float value){
        if(Items_Repository.GetInstance().GetItemByName(itemName)!= null){
            Items_Repository.GetInstance().GetItemByName(itemName).setPrice(value);
            return 0;
        }
        return -1; // Item not found.
    }
    
    //works fine
    @Override
    public Item GetItemByName(String ItemName){
        //other functionalities to be added here.
        if(ItemName == null) return null;
        return Items_Repository.GetInstance().GetItemByName(ItemName);
    }


    @Override
    public List<Item> GetItemsByCategory(String category){
        //other functionalities to be added here.
        return Items_Repository.GetInstance().GetItemsByCategory(category);
    }

    //works fine
    @Override
    public int updateStock(String item, int quantity){
        var it = Items_Repository.GetInstance().GetItemByName(item);
        if (it != null && it.getMedicineId() > 0 && quantity >= 0) {
            if (Items_Repository.GetInstance().patchStock(it.getMedicineId(), quantity)) {
                it.setQuantity(quantity);
                return 0;
            }
        }
        return -1;
    }    

    //works fine
    @Override
    public List<String> getLowStockItems(){
        List<Item> temp = Items_Repository.GetInstance().GetAllItems();
        if(temp == null) return null;
        List<String> lowstocks = new ArrayList<>();
        if(!temp.isEmpty()){
            for(Item item : temp){
                //5 is the bare minimum for an item (for now).
                if(item.getQuantity() <= 5){
                    lowstocks.add(item.getMedicName());
                }
            }
            return lowstocks;
        }else{
            return null; // No items in the inventory.
        }
    }

    @Override
    public List<Item> GetAllItems(){
        return Items_Repository.GetInstance().GetAllItems();
    }
}