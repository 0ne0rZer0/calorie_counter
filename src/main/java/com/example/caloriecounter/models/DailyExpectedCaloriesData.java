package com.example.caloriecounter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.action.internal.OrphanRemovalAction;
import org.hibernate.annotations.Cascade;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "daily_expected_calories_data")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@EntityListeners(AuditingEntityListener.class)
public class DailyExpectedCaloriesData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @PrimaryKeyJoinColumn
    private User user;


    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @OneToMany(fetch = FetchType.EAGER,
            mappedBy = "dailyExpectedCaloriesData",
            orphanRemoval = true)
    private List<UserRecord> userRecords = new ArrayList<>();;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Column
    private Integer expected_calories;

    @Column
    private Integer total_calories;

    @Column
    private Boolean day_within_expected_range;

    @Override
    public String toString() {
        return "DailyExpectedCaloriesData{" +
                "id=" + id +
                ", user=" + user.getId() +
                ", date=" + date +
                '}';
    }
}
