package com.one.aim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.service.WishlistService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api")
@Slf4j
public class WishListController {

	@Autowired
	private WishlistService wishlistService;

	@PostMapping("/wishlist/save/{cartId}")
	public ResponseEntity<?> saveWishList(@PathVariable("cartId") String cartId) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(wishlistService.addToWishlist(cartId), HttpStatus.OK);
	}

	@GetMapping("/wishlist")
	public ResponseEntity<?> retrieveWishList() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(wishlistService.getUserWishlist(), HttpStatus.OK);
	}

	@DeleteMapping("/wishlist/{wishlistId}")
	public ResponseEntity<?> deleteWishlist(@PathVariable("wishlistId") String wishlistId) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(wishlistService.deleteUserWishlist(wishlistId), HttpStatus.OK);
	}
}
