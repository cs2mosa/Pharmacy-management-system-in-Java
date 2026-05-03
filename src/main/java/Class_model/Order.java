/**
 * Represents an order in the system.
 * An order contains details such as order ID, order date, cashier who checked it,
 * total price, status, and a list of items in the order.
 * The status of the order can be "Pending", "Completed", or "Cancelled".
 * note: class needs exception handling for invalid inputs and edge cases.
 * The class provides methods to add and remove items from the order.
 */
package Class_model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an order with details such as items, total price, and status.
 */
public class Order {

    /** Unique identifier for the order. */
    private int orderId;

    /** For JSON / API hydration */
    public Order() {
    }

    /** Date when the order was placed. */
    private String orderDate;

    /** The cashier who checked the order. */
    private String checkedBy;

    /** Total price of the order. */
    private double totalPrice;

    /** Current status of the order ("Pending", "Completed", "Cancelled"). */
    private String status;

    /** List of items in the order. */
    private List<Item> orderItems = new ArrayList<>();

    /**
     * Constructs an Order with the given details.
     * @param orderId Unique identifier for the order.
     * @param orderDate Date of the order.
     * @param checkedBy Cashier who checked the order.
     * @param orderItems List of items in the order.
     * @param totalPrice Total price of the order.
     */
    private Order(builder builder) {
        this.orderId = builder.orderId;
        this.orderDate = builder.orderDate;
        this.checkedBy = builder.checkedBy;
        this.orderItems = builder.orderItems;
        this.totalPrice = builder.totalPrice;
        this.status = builder.status;
    }

    /** @return The unique identifier of the order. */
    public int getOrderId() {
        return orderId;
    }

    /** @return The date of the order. */
    public String getOrderDate() {
        return orderDate;
    }

    /** @return The cashier who checked the order. */
    public String getCheckedBy() {
        return checkedBy;
    }

    /** @return The list of items in the order. */
    public List<Item> getOrderItems() {
        return orderItems;
    }

    /** @return The total price of the order. */
    public double getTotalPrice() {
        return totalPrice;
    }

    /** @return The current status of the order. */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the unique identifier for the order.
     * @param orderId The new order ID.
     */
    private boolean is_set_id = false;
    public void setOrderId(int orderId) {
        if(!is_set_id){
            this.orderId = orderId;
            is_set_id = true;
        }
    }

    /** Assigns the id returned from the server after insert (overrides client-generated ids). */
    public void assignOrderIdFromApi(int id) {
        this.orderId = id;
        this.is_set_id = true;
    }

    /**
     * Sets the date of the order.
     * @param orderDate The new order date.
     */
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * Sets the cashier who checked the order.
     * @param checkedBy The new cashier.
     */
    public void setCheckedBy(String checkedBy) {
        this.checkedBy = checkedBy;
    }

    /**
     * Sets the list of items in the order.
     * @param orderItems The new list of items.
     */
    public void setOrderItems(List<Item> orderItems) {
        this.orderItems = orderItems;
        this.totalPrice = 0.0;
        for(Item item : orderItems){
            this.totalPrice += item.getPrice();
        }
    }

    /**
     * Sets the total price of the order.
     * @param totalPrice The new total price.
     */
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Sets the status of the order.
     * @param status The new status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Adds an item to the order and updates the total price.
     * @param item The item to add.
     */
    public void addItem(Item item) {
        orderItems.add(item);
        totalPrice += item.getPrice();
    }

    /**
     * Removes an item from the order and updates the total price.
     * @param item The item to remove.
     */
    public void removeItem(Item item) {
        if (orderItems.remove(item)) {
            totalPrice -= item.getPrice();
        }
    }

    /**
     * Clears all items from the order and resets the total price.
     */
    public void clearOrder() {
        orderItems.clear();
        totalPrice = 0.0;
    }

    /**
     * Marks the order as completed.
     */
    public void completeOrder() {
        status = "Completed";
    }

    /**
     * Cancels the order and updates its status.
     */
    public void cancelOrder() {
        status = "Cancelled";
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderDate='" + orderDate + '\'' +
                ", checkedBy='" + checkedBy + '\'' +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", orderItems=" + orderItems +
                '}';
    }
    /**
     * builder class
     */
    public static class builder{
        //data fields
        private int orderId;
        private String orderDate;
        private String checkedBy;
        private double totalPrice;
        private String status = "Pending"; //default value.
        private List<Item> orderItems = new ArrayList<>();

        //setters
        public builder setOrderId(int orderId) {
            this.orderId = orderId;
            return this;
        }
        public builder setOrderDate(String orderDate) {
            this.orderDate = orderDate;
            return this;
        }
        public builder setCheckedBy(String checkedBy) {
            this.checkedBy = checkedBy;
            return this;
        }
        public builder setOrderItems(List<Item> orderItems) {
            this.orderItems = orderItems;
            this.totalPrice = 0.0;
            for(Item item : orderItems){
                this.totalPrice += item.getPrice();
            }
            return this;
        }
        public builder setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }
        public builder setStatus(String status) {
            this.status = status;
            return this;
        }
        public Order build(){
            return new Order(this);
        }
    }
}
