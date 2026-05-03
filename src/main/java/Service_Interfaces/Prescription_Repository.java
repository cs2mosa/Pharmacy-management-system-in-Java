package Service_Interfaces;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.OptionalInt;

import Class_model.*;

import Http.ApiModels;
import Http.BaseService;
import Http.PharmacyJsonMapper;

/**
 * PrescriptionRepository is an interface that defines the contract for managing prescriptions.
 * It provides methods to add, delete, and retrieve prescriptions based on specific criteria.
 */
abstract interface PrescriptionRepository {

    /**
     * Adds a new prescription to the repository.
     * @param prescription The Prescription object to be added.
     * @param userId The ID of the user associated with the prescription.
     * @return prescription id on success. -1 else.
     * @throws IllegalArgumentException if the prescription is null or if the user ID is invalid.
     */
    int Add(int userId, Prescription prescription) throws IllegalArgumentException;    

    /**
     * Deletes a prescription from the repository based on its unique ID.
     * @param ID The unique identifier of the prescription to be deleted.
     * @return status code of 0 on success , -1 else.
     * @throws IllegalArgumentException if the prescription ID is invalid or if the user ID is invalid.
     */
    int Delete(int userId, int ID) throws IllegalArgumentException;

    /**
     * Finds and retrieves a list of prescriptions associated with a specific patient's name.
     * @param patientName The name of the patient whose prescriptions are to be retrieved.
     * @return A list of prescriptions matching the given patient name. or null if not found.
     * @throws IllegalArgumentException if the patient name is null or empty.
     */
    List<Prescription> findByPatientID(int patientId) throws IllegalArgumentException;

    /**
     * Retrieves all prescriptions from the repository.
     * @return A list of all prescriptions. or null if not found.
     * @throws IllegalArgumentException if no prescriptions are found.
     */
    List<Prescription> findAll() throws IllegalArgumentException;

    /**
     * Retrieves a prescription by its unique ID.
     * @param preID The unique identifier of the prescription to be retrieved.
     * @return The Prescription object corresponding to the given ID, or null if not found.
     * @throws IllegalArgumentException if the prescription ID is invalid or if no prescriptions are found.
     */
    Prescription getPreById(int preID) throws IllegalArgumentException;
}
class Prescription_Repository extends BaseService implements PrescriptionRepository {

    private static Prescription_Repository instance = null;

    private Prescription_Repository(){
    }

    public static Prescription_Repository GetInstance(){
        if (instance == null) {
            instance = new Prescription_Repository();
        }
        return instance;
    }

    @Override
    public int Add(int userId, Prescription prescription)  throws IllegalArgumentException{
        if(Patient_Repository.getInstance().GetPatient(userId) == null) {
            throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
        }
        if (prescription == null) {
            throw new IllegalArgumentException("prescription cannot be null, check usage");
        }
        Integer pharmacistId = prescription.getIssuedBy() != null ? prescription.getIssuedBy().getID() : null;
        List<LinkedHashMap<String, Object>> lines = new ArrayList<>();
        for (Item it : prescription.getItems()) {
            if (it.getMedicineId() <= 0) {
                throw new IllegalArgumentException("Prescription items must include medicineId for API persistence.");
            }
            var line = new LinkedHashMap<String, Object>();
            line.put("medicineId", it.getMedicineId());
            line.put("prescribedQuantity", it.getQuantity());
            lines.add(line);
        }
        var body = new LinkedHashMap<String, Object>();
        body.put("patientId", userId);
        body.put("pharmacistId", pharmacistId);
        body.put("status", prescription.getStatus());
        body.put("issueDate", null);
        body.put("items", lines);
        OptionalInt id = postForInt("/api/prescriptions", body);
        if (id.isEmpty()) {
            return -1;
        }
        prescription.setID(id.getAsInt());
        return id.getAsInt();
    }

    @Override
    public int Delete(int userId, int ID) throws IllegalArgumentException {
        try {
            if(Patient_Repository.getInstance().GetPatient(userId) == null) {
                throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
            }
            return delete("/api/prescriptions/patient/" + userId + "/" + ID) ? 0 : -1;
        } catch (Exception e) {
            System.out.println("Error in deleting prescription: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public List<Prescription> findByPatientID(int patientId) {
        Type t = BaseService.listOf(ApiModels.MPrescription.class);
        List<ApiModels.MPrescription> list = getJson("/api/prescriptions/patient/" + patientId, t).orElse(List.of());
        return mapList(list);
    }

    @Override
    public List<Prescription> findAll()  throws IllegalArgumentException{
        try {
            Type t = BaseService.listOf(ApiModels.MPrescription.class);
            List<ApiModels.MPrescription> list = getJson("/api/prescriptions", t).orElse(List.of());
            if (list.isEmpty()) {
                throw new IllegalArgumentException("No prescriptions found.");
            }
            return mapList(list);
        } catch (Exception e) {
            System.out.println("Error in finding all prescriptions: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Prescription getPreById(int preID) throws IllegalArgumentException{
        try {
            var m = getJson("/api/prescriptions/" + preID, ApiModels.MPrescription.class).orElse(null);
            if (m == null) {
                return null;
            }
            String patientName = "";
            var pat = getJson("/api/patients/" + m.patientId, ApiModels.MPatient.class).orElse(null);
            if (pat != null) {
                patientName = pat.username;
            }
            Pharmacist ph = resolvePharmacist(m.pharmacistId);
            return PharmacyJsonMapper.toPrescription(m, ph, patientName);
        } catch (Exception e) {
            System.out.println("Error in finding prescription by ID: " + e.getMessage());
            return null;
        }
    }

    private List<Prescription> mapList(List<ApiModels.MPrescription> list) {
        List<Prescription> out = new ArrayList<>();
        for (var m : list) {
            String patientName = "";
            var pat = getJson("/api/patients/" + m.patientId, ApiModels.MPatient.class).orElse(null);
            if (pat != null) {
                patientName = pat.username;
            }
            out.add(PharmacyJsonMapper.toPrescription(m, resolvePharmacist(m.pharmacistId), patientName));
        }
        return out;
    }

    private static Pharmacist resolvePharmacist(Integer pharmacistUserId) {
        if (pharmacistUserId == null) {
            return new Pharmacist();
        }
        User u = User_Repository.GetInstance().GetByID(pharmacistUserId);
        if (u instanceof Pharmacist p) {
            return p;
        }
        return new Pharmacist();
    }
}