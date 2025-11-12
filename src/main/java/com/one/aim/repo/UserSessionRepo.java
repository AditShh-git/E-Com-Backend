package com.one.aim.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.UserSessionBO;

@Repository
public interface UserSessionRepo extends JpaRepository<UserSessionBO, Long> {

}