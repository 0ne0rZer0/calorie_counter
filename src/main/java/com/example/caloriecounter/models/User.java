package com.example.caloriecounter.models;

import lombok.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.caloriecounter.enums.Roles.REGULAR_USER;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 36)
    private String sessionId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date sessionExpiryTime;

    @Column
    private Integer expected_calories;

    @Column
    private Integer role;

    @Column(nullable = false, unique = true, length = 45)
    private String email;

    @Column(nullable = false, length = 44)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String firstName;

    @Column(nullable = false, length = 20)
    private String lastName;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column
    private LocalDateTime updatedAt;

    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true)
    private List<DailyExpectedCaloriesData> dailyExpectedCaloriesDataList = new ArrayList<>();;

    public User() {

    }

    @PrePersist
    void preInsert() {
        if ( getExpected_calories() == null ) { setExpected_calories( 2400 );}
        if ( getRole() == null ) { setRole(REGULAR_USER.ordinal()); }
    }
}

