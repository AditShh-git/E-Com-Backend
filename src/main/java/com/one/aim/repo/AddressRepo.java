package com.one.aim.repo;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.AddressBO;

@Repository
public interface AddressRepo extends JpaRepository<AddressBO, Long>{

	 List<AddressBO> findByUserid(Long userid);

    Optional<AddressBO> findFirstByUserid(Long userid);
	



}
