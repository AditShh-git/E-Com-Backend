package com.one.aim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.rq.VendorRq;
import com.one.aim.service.VendorService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api")
@Slf4j
public class VendorController {

	@Autowired
	private VendorService adminService;

	@PostMapping(value = "/auth/signup/vendor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveAdmin(@ModelAttribute VendorRq rq,
			@RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		if (file != null && !file.isEmpty()) {
			rq.setImage(file.getBytes()); // ✅ convert file to byte[]
		}
		return new ResponseEntity<>(adminService.saveVendor(rq), HttpStatus.OK);
	}

//	@GetMapping("/find/id/{id}")
//    public AdminBO getUserBOById(@PathVariable Long id) {
//        return adminService.getAdminBOById(id);
//    }

	@GetMapping("/find/vendor")
	public ResponseEntity<?> retrieveAdminBO() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(adminService.retrieveVendor(), HttpStatus.OK);
	}

	@GetMapping("/vendors")
	public ResponseEntity<?> retrieveVendors() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [GET /vendors]");
		}
		return new ResponseEntity<>(adminService.retrieveVendors(), HttpStatus.OK);
	}

	@DeleteMapping("/delete/vendor/{id}")
	public ResponseEntity<?> deleteVendor(@PathVariable("id") String id) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(adminService.deleteVendor(id), HttpStatus.OK);
	}
}