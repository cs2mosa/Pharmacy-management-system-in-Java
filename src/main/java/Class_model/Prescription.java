/**
 * Represents a medical prescription issued by a pharmacist for a patient.
 */
package Class_model;

import java.util.HashSet;
import java.util.Set;

public class Prescription {
    public Prescription() {
    }

    private int ID;
    private String PatientName; // The name of the patient for whom the prescription is issued.
    private Pharmacist issuedBy; // The pharmacist who issued the prescription.
    private String status; // The status of the prescription (e.g., Pending, Expired, Filled).
    private Set<Item> items = new HashSet<>(); // The set of items included in the prescription.

    /**
     * Constructs a Prescription object with the specified patient name, issuing pharmacist, and items.
     * The initial status is set to "Pending".
     *
     * @param PatientName The name of the patient.
     * @param issuedBy The pharmacist who issued the prescription.
     * @param items The set of items included in the prescription.
     */
    public Prescription(int ID, String PatientName, Pharmacist issuedBy, Set<Item> items){
        this.ID = ID;
        this.PatientName = PatientName;
        this.issuedBy = issuedBy;
        this.items = items;
        status = "Pending";
    }

    /**
     * Sets ID for patient.
     * @param ID  The new ID of the patient;
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Gets ID for patient.
     * @return the ID of the patient.
     */
    public int getId(){
        return ID;
    }

    /**
     * Gets the current status of the prescription.
     *
     * @return The status of the prescription.
     */
    public String getStatus(){
        return status;
    }

    /**
     * Sets the status of the prescription.
     *
     * @param status The new status of the prescription.
     */
    public void setStatus(String status){
        this.status = status;
    }

    /**
     * Gets the name of the patient for whom the prescription is issued.
     *
     * @return The patient's name.
     */
    public String getPatientName() {
        return PatientName;
    }

    /**
     * Sets the name of the patient for whom the prescription is issued.
     *
     * @param patientName The new name of the patient.
     */
    public void setPatientName(String patientName) {
        PatientName = patientName;
    }

    /**
     * Gets the pharmacist who issued the prescription.
     *
     * @return The issuing pharmacist.
     */
    public Pharmacist getIssuedBy() {
        return issuedBy;
    }

    /**
     * Sets the pharmacist who issued the prescription.
     *
     * @param issuedBy The new issuing pharmacist.
     */
    public void setIssuedBy(Pharmacist issuedBy) {
        this.issuedBy = issuedBy;
    }

    /**
     * Gets the set of items included in the prescription.
     *
     * @return The set of items.
     */
    public Set<Item> getItems() {
        return items;
    }

    /**
     * Sets the set of items included in the prescription.
     *
     * @param items The new set of items.
     */
    public void setItems(Set<Item> items) {
        this.items = items;
    }

    /**
     * Adds an item to the prescription.
     *
     * @param item The item to be added.
     */
    public void Add_Item(Item item){
        items.add(item);
    }

    /**
     * Removes an item from the prescription.
     *
     * @param item The item to be removed.
     * @return True if the item was successfully removed, false otherwise.
     */
    public boolean Remove_Item(Item item){
        return items.remove(item);
    }
}
