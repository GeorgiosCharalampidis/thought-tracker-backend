package com.mindlog.model;

import com.mindlog.exception.BadCredentialsException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


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
    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Setter
    @NotBlank
    @Size(max = 120)
    @Email
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Setter
    @JsonIgnore
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String password;

    @Setter
    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ROLE_USER'")
    private String authority;

    @Setter
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean verified = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private java.util.List<Note> notes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private java.util.List<Comment> comments;

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
        if (email == null || !email.contains("@") || email.length() > 120) {
            throw new BadCredentialsException("Invalid email address");
        }
        if (password == null || password.length() < 4 || password.length() > 100) {
            throw new BadCredentialsException("Password must be between 4 and 100 characters long");
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
                ", authority='" + authority + '\'' +
                '}';
    }
}