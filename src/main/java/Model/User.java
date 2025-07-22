package model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Users") // Use "Users" (plural) instead of "User"
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private String role;
    
    @Column(name = "google_id")
    private String googleId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "school")
    private String school;

    public User() {
    }

    public User(String username, String password, String email, String role, String googleId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.googleId = googleId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role != null && (role.equals("student") || role.equals("teacher") || role.equals("admin"))) {
            this.role = role;
        } else {
            throw new IllegalArgumentException("Invalid role: " + role + ". Must be 'student', 'teacher', or 'admin'.");
        }
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getFirstName() {
      return this.firstName;
    }

    public void setFirstName(String value) {
      this.firstName = value;
    }

    public String getLastName() {
      return this.lastName;
    }
    public void setLastName(String value) {
      this.lastName = value;
    }

    public Date getDateOfBirth() {
      return this.dateOfBirth;
    }
    public void setDateOfBirth(Date value) {
      this.dateOfBirth = value;
    }

    public String getGender() {
      return this.gender;
    }
    public void setGender(String value) {
      this.gender = value;
    }

    public String getAddress() {
      return this.address;
    }
    public void setAddress(String value) {
      this.address = value;
    }

    public String getPhoneNumber() {
      return this.phoneNumber;
    }
    public void setPhoneNumber(String value) {
      this.phoneNumber = value;
    }

    public String getSchool() {
      return this.school;
    }
    public void setSchool(String value) {
      this.school = value;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}