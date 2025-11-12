package com.one.aim.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.one.aim.bo.Contact;


public interface ContactRepo extends JpaRepository<Contact, Long> {
}
