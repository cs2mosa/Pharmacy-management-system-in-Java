package Service_Interfaces;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import Class_model.*;

/**
 * This interface defines the contract for managing prescriptions in the system.
 * It provides methods for adding, deleting, updating, retrieving, and filling prescriptions.and others
 */
abstract interface PrescriptionServiceInterface {

    /**
     * Adds a new prescription to the system.
     * @param UserId The ID of the user associated with the prescription.
     * @param prescription The Prescription object to be added.
     * @return The ID of the added prescription. -1 if the prescription is invalid.
     */
    int AddPrescription(int UserId, Prescription prescription);

    /**
     * Deletes a prescription from the system based on its ID.
     * @param UserId The ID of the user associated with the prescription.
     * @param preID The ID of the prescription to be deleted.
     * @return status code of 0 on success , -1 else.
     */
    int DeletePrescription(int UserId,int preID);

    /**
     * Updates a specific field of a prescription identified by its ID.
     * 
     * @param PreID The ID of the prescription to be updated.
     * @param query The name of the field to be updated, valid queries -> ("status" , "Items").
     * @param value The new value to be set for the specified field.
     * @return The ID of the updated prescription. -1 if the prescription is invalid.
     */
    int UpdatePrescription(int PreID, String query, Object value);

    /**
     * Retrieves a list of prescriptions associated with a specific patient ID.
     * 
     * @param patientName The ID of the patient whose prescriptions are to be retrieved.
     * @return A list of Prescription objects for the specified patient.
     */
    List<Prescription> GetPrescriptionsByName(String patientName);

    /**
     * Retrieves a list of all prescriptions in the system.
     * 
     * @return A list of all Prescription objects.
     */
    List<Prescription> GetAllPrescriptions();

    /**
     * Fills a prescription, marking it as completed or dispensed.
     * @param preID The ID of the prescription to be filled.
     * @return The ID of the filled prescription. -1 if the prescription is invalid.
     */
    int FillPrescription(int preID);

    /**
     * Issues a new prescription to a patient, it's ID is OrderId, associating it with a pharmacist.
     * @param pharmacist The pharmacist who is issuing the prescription.
     * @param patientId The ID of the patient to whom the prescription is issued.
     * @param OrderId The ID of the order associated with the prescription.
     * @return The ID of the issued prescription. -1 else.
     */
    int IssuePrescription(Pharmacist pharmacist, int patientId ,int OrderId);

    /**
     * Retrieves a prescription by its unique ID.
     * @param preID The unique identifier of the prescription to be retrieved.
     * @return The Prescription object corresponding to the given ID, or null if not found.
     */
     Prescription getPreById(int preID);

    /**
     * Checks the validity of a prescription.
     * @param prescriptionId The ID of the prescription to check.
     * @param pharmacist The pharmacist who is checking the prescription.
     * @return True if the prescription is valid, false otherwise.
     */
    boolean CheckPrescriptionValidity(int prescriptionId, Pharmacist pharmacist);
}

public class Prescription_Service implements PrescriptionServiceInterface {

    /**
     * singleton design for less memory usage, only 1 object is needed.
     */
    private static Prescription_Service instance = null;
    
    private Prescription_Service(){

    }
    public static Prescription_Service getInstance(){
        if(instance == null){
            instance = new Prescription_Service();
        }
        return instance;
    }

    //works fine
    @Override
    public int AddPrescription(int UserId, Prescription prescription) {
        // Implementation for adding a prescription
        try {
            if (prescription == null) throw new IllegalArgumentException("Prescription cannot be null, check usage");
            if (prescription.getIssuedBy() == null) throw new IllegalArgumentException("Pharmacist cannot be null, check usage");
            if (prescription.getPatientName() == null) throw new IllegalArgumentException("Patient name cannot be null, check usage");
            if (prescription.getItems() == null || prescription.getItems().isEmpty()) throw new IllegalArgumentException("Items cannot be null or empty, check usage");
            return Prescription_Repository.GetInstance().Add(UserId, prescription);
        } catch (Exception e) {
            // handle exception
            System.out.println("Error in adding prescription: " + e.getMessage());
            return -1; // Return -1 to indicate failure
        }
    }
    //works fine
    @Override
    public int DeletePrescription(int UserId, int preID) {
        // Implementation for deleting a prescription
        try {
            return Prescription_Repository.GetInstance().Delete(UserId, preID);
        } catch (Exception e) {
            // handle exception
            System.out.println("Error in deleting prescription: " + e.getMessage());
            return -1; // Return -1 to indicate failure
        }
    }
    //works fine.
    @Override
    public int UpdatePrescription(int PreID, String query, Object value) {
        // Implementation for updating a prescription
        Prescription prescription = Prescription_Repository.GetInstance().getPreById(PreID);
        try {
            if (prescription == null) throw new IllegalArgumentException("Prescription not found, check the ID.");
            if (query == null || query.isEmpty()) throw new IllegalArgumentException("Query cannot be null or empty, check usage."); 
            switch (query) {
                case "status":
                    if(value instanceof String){
                        prescription.setStatus((String)value);
                    }else{
                        throw new IllegalArgumentException("Invalid value type for status, expected String.");
                    }
                    break;
                case "Items":
                    if(value instanceof Set<?>) {
                        @SuppressWarnings("unchecked")//suppressing warning as the object will always be a set of items.
                        Set<Item> items = new HashSet<>((Set<Item>) value);
                        prescription.setItems(items);
                    }else{
                        throw new IllegalArgumentException("Invalid value type for Items, expected Set<Item>.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid query, check usage.");
            }
            return prescription.getId();
        } catch (Exception e) {
            // handle exception
            System.out.println("Error in updating prescription: " + e.getMessage());
            return -1; // Return -1 to indicate failure
        }
    }
    //works fine
    @Override
    public Prescription getPreById(int preID){
        //adding more freatures here.
        return Prescription_Repository.GetInstance().getPreById(preID);
    }
    //works fine.
    @Override
    public List<Prescription> GetPrescriptionsByName(String patientName) {
        // Implementation for retrieving prescriptions by patient ID
        try {
            if (patientName == null || patientName.isEmpty()) throw new IllegalArgumentException("Patient name cannot be null or empty, check usage");
            if (Patient_Service.getInstance().GetPatient(patientName) == null) throw new IllegalArgumentException("Patient not found, check the name.");
            return Prescription_Repository.GetInstance().findByPatientID(Patient_Service.getInstance().GetPatient(patientName).getID());
        } catch (Exception e) {
            // handle exception
            System.out.println("Error in retrieving prescriptions by patient name: " + e.getMessage());
            return new ArrayList<>(); // Return null to indicate failure
        }
            
    }
    //works fine.
    @Override
    public List<Prescription> GetAllPrescriptions() {
        // Implementation for retrieving all prescriptions
        return Prescription_Repository.GetInstance().findAll();
    }
    //works fine.
    @Override
    public int FillPrescription(int preID) {
        // Implementation for filling a prescription
        try {
            Prescription prescription = Prescription_Repository.GetInstance().getPreById(preID);
            if (prescription == null) throw new IllegalArgumentException("Prescription not found, check the ID.");
            if (prescription.getStatus().equals("Filled")) throw new IllegalArgumentException("Prescription already filled, check the ID.");
            if (prescription.getStatus().equals("Pending")) {
                prescription.setStatus("Filled");
                return preID; // Return the ID of the filled prescription
            } else {
                throw new IllegalArgumentException("Invalid status, check the ID.");
            }

        } catch (Exception e) {
            // handle exception
            System.out.println("Error in filling prescription: " + e.getMessage());
            return -1; // Return -1 to indicate failure
        }
    }
    //works fine.
    @Override
    public int IssuePrescription(Pharmacist pharmacist, int patientId ,int OrderId){
        //implementation     
        try {
            Order temp = Order_Repository.getInstance().GetById(OrderId);
            Patient tempPatient = Patient_Repository.getInstance().GetPatient(patientId);
            if (pharmacist == null) throw new IllegalArgumentException("Pharmacist cannot be null, check usage");
            if (temp == null) throw new IllegalArgumentException("Order not found, check the ID.");
            if (tempPatient == null) throw new IllegalArgumentException("Patient not found, check the ID.");
            List<Item> orderItemList = temp.getOrderItems();
            Set<Item> orderItems = new HashSet<>(orderItemList);
            for(Item item : orderItemList){
                if(!pharmacist.is_safe(item, tempPatient)){
                    return -1;
                }
            }
            Prescription prescription = new Prescription(OrderId, tempPatient.getUsername(), pharmacist, orderItems);
            prescription.setStatus("Issued");
            return AddPrescription(patientId,prescription);
        } catch (Exception e) {
            // handle exception
            System.out.println("Error in issuing prescription: " + e.getMessage());
            return -1; // Return -1 to indicate failure
        }                              
    }
    //works fine
    @Override
    public boolean CheckPrescriptionValidity(int prescriptionId, Pharmacist pharmacist) {
        // Implementation for checking prescription validity
        try {
            Prescription prescription = Prescription_Repository.GetInstance().getPreById(prescriptionId);
            if (prescription == null) throw new IllegalArgumentException("Prescription not found, check the ID.");
            if (pharmacist == null) throw new IllegalArgumentException("Pharmacist cannot be null, check usage");
            if (prescription.getStatus().equals("Expired")) throw new IllegalArgumentException("Prescription expired, check the ID.");
            Patient temp = Patient_Service.getInstance().GetPatient(prescription.getPatientName());
            for(Item item : prescription.getItems()) {
                if(!pharmacist.is_safe(item, temp)){
                    return false;
                }
            }
            return true; // Return true to indicate valid prescription.
        } catch (Exception e) {
            //handle exception
            System.out.println("Error in checking prescription validity: " + e.getMessage());
            return false; // Return false to indicate failure
        }
    }
}