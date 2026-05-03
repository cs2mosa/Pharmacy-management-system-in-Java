package Service_Interfaces;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import java.lang.reflect.Type;

import Class_model.Patient;
import Class_model.Role;

import Http.ApiModels;
import Http.BaseService;
import Http.PharmacyJsonMapper;

/**
 * The PatientRepository interface defines the contract for managing patient records.
 * It provides methods to add, remove, update, retrieve, Get, and list patients.
 */
abstract interface PatientRepository {

    /**
     * Adds a new patient to the repository.
     * @param patient The Patient object to be added.
     * @return The unique ID of the added patient, or -1 if the addition failed.
     * @throws IllegalArgumentException if the patient is null or invalid.
     */
    int AddPatient(Patient patient) throws IllegalArgumentException;

    /**
     * Removes a patient from the repository based on their unique ID.
     * @param PatientID The unique identifier of the patient to be removed.
     * @return The status of the removal operation. -1 if failed, 0 if successful.
     * @throws IllegalArgumentException if the patient ID is invalid or not found.
     */
    int RemovePatient(int PatientID) throws IllegalArgumentException;

    /**
     * Updates a specific attribute of a patient in the repository.
     * @param PatientID The unique identifier of the patient to be updated.
     * @param Newpatient The patient to update.
     * @return The status of the update operation. -1 if failed, patient id if successful.
     * @throws IllegalArgumentException if the patient ID is invalid or the new patient is null.
     */
    int UpdatePatient(int PatientID, Patient Newpatient) throws IllegalArgumentException;

    /**
     * Retrieves a patient from the repository based on their unique ID.
     * @param PatientID The unique identifier of the patient to retrieve.
     * @return The Patient object corresponding to the given ID, or null if not found.
     */
    Patient GetPatient(int PatientID);

    /**
     * Retrieves a list of all patients in the repository.
     * @return A List containing all Patient objects in the repository.
     */
    Set<Patient> GetAllPatients();
}

class Patient_Repository extends BaseService implements PatientRepository {
    private static Patient_Repository instance = null;

    private Patient_Repository() {
    }

    public static Patient_Repository getInstance() {
        if (instance == null) {
            instance = new Patient_Repository();
        }
        return instance;
    }

    @Override
    public int AddPatient(Patient patient)  throws IllegalArgumentException{
        if(patient == null || !(patient instanceof Patient)) {
            throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
        }
        List<Integer> roleIds = new ArrayList<>();
        for (Role r : patient.getRoles()) {
            int id = PharmacyJsonMapper.roleIdByName(this, gson, r.getRoleName());
            if (id > 0) {
                roleIds.add(id);
            }
        }
        if (roleIds.isEmpty()) {
            int pid = PharmacyJsonMapper.roleIdByName(this, gson, "Patient");
            if (pid > 0) {
                roleIds.add(pid);
            }
        }
        List<String> allergies = patient.getAllergies() == null ? List.of() : new ArrayList<>(patient.getAllergies());
        var body = new LinkedHashMap<String, Object>();
        body.put("username", patient.getUsername());
        body.put("password", patient.getPassword());
        body.put("email", patient.getUserEmail());
        body.put("phone", patient.getPhoneNumber());
        body.put("roleIds", roleIds);
        body.put("age", patient.getAge());
        body.put("address", patient.getAddress());
        body.put("patientBalance", patient.GetBalance());
        body.put("allergies", allergies);
        OptionalInt id = postForInt("/api/patients", body);
        return id.isPresent() ? id.getAsInt() : -1;
    }

    @Override
    public int RemovePatient(int PatientID)  throws IllegalArgumentException{
        if(GetPatient(PatientID) == null) {
            throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
        }
        return delete("/api/patients/" + PatientID) ? 0 : -1;
    }

    @Override
    public int UpdatePatient(int PatientID, Patient Newpatient)  throws IllegalArgumentException{
        var body = new LinkedHashMap<String, Object>();
        body.put("username", Newpatient.getUsername());
        body.put("password", Newpatient.getPassword());
        body.put("email", Newpatient.getUserEmail());
        body.put("phone", Newpatient.getPhoneNumber());
        body.put("isActive", Newpatient.getactive());
        body.put("age", Newpatient.getAge());
        body.put("address", Newpatient.getAddress());
        body.put("patientBalance", Newpatient.GetBalance());
        return putJson("/api/patients/" + PatientID, body) ? PatientID : -1;
    }

    @Override
    public Patient GetPatient(int PatientID) {
        var m = getJson("/api/patients/" + PatientID, ApiModels.MPatient.class).orElse(null);
        if (m == null) {
            return null;
        }
        Patient p = PharmacyJsonMapper.toPatient(gson, this, m);
        p.setPassword("");
        p.getOrders().clear();
        p.getOrders().addAll(Order_Repository.getInstance().GetOrdersForPatient(PatientID));
        return p;
    }

    @Override
    public Set<Patient> GetAllPatients() {
        Type t = BaseService.listOf(ApiModels.MPatient.class);
        List<ApiModels.MPatient> list = getJson("/api/patients", t).orElse(List.of());
        Set<Patient> set = new HashSet<>();
        for (var mp : list) {
            Patient p = PharmacyJsonMapper.toPatient(gson, this, mp);
            p.setPassword("");
            set.add(p);
        }
        return set;
    }

}