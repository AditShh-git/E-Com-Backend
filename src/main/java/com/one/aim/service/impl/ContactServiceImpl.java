package com.one.aim.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.one.aim.bo.Contact;
import com.one.aim.repo.ContactRepo;
import com.one.aim.service.ContactService;


@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepo contactRepository;

    
    public Contact saveContact(Contact contact) {
        return contactRepository.save(contact);
 
   }
}