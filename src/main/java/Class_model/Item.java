/**
 * The Item class represents a medical item with various attributes such as
 * name, expiration date, category, quantity, usage, price, side effects, 
 * healing effects, and refundability status.
 * 
 * This class provides getter and setter methods to access and modify the 
 * attributes of the item.
 * note:1. class need exeption handling for the setters and getters.
 *      2. implements builder design for more comprehensive structure. and easier for inistantiaing in main.java
 */
package Class_model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an item with details such as name, category, price, quantity, and effects.
 */
public class Item implements Comparable<Item> {
    /** Database medicine id when using the REST API; -1 if unknown. */
    private int medicineId = -1;

    // The quantity of the item in stock.
    private int quantity;

    // The price of the item.
    private double price;

    // The category of the item.
    private String category;

    // The name of the medicine.
    private String medic_name;

    // The expiration date of the item.
    private String expire_date;

    // The usage instructions for the item.
    private String usage;

    // Indicates whether the item is refundable.
    private boolean is_refundable;

    // The healing effects of the item.
    private Set<String> healing_effects = new HashSet<>();

    // The side effects of the item.
    private Set<String> side_effects = new HashSet<>();

    /**
     * Constructs an Item with the specified details.
     *
     * @param medic_name     The name of the medicine.
     * @param expire_date    The expiration date of the item.
     * @param category       The category of the item.
     * @param quantity       The quantity of the item.
     * @param usage          The usage instructions for the item.
     * @param price          The price of the item.
     * @param side_effects   The side effects of the item.
     * @param healing_effects The healing effects of the item.
     */
    /** Gson / persistence */
    public Item() {
    }

    private Item(builder builder) {
        this.medicineId = builder.medicineId;
        this.medic_name = builder.medic_name;
        this.expire_date = builder.expire_date;
        this.quantity = builder.quantity;
        this.usage = builder.usage;
        this.side_effects = builder.side_effects;
        this.price = builder.price;
        this.healing_effects = builder.healing_effects;
        this.category = builder.category;
        this.is_refundable = builder.is_refundable;
    }
    
    /**
     * Sets whether the item is refundable.
     *
     * @param refundable True if the item is refundable, false otherwise.
     */
    public void set_refundable(boolean refundable) {
        this.is_refundable = refundable;
    }

    /**
     * Checks if the item is refundable.
     *
     * @return True if the item is refundable, false otherwise.
     */
    public boolean is_Refundable() {
        return this.is_refundable;
    }

    /**
     * Gets the price of the item.
     *
     * @return The price of the item.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the name of the medicine.
     *
     * @return The name of the medicine.
     */
    public String getMedicName() {
        return medic_name;
    }

    /**
     * Gets the category of the item.
     *
     * @return The category of the item.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the expiration date of the item.
     *
     * @return The expiration date of the item.
     */
    public String getExpireDate() {
        return expire_date;
    }

    /**
     * Gets the quantity of the item.
     *
     * @return The quantity of the item.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the usage instructions for the item.
     *
     * @return The usage instructions for the item.
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Gets the side effects of the item.
     *
     * @return A set of side effects of the item.
     */
    public Set<String> getSideEffects() {
        return side_effects;
    }

    /**
     * Gets the healing effects of the item.
     *
     * @return A set of healing effects of the item.
     */
    public Set<String> getHealingEffects() {
        return healing_effects;
    }

    /**
     * Sets the name of the medicine.
     *
     * @param medic_name The name of the medicine.
     */
    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public void setMedicName(String medic_name) {
        this.medic_name = medic_name;
    }

    /**
     * Sets the price of the item.
     *
     * @param price The price of the item.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Sets the expiration date of the item.
     *
     * @param expire_date The expiration date of the item.
     */
    public void setExpireDate(String expire_date) {
        this.expire_date = expire_date;
    }

    /**
     * Sets the quantity of the item.
     *
     * @param quantity The quantity of the item.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Sets the usage instructions for the item.
     *
     * @param usage The usage instructions for the item.
     */
    public void setUsage(String usage) {
        this.usage = usage;
    }

    /**
     * Sets the side effects of the item.
     *
     * @param side_effects A set of side effects of the item.
     */
    public void setSideEffects(Set<String> side_effects) {
        this.side_effects = side_effects;
    }

    /**
     * Sets the healing effects of the item.
     *
     * @param healing_effects A set of healing effects of the item.
     */
    public void setHealingEffects(Set<String> healing_effects) {
        this.healing_effects = healing_effects;
    }

    /**
     * implements comparable class for sorting
     * @param item The category of the item.
     */
    @Override
    public int compareTo(Item item) {
        return this.medic_name.compareTo(item.medic_name);
    }

    @Override
    public String toString() {
        return "Item{" +
                "medicName='" + medic_name + '\'' +
                ", expireDate='" + expire_date + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", usage='" + usage + '\'' +
                ", is_refundable=" + is_refundable +
                ", healing_effects=" + healing_effects +
                ", side_effects=" + side_effects +
                '}';
    }

    /**
     * builder class
     */
    public static class builder{
        //data fields.
        private int medicineId = -1;
        private int quantity;
        private double price;
        private String category;
        private String medic_name;
        private String expire_date;
        private String usage;
        private boolean is_refundable;
        private Set<String> healing_effects = new HashSet<>();
        private Set<String> side_effects = new HashSet<>();

        //setters for builder class.
        public builder set_Refundable( boolean is_Refundable) {
            this.is_refundable = is_Refundable;
            return this;
        }
        public builder setMedicineId(int medicineId) {
            this.medicineId = medicineId;
            return this;
        }
        public builder setMedicName(String medic_name) {
            this.medic_name = medic_name;
            return this;
        }
        public builder setPrice(double price) {
            this.price = price;
            return this;
        }
        public builder setExpireDate(String expire_date) {
            this.expire_date = expire_date;
            return this;
        }
        public builder setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }
        public builder setUsage(String usage) {
            this.usage = usage;
            return this;
        }
        public builder setSideEffects(Set<String> side_effects) {
            this.side_effects = side_effects;
            return this;
        }
        public builder setHealingEffects(Set<String> healing_effects) {
            this.healing_effects = healing_effects;
            return this;
        }
        public builder setCategory(String category) {
            this.category = category;
            return this;
        }
        public Item build(){
            return new Item(this);
        }
    }
}