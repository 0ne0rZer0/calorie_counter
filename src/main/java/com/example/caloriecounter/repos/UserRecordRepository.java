package com.example.caloriecounter.repos;

import com.example.caloriecounter.models.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRecordRepository extends JpaRepository<UserRecord,Long> {
}
