package com.one.aim.service;

import com.one.aim.rq.ProductRq;
import com.one.aim.rs.ProductRs;
import com.one.vm.core.BaseRs;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    BaseRs addProduct(ProductRq rq) throws Exception;

    BaseRs updateProduct(ProductRq rq) throws Exception;

    BaseRs uploadProductImages(Long productId, List<MultipartFile> files) throws Exception;

    BaseRs getProductImages(Long productId) throws Exception;

    BaseRs deleteProductImage(Long productId, Long imageId) throws Exception;

    ProductRs getShareableProduct(String slug) throws Exception;

    BaseRs deleteProduct(Long productId) throws Exception;

}
