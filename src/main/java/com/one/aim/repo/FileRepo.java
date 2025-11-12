package com.one.aim.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.FileBO;

@Repository
public interface FileRepo extends JpaRepository<FileBO, Long> {

	FileBO findByIdAndEnabledIsTrue(Long id);

	FileBO findTop1ByMd5AndEnabledIsTrue(String md5);

}
