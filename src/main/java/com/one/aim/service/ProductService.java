package com.one.aim.service;

import com.one.aim.rq.ProductRq;
import com.one.aim.rs.ProductCardRs;
import com.one.aim.rs.ProductRs;
import com.one.vm.core.BaseRs;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    BaseRs addProduct(ProductRq rq) throws Exception;

    BaseRs updateProduct(ProductRq rq) throws Exception;

    BaseRs uploadProductImages(Long productId, List<MultipartFile> files) throws Exception;

    BaseRs getProductImages(Long productId) throws Exception;

    BaseRs deleteProductImage(Long productId, Long imageId) throws Exception;

    String getShareableProduct(String slug) throws Exception;

    BaseRs deleteProduct(Long productId) throws Exception;

    BaseRs listProducts(int offset, int limit) throws Exception;

    BaseRs getProductsByCategory(String category, int offset, int limit) throws Exception;

    BaseRs searchProducts(String name, int offset, int limit) throws Exception;

    BaseRs listSellerProducts(boolean showInactive);

    BaseRs listAdminProducts(int page, int size, String sortBy, String direction) throws Exception;

    Page<ProductCardRs> getProducts(String category, int page, int size, String sort) throws Exception;

    Page<ProductCardRs> searchProducts(String q, String category, int page, int size) throws Exception;
}
