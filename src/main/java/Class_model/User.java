package Class_model;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class representing a User with attributes and roles.
 */
public abstract class User {
     /**
     * The ID of the user (per-instance; backed by the database when using the API).
     */
    private int userId;
    /**
     * The username of the user.
     */
    private String Username;

    /**
     * The password of the user.
     */
    private String Password;

    /**
     * The email address of the user.
     */
    private String UserEmail;

    /**
     * The phone number of the user.
     */
    private String PhoneNumber;

    /**
     * Indicates whether the user is active.
     */
    private boolean isActive;

    /**
     * The set of roles assigned to the user.
     */
    protected Set<Role> Roles = new HashSet<>();

    /**
     * Constructor to initialize a User object with the given attributes.
     * 
     * @param Username   The username of the user.
     * @param Password   The password of the user.
     * @param User_Email The email address of the user.
     * @param PhoneNumber The phone number of the user.
     * @param Roles      The set of roles assigned to the user.
     */
    public User(int UserId,String Username, String Password, String User_Email, String PhoneNumber, Set<Role> Roles) {
        this.userId = UserId;
        this.Username = Username;
        this.Password = Password;
        this.UserEmail = User_Email;
        this.Roles = Roles;
        this.PhoneNumber = PhoneNumber;
        isActive = true;
    }
    public User(){
        this.Username = " ";
        this.Password = " ";
        this.UserEmail = " ";
        this.PhoneNumber = " ";
        isActive = true;
    }
    /**
     * Sets the id of the user.
     * 
     * @param ID The new Id.
     */
    public void setID(int ID) {
        this.userId = ID;
    }

    /**
     * Gets the Id of the user.
     * 
     * @return The ID.
     */
    public int getID() {
        return userId;
    }

    /**
     * Sets the username of the user.
     * 
     * @param Username The new username.
     */
    public void setUsername(String Username) {
        this.Username = Username;
    }

    /**
     * Gets the username of the user.
     * 
     * @return The username.
     */
    public String getUsername() {
        return Username;
    }

    /**
     * Sets the password of the user.
     * 
     * @param Password The new password.
     */
    public void setPassword(String Password) {
        this.Password = Password;
    }

    /**
     * Gets the password of the user.
     * 
     * @return The password.
     */
    public String getPassword() {
        return Password;
    }

    /**
     * Sets the email address of the user.
     * 
     * @param UserEmail The new email address.
     */
    public void setUserEmail(String UserEmail) {
        this.UserEmail = UserEmail;
    }

    /**
     * Gets the email address of the user.
     * 
     * @return The email address.
     */
    public String getUserEmail() {
        return UserEmail;
    }

    /**
     * Sets the phone number of the user.
     * 
     * @param PhoneNumber The new phone number.
     */
    public void setPhoneNumber(String PhoneNumber) {
        this.PhoneNumber = PhoneNumber;
    }

    /**
     * Gets the phone number of the user.
     * 
     * @return The phone number.
     */
    public String getPhoneNumber() {
        return PhoneNumber;
    }

    /**
     * Returns a string representation of the user.
     * 
     * @return A string containing user details.
     */
    public String toString() {
        return "UserID: " + Username + "\nPassword: " + Password + "\nUserEmail: " + UserEmail + "\nPhoneNumber: " + PhoneNumber;
    }

    /**
     * Checks if the user has a specific role.
     * 
     * @param role The role to check.
     * @return True if the user has the role, false otherwise.
     */
    public boolean CheckRole(Role role) {
        return Roles.contains(role);
    }

    /**
     * Sets the roles of the user.
     * 
     * @param Roles The new set of roles.
     */
    public void setRoles(Set<Role> Roles) {
        this.Roles = Roles;
    }

    /**
     * Gets the roles of the user.
     * 
     * @return The set of roles.
     */
    public Set<Role> getRoles() {
        return Roles;
    }

    /**
     * Adds a role to the user.
     * 
     * @param role The role to add.
     */
    public void Add_Role(Role role) {
        Roles.add(role);
    }

    /**
     * Removes a role from the user.
     * 
     * @param role The role to remove.
     * @return True if the role was removed, false otherwise.
     */
    public boolean Remove_Role(Role role) {
        return Roles.remove(role);
    }

    /**
     * Deactivates the user.
     */
    public void setactive(boolean active) {
        this.isActive = active;
    }

    /**
     * Gets the active status of the user.
     * 
     * @return True if the user is active, false otherwise.
     */
    public boolean getactive() {
        return isActive;
    }
}