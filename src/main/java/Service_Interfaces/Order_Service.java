package Service_Interfaces;

import java.util.List;
import Class_model.Order;
import Class_model.Item;


/**
 * This interface defines the contract for order-related operations.
 * It provides methods for placing, deleting, updating, retrieving, and calculating orders.
 */
abstract interface OrderServiceInterface {

    /**
     * Adds a new order to the repository.
     * @param patientId the patient id to whom the order will be attached to.
     * @param order The order to be added.
     * @return order id on success , -1 else.
     */
    int AddOrder(int patientId, Order order); // Adds an order to the repository.

    /**
     * Deletes an order by its ID.
     * @param patientId The ID of the patient associated with the order.
     * @param orderId The ID of the order to be deleted.
     * @return 0 on success, -1 if order already not found.
     */
    int DeleteOrder(int patientId,int orderId); // can be void

    /**
     * Updates the items of an existing order.
     * 
     * @param orderId The ID of the order to be updated.
     * @param query A boolean flag where true represents adding an item and false represents removing an item.
     * @param item The item to be added or removed.
     * @return order id on success, -1 if order or item not found.
     */
    int UpdateOrderItems(int orderId, boolean query, Item item); // query(0) represents adding, query(1) represents removing

    /**
     * Retrieves an order by its ID.
     * 
     * @param orderId The ID of the order to retrieve.
     * @return The order with the specified ID.
     */
    Order GetById(int orderId);

    /**
     * Retrieves a list of orders placed by a specific customer.
     * 
     * @param CustomerName The name of the customer whose orders are to be retrieved.
     * @return A list of orders associated with the specified customer.
     */
    List<Order> GetByCustomer(String CustomerName);

    /**
     * Calculates the total income from a queue of orders.
     * 
     * @return The total income as a long value.
     */
    long CalcTotalIncome();

    /**
     * Handles the return of an item.
     * 
     * @param orderId The ID of the order containing the item to be returned.
     * @param ItemName The Name of the item to be returned.
     * @return order id on success, -1 if order or item not found.
     */
    int HandleReturn(int orderId, String ItemName);
}

public class Order_Service implements OrderServiceInterface {

    /**
     * singleton design for less memory usage, only 1 object is needed.
     */
    private static Order_Service instance;

    private Order_Service() {
        // private constructor to prevent instantiation
    }

    public static Order_Service getInstance() {
        if (instance == null) {
            instance = new Order_Service();
        }
        return instance;
    }

    //works fine
    @Override
    public int AddOrder(int patientId, Order order) {
        // Implementation for adding an order
        if(order == null) return -1;
        try {
            return Order_Repository.getInstance().AddOrder(patientId, order);
        } catch (Exception e) {
            // handle exception
            System.out.println("error adding :" + e.getMessage());
            return -1;
        }
    }

    //works fine. 
    @Override
    public int DeleteOrder(int patientId,int orderId){
        // Implementation for deleting an order
        try {
            return Order_Repository.getInstance().DeleteOrder(patientId, orderId);
        } catch (Exception e) {
            // handle exception
            System.out.println("error deleting :" + e.getMessage());
            return -1;
        }
    }

    //works fine
    @Override
    public int UpdateOrderItems(int orderId, boolean query, Item item) {
        // Implementation for updating order items
        if(Order_Repository.getInstance().GetById(orderId) == null) 
            throw new IllegalArgumentException("order not found, you should add order first or check the id");  // Order not found
        if(item == null) 
            throw new IllegalArgumentException("item cannot be null"); // Item not found
        if(query){
            GetById(orderId).addItem(item);
            return orderId; // 0 for adding
        }else{
            if(item.is_Refundable()){
                GetById(orderId).removeItem(item);
                return orderId;
            } else{
                return -1;
            }
        }
    }

    //works fine checking again
    @Override
    public Order GetById(int orderId) {
        // Implementation for retrieving an order by ID
        // other functionalities to be added here.
        return Order_Repository.getInstance().GetById(orderId);
    }

    //works fine
    @Override
    public List<Order> GetByCustomer(String CustomerName) {
        // Implementation for retrieving orders by customer name
        // other functionalities to be added here.
        try {
            return Order_Repository.getInstance().GetByName(CustomerName);
        } catch (Exception e) {
            //  handle exception
            System.out.println("error retriving orders: " + e.getMessage());
            return null;
        }
    }

    //works fine
    @Override
    public long CalcTotalIncome() {
        // Implementation for calculating total income from orders
        long x = 0;
        for(Order o : Order_Repository.getInstance().GetHistory()){
            if("Paid".equals(o.getStatus())){
                x += o.getTotalPrice();
            }
        }
        return x; 
    }

    //works fine
    @Override
    public int HandleReturn(int orderId, String ItemName){
        // Implementation for handling item returns
        Order order = Order_Repository.getInstance().GetById(orderId);
        if(order == null) return -1;
        if(Inventory_service.getInstance().GetItemByName(ItemName) == null) return -1;
        for(Item item : order.getOrderItems()){
            if(item == null) return -1;
            if(ItemName != null && ItemName.equals(item.getMedicName())){
                order.removeItem(item);
                break;
            }
        }
        order.setStatus("Returned" + ItemName);
        order.setTotalPrice(order.getTotalPrice() - Inventory_service.getInstance().GetItemByName(ItemName).getPrice());
        return orderId; // success
    
    }
}