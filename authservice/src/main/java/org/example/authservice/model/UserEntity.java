package org.example.authservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;

    public UserEntity() {}

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
