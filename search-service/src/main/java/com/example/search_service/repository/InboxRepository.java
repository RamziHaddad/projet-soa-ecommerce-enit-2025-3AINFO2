package com.example.search_service.repository;
import com.example.search_service.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxRepository extends JpaRepository<InboxEvent, String> { }
