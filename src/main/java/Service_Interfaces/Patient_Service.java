package Service_Interfaces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import Class_model.Patient;
import Class_model.Pharmacist;
import Class_model.Prescription;
import Class_model.Item;
import Class_model.Order;
import GUI.Date;

/**
 * This interface defines the contract for patient-related services.
 * It provides methods to add, remove, update, and retrieve patient information,
 * as well as manage patient balances and orders.
 */
abstract interface PatientServiceInterface {

    /**
     * Adds a new patient to the system.
     * @param patient The patient object to be added.
     * @return integer number represnets Patient id, -1 else
     */
    int AddPatient(Patient patient);

    /**
     * Removes a patient from the system by their name.
     * @param Patientname The name of the patient to be removed.
     * @return status code of 0 on success , and -1 else.
     */
    int RemovePatient(String Patientname);

    /**
     * Authenticate, then Update a specific attribute of a patient, can change Password too.
     * @param PatientName The name of the patient to be updated.
     * @param Password The password of the patient to be updated.
     * @param query The attribute to be updated.
     * @param value The new value for the specified attribute.
     * @return status code of patient id on success , and -1 else.
     */
    int UpdatePatient(String PatientName,String Password, String query, Object value);

    /**
     * Retrieves a patient by their name.
     * @param Patientname The name of the patient to retrieve.
     * @return The patient object corresponding to the given name.
     */
    Patient GetPatient(String Patientname);

    /**
     * Retrieves a list of all patients in the system.
     * @return A list of all patient objects.
     */
    List<Patient> GetAllPatients();

    /**
     * Retrieves the balance of a specific patient.
     * @param PatientName The name of the patient whose balance is to be retrieved.
     * @return The balance of the specified patient.
     */
    double GetPatientBalance(String PatientName);

    /**
     * Retrieves a list of orders associated with a specific patient.
     * @param PatientId The ID of the patient whose orders are to be retrieved.
     * @return A list of orders for the specified patient.
     */
    List<Order> GetPatientOrders(int PatientId);

    /**
     * Places an order with the specified items, issues a new prescription to that order.
     * 
     * @param items A map where the key is the item name and the value is the quantity.
     * @param PatientId The ID of the patient placing the order.
     * @param pharmacist The pharmacist handling the order.
     * @return The ID of the placed order if placed correctly, -1 else.
     */
    int PlaceOrder(Map<String, Integer> items, int PatientId, Pharmacist pharmacist);

    /**
     * Authenticates a patient based on their username and password.
     * @param PatientName  The username of the patient to authenticate.
     * @param Password The password of the patient to authenticate.
     * @return true if authentication is successful, false otherwise.
     */
    boolean AuthenticatePatient(String PatientName, String Password);

    /**
     * Places an order based on a prescription. you should validate the prescription first as this method doesnt hande invalid presciptions
     * @see Prescription_Service for more details about Checking validity.
     * @param PatientId The ID of the patient placing the order.
     * @param PreID The ID of the prescription.
     * @return The ID of the placed order if placed correctly, -1 else.
     */
    int PlaceOrderByPrescription(int PatientId, int PreID);
}

public class Patient_Service implements PatientServiceInterface {

    /**
     * singleton design for less memory usage, only 1 object is needed.
    */
    private static Patient_Service instance;

    private Patient_Service() {
        // private constructor to prevent instantiation
    }

    public static  Patient_Service getInstance() {
        if (instance == null) {
            instance = new Patient_Service();
        }
        return instance;
    }
    //works fine
    @Override
    public int AddPatient(Patient patient) {
        try {
            return Patient_Repository.getInstance().AddPatient(patient);
        } catch (Exception e) {
            System.err.println("Error adding patient: " + e.getMessage());
            return -1;
        }
    }
    //works fine
    @Override
    public int RemovePatient(String Patientname) {
        try {
            if (GetPatient(Patientname) == null)
                return -1;
            return Patient_Repository.getInstance().RemovePatient(GetPatient(Patientname).getID());
        } catch (Exception e) {
            System.err.println("Error removing patient: " + e.getMessage());
            return -1;
        }
    }
    //works fine
    @Override
    public Patient GetPatient(String Patientname) {
        try {
            if( Patientname == null) throw new IllegalArgumentException("patient name should be valid type of string");
            Set<Patient> temp = Patient_Repository.getInstance().GetAllPatients();
            if (temp == null || temp.isEmpty())
                return null;
            Iterator<Patient> value = temp.iterator();
            while (value.hasNext()) {
                Patient temppPatient = value.next();
                if (temppPatient.getUsername().equals(Patientname)) {
                    return temppPatient;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error retrieving patient: " + e.getMessage());
            return null;
        }
    }
    //works fine
    @Override
    public List<Patient> GetAllPatients() {
        try {
            return new ArrayList<>(Patient_Repository.getInstance().GetAllPatients());
        } catch (Exception e) {
            System.err.println("Error retrieving all patients: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    //works fine
    @Override
    public double GetPatientBalance(String PatientName) {
        try {
            Patient patient = GetPatient(PatientName);
            if (patient == null)
                return -1;
            return patient.GetBalance();
        } catch (Exception e) {
            System.err.println("Error retrieving patient balance: " + e.getMessage());
            return -1;
        }
    }
    //works fine
    @Override
    public List<Order> GetPatientOrders(int PatientId) {
        try {
            if (Patient_Repository.getInstance().GetPatient(PatientId) == null) {
                return null;
            }
            return Order_Repository.getInstance().GetOrdersForPatient(PatientId);
        } catch (Exception e) {
            System.err.println("Error retrieving patient orders: " + e.getMessage());
            return null;
        }
    }
    
    //works fine.
    @Override
    public int UpdatePatient(String PatientName, String Password, String query, Object value) throws IllegalArgumentException, IllegalStateException {
        Patient temp = GetPatient(PatientName);
        if (!AuthenticatePatient(PatientName, Password)) {
            return -1; //status code for invalid authentication.
        }
        if(!(value instanceof Integer || value instanceof Double || value instanceof Float || value instanceof String || value instanceof Boolean))
            throw new IllegalArgumentException("invalid value type.");//status code of invalid value type.
        try {
            switch (query) {
                case "username":
                    if (value instanceof String) {
                        temp.setUsername((String) value);
                        return Patient_Repository.getInstance().UpdatePatient(temp.getID(), temp);
                    }
                    throw new IllegalArgumentException("invalid value type.");
                case "age":
                    if (value instanceof Float) {
                        temp.setAge((Float) value);
                        return Patient_Repository.getInstance().UpdatePatient(temp.getID(), temp);
                    }
                    if (value instanceof Integer) {
                        temp.setAge((Integer) value);
                        return Patient_Repository.getInstance().UpdatePatient(temp.getID(), temp);
                    }
                    throw new IllegalArgumentException("invalid value type.");
                case "address":
                    if (value instanceof String) {
                        temp.setAddress((String) value);
                        return Patient_Repository.getInstance().UpdatePatient(temp.getID(), temp);
                    }
                    throw new IllegalArgumentException("invalid value type.");
                case "PatientBalance":
                    if (value instanceof Double) {
                        temp.SetBalance((Double) value);
                        return Patient_Repository.getInstance().UpdatePatient(temp.getID(), temp);
                    }
                    if (value instanceof Integer) {
                        temp.SetBalance((Integer) value);
                        return Patient_Repository.getInstance().UpdatePatient(temp.getID(), temp);
                    }
                    throw new IllegalArgumentException("invalid value type.");
                case "Password":
                    if (value instanceof String) {
                        temp.setPassword((String) value);
                        return Patient_Repository.getInstance().UpdatePatient(temp.getID(), temp);
                    }
                    throw new IllegalArgumentException("invalid value type.");
                case "email":
                    if (value instanceof String) {
                        temp.setUserEmail((String) value);
                        return Patient_Repository.getInstance().UpdatePatient(temp.getID(), temp);
                    }
                    throw new IllegalArgumentException("invalid value type.");
                case "active":
                    if (value instanceof Boolean) {
                        temp.setactive((Boolean) value);
                        return Patient_Repository.getInstance().UpdatePatient(temp.getID(), temp);
                    }
                    throw new IllegalArgumentException("invalid value type.");
                default:
                    throw new IllegalArgumentException("invalid value type."); //status code of invalid input type.
            }
        }
        catch (NullPointerException e) {
            //  handle exception
            System.out.println("error updating: query cannot be null");
            return -1;
        } 
        catch (Exception e) {
            // handle exception
            System.out.println("error updating: " + e.getMessage());
            return -1;
        }
        
    }

    //checked, works right 
    @Override
    public boolean AuthenticatePatient(String PatientName, String Password) {
        try {
            if (Password == null || Password.isEmpty()) {
                throw new IllegalArgumentException("Password cannot be null.");
            }
            var logged = User_Repository.GetInstance().apiLogin(PatientName, Password);
            return logged != null && "Patient".equalsIgnoreCase(logged.userKind);
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            return false;
        }
    }

    //checked , works fine./* should be tested*/
    @Override
    public int PlaceOrderByPrescription(int PatientId, int PreID) {
        try {
            Prescription temp1 = Prescription_Service.getInstance().getPreById(PreID);
            Patient temp2 = Patient_Repository.getInstance().GetPatient(PatientId);
            if (temp2 == null )throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
            if (temp1 == null )throw new IllegalArgumentException("Prescription not found, you should add Prescription first or check the id");
            if(temp1.getItems() ==null) throw new IllegalArgumentException("Prescription has no items, failed to fetch items");
            Order order = new Order.builder()
                    .setOrderItems(new ArrayList<>(temp1.getItems()))
                    .setOrderId(new Random().nextInt(50000))
                    .setStatus("Pending")
                    .setOrderDate(Date.getCurrentDateAsString())
                    .build();
            /* should be tested*/
            for(Item item : order.getOrderItems()) {
                if(Inventory_service.getInstance().GetItemByName(item.getMedicName()) == null) throw new IllegalArgumentException("item "+ item.getMedicName() +" has not been found, failed to place the order");
                if(Inventory_service.getInstance().GetItemByName(item.getMedicName()).getQuantity() < item.getQuantity()) throw new IllegalArgumentException("item has no sufficient quantity");
                Inventory_service.getInstance().updateStock(item.getMedicName(),  Inventory_service.getInstance().GetItemByName(item.getMedicName()).getQuantity() - item.getQuantity());
            }
            if (Order_Service.getInstance().AddOrder(PatientId, order) > 0) {
                temp2.Add_order(order);
                System.out.println("order added");
            }
            return order.getOrderId();
        } catch (Exception e) {
            System.err.println("Error placing order by prescription: " + e.getMessage());
            return -1;
        }
    }
    //works fine.
    @Override
    public int PlaceOrder(Map<String, Integer> items, int PatientId, Pharmacist pharmacist) {
        try {
            List<Item> tempitems = new ArrayList<>();
            Patient tempel = Patient_Repository.getInstance().GetPatient(PatientId);
            if (tempel == null)
                throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
            if (pharmacist == null)
                throw new IllegalArgumentException("Pharmacist not found, you should add Pharmacist first or check the id");
            if (items == null || items.isEmpty())
                throw new IllegalArgumentException("no items has been found, failed to place the order");
            Iterator<Map.Entry<String, Integer>> it = items.entrySet().iterator();
            double totalprice = 0.0;
            while (it.hasNext()) {
                Map.Entry<String, Integer> temp = it.next();
                Item tempitem = Inventory_service.getInstance().GetItemByName(temp.getKey());
                if (tempitem == null)
                    throw new IllegalArgumentException("item "+ temp.getKey() +" has not been found, failed to place the order");
                if (tempitem.getQuantity() < temp.getValue())
                    throw new IllegalArgumentException("item has no sufficient quantity");
                if (!pharmacist.is_safe(tempitem, tempel))
                    throw new IllegalArgumentException("item "+ temp.getKey() +" is not safe for the patient");
                
                tempitems.add(tempitem);
                Inventory_service.getInstance().updateStock(tempitem.getMedicName(), tempitem.getQuantity() - temp.getValue());
                totalprice += tempitem.getPrice() * temp.getValue();
            }
            Order order = new Order.builder()
                    .setOrderId(new Random().nextInt(50000))
                    .setStatus("Pending")
                    .setOrderItems(tempitems)
                    .setTotalPrice(totalprice)
                    .setOrderDate(Date.getCurrentDateAsString())
                    .build();

            if (Order_Repository.getInstance().AddOrder(PatientId, order) > 0 && Prescription_Service.getInstance().IssuePrescription(pharmacist, PatientId, order.getOrderId()) > 0) {
                tempel.Add_order(order);
                return order.getOrderId();
            } else {
                return -1;
            }
        } catch (Exception e) {
            System.err.println("Error placing order: " + e.getMessage());
            return -1;
        }
    }
}