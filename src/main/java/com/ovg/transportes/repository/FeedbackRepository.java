package com.ovg.transportes.repository;

import com.ovg.transportes.model.Feedback;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
