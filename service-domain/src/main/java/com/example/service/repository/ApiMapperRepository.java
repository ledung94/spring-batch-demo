package com.example.service.repository;

import com.example.service.entity.ApiMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiMapperRepository extends JpaRepository<ApiMapper, Long> {
    List<ApiMapper> findAllByPartnerIdOrderByOrder(Long partnerId);
}
