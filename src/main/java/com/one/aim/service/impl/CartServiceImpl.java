package com.one.aim.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.CartBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.UserBO;
import com.one.aim.bo.VendorBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.mapper.CartMapper;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.CartRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.repo.VendorRepo;
import com.one.aim.rq.CartRq;
import com.one.aim.rs.CartMaxRs;
import com.one.aim.rs.CartRs;
import com.one.aim.rs.data.CartDataRs;
import com.one.aim.rs.data.CartDataRsList;
import com.one.aim.rs.data.CartMaxDataRsList;
import com.one.aim.service.CartService;
import com.one.aim.service.FileService;
import com.one.constants.StringConstants;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepo cartRepo;
    private final AdminRepo adminRepo;
    private final SellerRepo sellerRepo;
    private final UserRepo userRepo;
    private final VendorRepo vendorRepo;

    // ============================================
    // ✅ SAVE / UPDATE CART
    // ============================================
    @Override
    public BaseRs saveCart(CartRq rq) throws Exception {
        log.debug("Executing saveCart() ->");

        // ✅ Find logged-in user (by fullName)
         AdminBO adminBO = adminRepo.findByIdAndFullName(AuthUtils.findLoggedInUser().getDocId(),
                AuthUtils.findLoggedInUser().getFullName());
        SellerBO sellerBO = sellerRepo.findByIdAndFullName(AuthUtils.findLoggedInUser().getDocId(),
                AuthUtils.findLoggedInUser().getFullName());
        VendorBO vendorBO = vendorRepo.findByIdAndFullName(AuthUtils.findLoggedInUser().getDocId(),
                AuthUtils.findLoggedInUser().getFullName());

        if (adminBO == null && sellerBO == null && vendorBO == null) {
            log.error(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
            return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
        }

        String docId = Utils.getValidString(rq.getDocId());
        String message;
        CartBO cartBO;

        // ✅ Update existing cart
        if (Utils.isNotEmpty(docId)) {
            Optional<CartBO> optCartBO = cartRepo.findById(Long.parseLong(docId));
            if (optCartBO.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_CART_NOT_FOUND);
            }
            cartBO = optCartBO.get();

            boolean verified = Boolean.parseBoolean(rq.getIsVarified());
            adminBO = adminRepo.findByIdAndFullName(AuthUtils.findLoggedInUser().getDocId(),
                    AuthUtils.findLoggedInUser().getFullName());
            if (adminBO == null && verified) {
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
            }
            cartBO.setVarified(verified);
            message = MessageCodes.MC_UPDATED_SUCCESSFUL;

        } else {
            // ✅ New cart creation
            cartBO = new CartBO();
            message = MessageCodes.MC_SAVED_SUCCESSFUL;
            cartBO.setCartempid(AuthUtils.findLoggedInUser().getDocId());
            cartBO.setCartempname(AuthUtils.findLoggedInUser().getFullName());
        }

        // ✅ Set cart details
        String pName = Utils.getValidString(rq.getPName());
        if (!pName.equals(Utils.getValidString(cartBO.getPname()))) {
            cartBO.setPname(pName);
        }

        String description = Utils.getValidString(rq.getDescription());
        if (!description.equals(cartBO.getDescription())) {
            cartBO.setDescription(description);
        }

        cartBO.setTotalitem(rq.getTotalItem());
        cartBO.setCategory(Utils.getValidString(rq.getCategory()));

        if (rq.getImage() != null) {
            cartBO.setImage(rq.getImage());
        }

        cartBO.setPrice(rq.getPrice());
        cartBO.setOffer(rq.getOffer());
        cartBO.setEnabled(rq.isEnabled());

        cartRepo.save(cartBO);

        CartRs cartRs = CartMapper.mapToCartRs(cartBO);
        return ResponseUtils.success(new CartDataRs(message, cartRs));
    }

    // ============================================
    // ✅ ADD TO CART
    // ============================================
    @Override
    public BaseRs addToCart(String cartIds) throws Exception {
        log.debug("Executing addToCart() ->");

        UserBO userBO = userRepo.findById(AuthUtils.findLoggedInUser().getDocId())
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

        List<Long> ids = Arrays.stream(cartIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<CartBO> cartBos = cartRepo.findAllById(ids);
        cartBos.forEach(cart -> cart.setUserAddToCart(userBO));

        userBO.getAddtoCart().addAll(cartBos);
        userRepo.save(userBO);

        List<CartRs> rsList = CartMapper.mapToCartMinRsList(cartBos);
        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_SAVED_SUCCESSFUL, rsList));
    }

    // ============================================
    // ✅ RETRIEVE VERIFIED CARTS (PUBLIC)
    // ============================================
    @Override
    public BaseRs retrieveCarts(int limit, int offset) throws Exception {
        log.debug("Executing retrieveCarts() ->");

        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<CartBO> cartPage = cartRepo.findAllByVarifiedIsTrue(pageRequest);

        List<CartRs> rslist = CartMapper.mapToCartMinRsList(cartPage.getContent());
        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rslist));
    }

    // ============================================
    // ✅ RETRIEVE USER'S CART ITEMS
    // ============================================
    @Transactional
    @Override
    public BaseRs retrieveAddToCarts() throws Exception {
        log.debug("Executing retrieveAddToCarts() ->");

        UserBO userBO = userRepo.findById(AuthUtils.findLoggedInUser().getDocId())
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

        List<CartBO> cartBOs = userBO.getAddtoCart();
        List<CartRs> rsList = CartMapper.mapToCartMinRsList(cartBOs);
        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rsList));
    }

    // ============================================
    // ✅ RETRIEVE BY CATEGORY
    // ============================================
    @Override
    public BaseRs retrieveCartsByCategory(String category) {
        log.debug("Executing retrieveCartsByCategory() ->");

        List<CartBO> bos = cartRepo.findAllByCategoryAndVarifiedIsTrue(category);
        List<CartRs> rslist = CartMapper.mapToCartMinRsList(bos);
        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rslist));
    }

    // ============================================
    // ✅ RETRIEVE SINGLE CART
    // ============================================
    @Override
    public BaseRs retrieveCart(String id) {
        log.debug("Executing retrieveCart(id) ->");

        Optional<CartBO> optBo = cartRepo.findById(Long.valueOf(id));
        CartBO cartBO = optBo.orElse(null);

        CartRs rs = CartMapper.mapToCartMinRs(cartBO);
        return ResponseUtils.success(new CartDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rs));
    }

    // ============================================
    // ✅ RETRIEVE CARTS BY LOGGED-IN SELLER/VENDOR
    // ============================================
    @Override
    public BaseRs retrieveCartByEmpType() {
        log.debug("Executing retrieveCartByEmpType() ->");

        List<CartBO> cartBOs = cartRepo.findAllByCartempidAndCartempname(
                AuthUtils.findLoggedInUser().getDocId(),
                AuthUtils.findLoggedInUser().getFullName()
        );

        if (Utils.isEmpty(cartBOs)) {
            return ResponseUtils.success(new CartDataRs(MessageCodes.MC_NO_RECORDS_FOUND));
        }

        List<CartRs> rsList = CartMapper.mapToCartRsList(cartBOs);
        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rsList));
    }

    // ============================================
    // ✅ RETRIEVE ALL CARTS (ADMIN)
    // ============================================
    @Override
    public BaseRs retrieveCartsByAdmin() {
        log.debug("Executing retrieveCartsByAdmin() ->");

        Optional<AdminBO> optAdmin = adminRepo.findById(AuthUtils.findLoggedInUser().getDocId());
        if (optAdmin.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
        }

        List<CartBO> cartBOs = cartRepo.findAll();
        if (Utils.isEmpty(cartBOs)) {
            return ResponseUtils.success(new CartDataRs(MessageCodes.MC_NO_RECORDS_FOUND));
        }

        List<SellerBO> sellerBOs = new ArrayList<>();
        List<VendorBO> vendorBOs = new ArrayList<>();

        for (CartBO cartBO : cartBOs) {
            Long cartEmpId = cartBO.getCartempid();
            String cartEmpName = cartBO.getCartempname();

            SellerBO sellerBO = sellerRepo.findByIdAndFullName(cartEmpId, cartEmpName);
            VendorBO vendorBO = vendorRepo.findByIdAndFullName(cartEmpId, cartEmpName);

            sellerBOs.add(sellerBO);
            vendorBOs.add(vendorBO);
        }

        List<CartMaxRs> rsList = CartMapper.mapToCartAdminRsList(cartBOs, sellerBOs, vendorBOs);
        return ResponseUtils.success(new CartMaxDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rsList));
    }

    // ============================================
    // ✅ SEARCH BY PRODUCT NAME
    // ============================================
    @Override
    public BaseRs searchCartsByPname(String pname, int offset, int limit) {
        log.debug("Executing searchCartsByPname() ->");

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<CartBO> cartPage = cartRepo.findByPnameContainingIgnoreCase(pname, pageable);

        List<CartRs> rslist = CartMapper.mapToCartMinRsList(cartPage.getContent());
        String message = Utils.isEmpty(rslist) ? MessageCodes.MC_NO_RECORDS_FOUND : MessageCodes.MC_RETRIEVED_SUCCESSFUL;
        return ResponseUtils.success(new CartDataRsList(message, rslist));
    }

    // ============================================
    // ✅ DELETE CART
    // ============================================
    @Override
    public BaseRs deleteCart(String id) throws Exception {
        log.debug("Executing deleteCart(id) ->");

        Optional<CartBO> optCartBO = cartRepo.findById(Long.valueOf(id));
        if (optCartBO.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_CART_NOT_FOUND);
        }

        Optional<UserBO> optUser = userRepo.findById(AuthUtils.findLoggedInUser().getDocId());
        if (optUser.isPresent()) {
            return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
        }

        CartBO cartBO = optCartBO.get();
        cartRepo.delete(cartBO);

        CartRs cartRs = CartMapper.mapToCartRs(cartBO);
        return ResponseUtils.success(new CartDataRs(MessageCodes.MC_DELETED_SUCCESSFUL, cartRs));
    }
}