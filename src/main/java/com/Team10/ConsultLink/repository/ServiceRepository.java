package com.Team10.ConsultLink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Team10.ConsultLink.model.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
}