package com.smart.dao;

import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smart.entities.Contact;
import com.smart.entities.User;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer>{


	
	// custom method to find the contacts of that login person
	
//	@Query("Select c from Contact c where c.user.id =:userId")
//	public List<Contact> findContactsByUser(@Param("userId") int userId);
	
	// pagination
	@Query("Select c from Contact c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);

//pageable ke pass 2 chiz hoga 1st-currentpage, 2nd-contact per page

	
	
	
	
//	search method for contacts
	
	public List<Contact> findByNameContainingAndUser(String name,User user);
	
	
	
	
}


