package com.one.aim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.rq.AdminRq;
import com.one.aim.service.AdminService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api")
@Slf4j
public class AdminController {

	@Autowired
	private AdminService adminService;

	@PostMapping(value = "/auth/signup/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveAdmin(
	        @ModelAttribute AdminRq rq,
			@RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

	    if (file != null && !file.isEmpty()) {
	        rq.setImage(file.getBytes());
	    }

	    return new ResponseEntity<>(adminService.saveAdmin(rq), HttpStatus.OK);
	}


//	@GetMapping("/find/id/{id}")
//    public AdminBO getUserBOById(@PathVariable Long id) {
//        return adminService.getAdminBOById(id);
//    }

	@PostMapping("/find/admin")
	public ResponseEntity<?> retrieveAdminBO() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(adminService.retrieveAdmin(), HttpStatus.OK);
	}

	@DeleteMapping("/delete/admin/{id}")
	public ResponseEntity<?> deleteAdmin(@PathVariable("id") String id) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(adminService.deleteAdmin(id), HttpStatus.OK);
	}
}
