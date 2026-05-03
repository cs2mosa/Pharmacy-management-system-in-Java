package Service_Interfaces;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import Class_model.Casher;
import Class_model.Pharmacist;
import Class_model.Role;
import Class_model.User;

import Http.ApiModels;
import Http.BaseService;
import Http.PharmacyJsonMapper;

/**
 * UserRepository is an interface that defines the contract for managing User entities.
 * It provides methods to add, update, delete, and retrieve users by their username or ID.
 */
abstract interface UserRepository {

    /**
     * Adds a new user to the repository.
     * @param user The User object to be added.
     * @return The unique identifier of the newly added user. -1 if the user already exists.
     * @throws IllegalArgumentException if the user is null or has invalid properties.
     */
    int Add(User user) throws IllegalArgumentException;

    /**
     * Updates an existing user in the repository.
     * @param user The User object with updated information.
     * @return The unique identifier of the updated user. -1 if the user does not exist.
     * @throws IllegalArgumentException if the user is null or has invalid properties.
     */
    int Update(User user) throws IllegalArgumentException;

    /**
     * Deletes a user from the repository.
     * @return 0 if the user was successfully deleted, -1 if the user does not exist.
     * @throws IllegalArgumentException if the user is null or has invalid properties.
     */
    int Delete(int UserId) throws IllegalArgumentException;

    /**
     * Retrieves a user by their username.
     * @param username The username of the user to retrieve.
     * @return The User object corresponding to the given username, or null if not found.
     * @throws IllegalArgumentException if the username is null or empty.
     */
    User GetByUsername(String username) throws IllegalArgumentException;

    /**
     * Retrieves a user by their unique ID.
     * @param ID The unique identifier of the user to retrieve.
     * @return The User object corresponding to the given ID, or null if not found.
     */
    User GetByID(int ID) ;
}

class User_Repository extends BaseService implements UserRepository {

    private static User_Repository instance = null;

    private User_Repository() {
    }

    public static User_Repository GetInstance(){
        if (instance == null) {
            instance = new User_Repository();
        }
        return instance;
    }

    /** Server-side credential check (password never returned on GET). */
    public ApiModels.MUser apiLogin(String username, String password) {
        var login = new ApiModels.MLogin();
        login.username = username;
        login.password = password;
        return postForJson("/api/users/login", login, ApiModels.MUser.class).orElse(null);
    }

    @Override
    public int Add(User user) throws IllegalArgumentException {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (user.getUsername() == null || user.getPassword() == null || user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be null nor empty");
        }
        if (!(user instanceof Pharmacist) && !(user instanceof Casher)) {
            return -1;
        }
        if (GetByUsername(user.getUsername()) != null) {
            return -1;
        }
        List<Integer> roleIds = new ArrayList<>();
        for (Role r : user.getRoles()) {
            int id = PharmacyJsonMapper.roleIdByName(this, gson, r.getRoleName());
            if (id > 0) {
                roleIds.add(id);
            }
        }
        String kind = user instanceof Pharmacist ? "Pharmacist" : "Casher";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", user.getUsername());
        body.put("password", user.getPassword());
        body.put("email", user.getUserEmail());
        body.put("phone", user.getPhoneNumber());
        body.put("userKind", kind);
        body.put("roleIds", roleIds);
        if (user instanceof Casher c) {
            body.put("salary", c.getSalary());
        } else if (user instanceof Pharmacist p) {
            body.put("salary", p.getSalary());
        } else {
            body.put("salary", 0);
        }
        body.put("jobType", kind);
        OptionalInt id = postForInt("/api/users", body);
        return id.isPresent() ? id.getAsInt() : -1;
    }

    @Override
    public int Delete(int UserId)  throws IllegalArgumentException{
        User user_indata = GetByID(UserId);
        if (user_indata == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return delete("/api/users/" + UserId) ? 0 : -1;
    }

    @Override
    public int Update(User Newuser)  throws IllegalArgumentException{
        Map<String, Object> patch = new LinkedHashMap<>();
        patch.put("username", Newuser.getUsername());
        patch.put("password", Newuser.getPassword());
        patch.put("email", Newuser.getUserEmail());
        patch.put("phone", Newuser.getPhoneNumber());
        patch.put("isActive", Newuser.getactive());
        return putJson("/api/users/" + Newuser.getID(), patch) ? Newuser.getID() : -1;
    }

    @Override
    public User GetByUsername(String username)  throws IllegalArgumentException{
        if(username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null nor empty");
        }
        var m = getJson("/api/users/by-username?username=" + enc(username), ApiModels.MUser.class).orElse(null);
        if (m == null) {
            return null;
        }
        return PharmacyJsonMapper.toUser(gson, this, m, "");
    }

    @Override
    public User GetByID(int ID) {
        var m = getJson("/api/users/" + ID, ApiModels.MUser.class).orElse(null);
        if (m == null) {
            return null;
        }
        return PharmacyJsonMapper.toUser(gson, this, m, "");
    }

}