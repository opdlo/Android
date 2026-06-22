package com.danceflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 16)
    private String gender;

    @Column
    private LocalDate birthday;

    @Column(length = 255)
    private String signature;

    @Column(length = 512)
    private String avatarUrl;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 512)
    private String backgroundUrl;

}

