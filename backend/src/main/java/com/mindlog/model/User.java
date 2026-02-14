package com.mindlog.model;

import com.mindlog.exception.BadCredentialsException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true)
    private String username;

    @Setter
    @NotBlank
    @Size(max = 40)
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    @NotBlank
    @Size(max = 40)
    @Column(nullable = false)
    private String password;

    @Setter
    @NotBlank
    @Size(max = 20)
    @Column(nullable = false)
    private String authority;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Note> notes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments;

    public User() {
    }

    public User(String username, String email, String password, String authority) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.authority = authority;
    }

    public void validateCredentials() {
        if (username == null || username.isBlank() || username.length() > 20) {
            throw new BadCredentialsException("Invalid username");
        }
        if (email == null || !email.contains("@") || email.length() > 50) {
            throw new BadCredentialsException("Invalid email address");
        }
        if (password == null || password.length() < 4) {
            throw new BadCredentialsException("Password must be at least 4 characters long");
        }
        if (authority == null || authority.isBlank() || authority.length() > 20) {
            throw new BadCredentialsException("Invalid authority");
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}