package Service_Interfaces;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import Class_model.Item;

import Http.ApiModels;
import Http.BaseService;
import Http.PharmacyJsonMapper;

//item repo is a Set of Items, used Set for better complexity in searching.
/**
 * The ItemsRepository interface defines the contract for managing a collection of items.
 * It provides methods to add, remove, update, and retrieve items, as well as query items by category
 * and get the total number of items.
 */
abstract interface ItemsRepository {
    
    /**
     * retrives item based on its name.
     * @param ItemName
     * @return 
     */
    Item GetItemByName(String ItemName);
    /**
     * Adds a new item to the repository.
     * @param item The item to be added.
     */
    void AddNewItem(Item item);

    /**
     * Removes an item from the repository by its name.
     * @param Itemname The name of the item to be removed.
     */
    void RemoveItemByName(String Itemname);

    /**
     * Updates an existing item in the repository based on a query and a new value.
     * @param itemName name of the item to be updated
     * @param newItem  the updated item to register
     */
    void UpdateItem(String itemName, Item newItem);

    /**
     * Retrieves all items from the repository.
     * @return A list of all items.
     */
    List<Item> GetAllItems();

    /**
     * Gets the total number of items in the repository.
     * @return The number of items.
     */
    long GetNumberOfItems();

    /**
     * Retrieves a list of items that belong to a specific category.
     * @param category The category to filter items by.
     * @return A list of items in the specified category.
     */
    List<Item> GetItemsByCategory(String category);
}

class Items_Repository extends BaseService implements ItemsRepository{

    private static Items_Repository instance = null;

    private Items_Repository(){
    }
    
    public static Items_Repository GetInstance(){
        if(instance == null){
            instance = new Items_Repository();
        }
        return instance;
    }

    public boolean patchStock(int medicineId, int quantity) {
        var body = new LinkedHashMap<String, Object>();
        body.put("quantity", quantity);
        return patchJson("/api/medicines/" + medicineId + "/stock", body);
    }

    private static String toIsoDate(String displayOrIso) {
        if (displayOrIso == null || displayOrIso.isBlank()) {
            return LocalDate.now().toString();
        }
        try {
            return LocalDate.parse(displayOrIso).toString();
        } catch (DateTimeParseException ignored) {
        }
        try {
            var fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(displayOrIso.trim(), fmt).toString();
        } catch (DateTimeParseException ignored) {
        }
        return LocalDate.now().toString();
    }

    @Override
    public Item GetItemByName(String ItemName){
        if(ItemName == null) return null;
        var m = getJson("/api/medicines/by-name/" + enc(ItemName), ApiModels.MMedicine.class).orElse(null);
        return m == null ? null : PharmacyJsonMapper.toItem(m);
    }

    @Override
    public void AddNewItem(Item item){
        if(item == null || GetItemByName(item.getMedicName()) != null) {
            throw new IllegalArgumentException("Item already exists or null item");
        }
        var body = new LinkedHashMap<String, Object>();
        body.put("name", item.getMedicName());
        body.put("price", item.getPrice());
        body.put("category", item.getCategory());
        body.put("expiryDate", toIsoDate(item.getExpireDate()));
        body.put("stockQuantity", item.getQuantity());
        body.put("usageInstructions", item.getUsage());
        body.put("isRefundable", item.is_Refundable());
        body.put("sideEffects", item.getSideEffects() == null ? List.of() : new ArrayList<>(item.getSideEffects()));
        body.put("healingEffects", item.getHealingEffects() == null ? List.of() : new ArrayList<>(item.getHealingEffects()));
        postForInt("/api/medicines", body);
    }

    @Override
    public void RemoveItemByName(String Itemname){
        if(Itemname == null) throw new IllegalArgumentException("Item name cannot be null");
        Item temp = GetItemByName(Itemname);
        if(temp != null && temp.getMedicineId() > 0){
            delete("/api/medicines/" + temp.getMedicineId());
        }
    }

    @Override
    public void UpdateItem(String itemName, Item newItem){
        Item old = GetItemByName(itemName);
        if (old == null || old.getMedicineId() <= 0) {
            AddNewItem(newItem);
            return;
        }
        var body = new LinkedHashMap<String, Object>();
        body.put("name", newItem.getMedicName());
        body.put("price", newItem.getPrice());
        body.put("category", newItem.getCategory());
        body.put("expiryDate", toIsoDate(newItem.getExpireDate()));
        body.put("stockQuantity", newItem.getQuantity());
        body.put("usageInstructions", newItem.getUsage());
        body.put("isRefundable", newItem.is_Refundable());
        body.put("sideEffects", newItem.getSideEffects() == null ? List.of() : new ArrayList<>(newItem.getSideEffects()));
        body.put("healingEffects", newItem.getHealingEffects() == null ? List.of() : new ArrayList<>(newItem.getHealingEffects()));
        putJson("/api/medicines/" + old.getMedicineId(), body);
    }

    @Override
    public List<Item> GetAllItems(){
        Type t = BaseService.listOf(ApiModels.MMedicine.class);
        List<ApiModels.MMedicine> list = (List<ApiModels.MMedicine>) getJson("/api/medicines", t).orElse(List.of());
        List<Item> out = new ArrayList<>();
        for (var m : list) {
            out.add(PharmacyJsonMapper.toItem(m));
        }
        return out;
    }

    @Override
    public long GetNumberOfItems(){
        return GetAllItems().size();
    }

    @Override
    public List<Item> GetItemsByCategory(String category){
        Type t = BaseService.listOf(ApiModels.MMedicine.class);
        List<ApiModels.MMedicine> list = (List<ApiModels.MMedicine>) getJson("/api/medicines/by-category?category=" + enc(category), t).orElse(List.of());
        List<Item> out = new ArrayList<>();
        for (var m : list) {
            out.add(PharmacyJsonMapper.toItem(m));
        }
        return out;
    }
}