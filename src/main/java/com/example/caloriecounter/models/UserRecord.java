package com.example.caloriecounter.models;

import lombok.*;
import org.hibernate.annotations.Cascade;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "user_records")
@Builder
@Data
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @PrimaryKeyJoinColumn
    private DailyExpectedCaloriesData dailyExpectedCaloriesData;


    @Temporal(TemporalType.DATE)
    @Column(nullable = false, length = 10)
    private Date mealDate;

    @Temporal(TemporalType.TIME)
    @Column(nullable = false, length = 45)
    private Date mealTime;

    @Column(nullable = false)
    private String meal;

    @Column
    private Integer calories;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column
    private LocalDateTime updatedAt;

    public UserRecord() {

    }
}
