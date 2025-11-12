package com.one.aim.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.CartBO;

@Repository
public interface CartRepo extends JpaRepository<CartBO, Long> {

	// List<CartBO> findByUserId(Long userId);

	// List<CartBO> findBy

	List<CartBO> findByEnabledIsTrue();

	List<CartBO> findAllByCategoryAndVarifiedIsTrue(String category);

	List<CartBO> findAllByCartempid(Long id);

	List<CartBO> findAllByCartempidAndCartempname(Long id, String name);

	Page<CartBO> findAllByVarifiedIsTrue(Pageable pageable);

	List<CartBO> findByPnameContainingIgnoreCase(String pname);

	Page<CartBO> findByPnameContainingIgnoreCase(String pname, Pageable pageable);

}
