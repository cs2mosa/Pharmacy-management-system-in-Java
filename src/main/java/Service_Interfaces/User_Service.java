package Service_Interfaces;

import Class_model.Casher;
import Class_model.Pharmacist;
import Class_model.User;

/**
 * UserServiceInterface defines the contract for user-related operations.
 * This interface provides methods for adding, updating, deleting, authenticating,
 * deactivating, activating users, changing passwords, and checking access permissions.
 * NOTE: Patient services are extending this service, please make sure to think about that.
 */
interface UserServiceInterface {

    /**
     * Adds a new user to the system, For every user except Patients
     * @param user The User object to be added.
     * @return The unique identifier of the newly added user. -1 if the user already exists.
     */
    int AddUser(User user);

    /**
     * Updates a specific field of a user in the system.
     * @param user The identifier of the user to be updated.
     * @param query The field to be updated.
     * @param value The new value for the specified field.
     * @return The unique identifier of the updated user.
     */
    int UpdateUser(String user, String query, Object value);

    /**
     * Deletes a user from the system.
     * @param user The identifier of the user to be deleted.
     * @return The unique identifier of the deleted user. -1 if the user does not exist.
     */
    int DeleteUser(String user);

    /**
     * Authenticates a user based on their credentials.
     * @param user The User object containing authentication details.
     * @return true if authentication is successful, false otherwise.
     */
    boolean AuthenticateUser(User user);
    
    /**
     * Retrieves a user by their username.
     * @param username The username of the user to retrieve.
     * @return The User object corresponding to the given username, or null if not found.
     */
    User GetByUsername(String username);

    /**
     * Retrieves a user by their unique ID.
     * @param ID The unique identifier of the user to retrieve.
     * @return The User object corresponding to the given ID, or null if not found.
     */
    User GetByID(int ID);
}

public class User_Service implements UserServiceInterface {

    /**
     * singleton design for less memory usage, only 1 object is needed.
     */
    private static User_Service instance;

    private User_Service() {
        // Private constructor to prevent instantiation
    }

    public static User_Service getInstance() {
        if (instance == null) {
            instance = new User_Service();
        }
        return instance;
    }
    //works fine
    @Override
    public int AddUser(User user) {
        // Implementation for adding a user
        try {
            if (user == null) throw new IllegalArgumentException("User cannot be null");
            if(user.getUsername() == null || user.getPassword() == null) throw new IllegalArgumentException("Username or password cannot be null");
            return User_Repository.GetInstance().Add(user);
        } catch (Exception e) {
            // handle exception
            System.out.println("Error adding user: " + e.getMessage());
            return -1; // Return -1 if an error occurs
        }
    }
    //works fine
    @Override
    public int DeleteUser(String user) {
        // Implementation for deleting a user
        try {
            User temp = User_Repository.GetInstance().GetByUsername(user);
            if(temp == null) throw new IllegalArgumentException("User not found");
            return User_Repository.GetInstance().Delete(temp.getID());
        } catch (Exception e) {
            //handle exception
            System.out.println("Error deleting user: " + e.getMessage());
            return -1; // Return -1 if an error occurs
        }
    }

    @Override
    public int UpdateUser(String user, String query, Object value) {
        // Implementation for updating a user
        try {
            User temp = User_Repository.GetInstance().GetByUsername(user);
            if(temp == null) throw new IllegalArgumentException("User not found");
            if(query== null || query.isEmpty()) throw new IllegalArgumentException("query cannot be null or empty");
            switch (query) {
                case "username":
                    if(value instanceof String && !((String)value).isEmpty()){
                        temp.setUsername((String)value);
                        return temp.getID();
                    }
                    throw new IllegalArgumentException("invalid value type. or value");
                case "password":
                    if(value instanceof String && !((String)value).isEmpty()){
                        temp.setPassword((String)value);
                        return temp.getID();
                    }
                    throw new IllegalArgumentException("invalid value type. or value");
                case "email":
                    if(value instanceof String && !((String)value).isEmpty()){
                        temp.setUserEmail((String)value);
                        return temp.getID();
                    }
                    throw new IllegalArgumentException("invalid value type. or value");
                case "phone":
                    if(value instanceof String && !((String)value).isEmpty()){
                        temp.setPhoneNumber((String)value);
                        return temp.getID();
                    }
                    throw new IllegalArgumentException("invalid value type. or value");
                case "activate":
                    if(value instanceof Boolean){
                        temp.setactive((Boolean)value);
                        return temp.getID();
                    }
                    throw new IllegalArgumentException("invalid value type. or value");
                default:
                    throw new IllegalArgumentException("invalid query type. or value");
            }
        } catch (Exception e) {
            //handle exception
            System.out.println("Error updating user: " + e.getMessage());
            return -1; // Return -1 if an error occurs
        }
    }
    
    //works fine
    @Override
    public User GetByUsername(String username) {
        // Implementation for getting a user by username
        try {
            return User_Repository.GetInstance().GetByUsername(username);
        } catch (Exception e) {
            // handle exception
            System.out.println("Error getting user by username: " + e.getMessage());
            return null; // Return null if an error occurs
        }
    }

    //works fine
    @Override
    public User GetByID(int ID) {
        // Implementation for getting a user by ID
        try {
            return User_Repository.GetInstance().GetByID(ID);
        } catch (Exception e) {
            // handle exception
            System.out.println("Error getting user by ID: " + e.getMessage());
            return null; // Return null if an error occurs
        }
    }
    //works fine.
    @Override
    public boolean AuthenticateUser(User user) {
        try {
            if (user == null || user.getUsername() == null || user.getPassword() == null) {
                throw new IllegalArgumentException("Username or password cannot be null");
            }
            if (user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Username or password cannot be empty");
            }
            var logged = User_Repository.GetInstance().apiLogin(user.getUsername(), user.getPassword());
            if (logged == null) {
                return false;
            }
            if (user instanceof Pharmacist) {
                return "Pharmacist".equalsIgnoreCase(logged.userKind);
            }
            if (user instanceof Casher) {
                return "Casher".equalsIgnoreCase(logged.userKind) || "Cashier".equalsIgnoreCase(logged.userKind);
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error authenticating user: " + e.getMessage());
            return false;
        }
    }
}