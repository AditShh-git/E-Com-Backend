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

import com.one.aim.rq.CartRq;
import com.one.aim.service.CartService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api")
@Slf4j
public class CartController {

	@Autowired
	private CartService cartService;

	@PostMapping(value = "/cart/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveCart(@ModelAttribute CartRq rq,
			@RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		if (file != null && !file.isEmpty()) {
			rq.setImage(file.getBytes()); //  convert file to byte[]
		}
		return new ResponseEntity<>(cartService.saveCart(rq), HttpStatus.OK);
	}

	@PostMapping(value = "/cart/addtocart")
	public ResponseEntity<?> AddToCart(@RequestParam String cartIds) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(cartService.addToCart(cartIds), HttpStatus.OK);
	}

	@GetMapping("/carts")
	public ResponseEntity<?> retrieveCartList(
			@RequestParam(value = "limit", required = false, defaultValue = "20") int limit,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(cartService.retrieveCarts(limit, offset), HttpStatus.OK);
	}

	@GetMapping("/addTocarts")
	public ResponseEntity<?> retrieveAddToCarts() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [GET /addTocarts]");
		}
		return new ResponseEntity<>(cartService.retrieveAddToCarts(), HttpStatus.OK);
	}

	@GetMapping("/carts/category/{category}")
	public ResponseEntity<?> retrieveCartsByCategory(@PathVariable("category") String category) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(cartService.retrieveCartsByCategory(category), HttpStatus.OK);
	}

	@GetMapping("/carts/{id}")
	public ResponseEntity<?> retrieveCart(@PathVariable("id") String id) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(cartService.retrieveCart(id), HttpStatus.OK);
	}

	@GetMapping("/emptype/carts")
	public ResponseEntity<?> retrieveCartsEmpType() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(cartService.retrieveCartByEmpType(), HttpStatus.OK);
	}

	@GetMapping("/admin/carts")
	public ResponseEntity<?> retrieveCartsAdmin() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(cartService.retrieveCartsByAdmin(), HttpStatus.OK);
	}

	@GetMapping("/search")
	public ResponseEntity<?> retrieveCartList(@RequestParam String pname,
			@RequestParam(value = "limit", required = false, defaultValue = "20") int limit,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(cartService.searchCartsByPname(pname, offset, limit), HttpStatus.OK);
	}

	@DeleteMapping("/delete/cart/{id}")
	public ResponseEntity<?> deleteCart(@PathVariable("id") String id) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(cartService.deleteCart(id), HttpStatus.OK);
	}
}
