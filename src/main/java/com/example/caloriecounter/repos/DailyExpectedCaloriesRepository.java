package com.example.caloriecounter.repos;

import com.example.caloriecounter.models.DailyExpectedCaloriesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface DailyExpectedCaloriesRepository extends JpaRepository<DailyExpectedCaloriesData, Long> {
    public DailyExpectedCaloriesData findByDateAndUser_Id(Date date, Long id);
}
