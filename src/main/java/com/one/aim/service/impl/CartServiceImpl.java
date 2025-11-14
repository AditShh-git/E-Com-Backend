package com.one.aim.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.one.aim.bo.*;
import com.one.aim.repo.*;
import com.one.aim.service.AdminSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.mapper.CartMapper;
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
    private final UserRepo userRepo;
    private final AdminRepo adminRepo;
    private final SellerRepo sellerRepo;
    private final VendorRepo vendorRepo;
    private final ProductRepo productRepo;
    private final FileService fileService;
    private final AdminSettingService adminSettingService;

    // Save / create a cart (admin/seller/vendor)
    @Override
    public BaseRs saveCart(CartRq rq) throws Exception {
        log.debug("Executing saveCart() ->");

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
            cartBO = new CartBO();
            message = MessageCodes.MC_SAVED_SUCCESSFUL;
            cartBO.setCartempid(AuthUtils.findLoggedInUser().getDocId());
            cartBO.setCartempname(AuthUtils.findLoggedInUser().getFullName());
        }

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

        CartRs cartRs = CartMapper.mapToCartRs(cartBO,
                adminSettingService.getGlobalDiscount(), adminSettingService.isDiscountEngineEnabled());

        return ResponseUtils.success(new CartDataRs(message, cartRs));
    }

    // Add product(s) to logged-in user's cart (cartIds are product IDs or cart IDs)
    @Override
    @Transactional
    public BaseRs addToCart(String cartIds) throws Exception {
        log.debug("Executing addToCart() ->");

        Long userId = AuthUtils.findLoggedInUser().getDocId();
        UserBO user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

        List<Long> ids = Arrays.stream(cartIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<CartBO> responseList = new ArrayList<>();

        for (Long id : ids) {

            CartBO cart = cartRepo.findById(id).orElse(null);

            if (cart == null) {
                ProductBO product = productRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

                cart = new CartBO();
                cart.setPname(product.getName());
                cart.setDescription(product.getDescription());
                cart.setPrice(product.getPrice() == null ? 0L : product.getPrice().longValue());
                cart.setCategory(product.getCategoryName());
                cart.setVarified(true);
                cart.setEnabled(true);
                cart.setTotalitem(product.getStock() == null ? 0 : product.getStock());
                cart.setOffer(0);

                if (product.getSeller() != null) {
                    cart.setCartempid(product.getSeller().getId());
                    cart.setCartempname(product.getSeller().getFullName());
                }

                cartRepo.save(cart);
            }

            final CartBO cartItem = cart;

            Optional<CartBO> existing = user.getAddtoCart().stream()
                    .filter(c -> c.getId().equals(cartItem.getId()))
                    .findFirst();

            if (existing.isPresent()) {
                CartBO existingCart = existing.get();
                existingCart.setQuantity(existingCart.getQuantity() + 1);
                responseList.add(existingCart);
            } else {
                cart.setQuantity(1);
                cart.setUserAddToCart(user);
                user.getAddtoCart().add(cart);
                responseList.add(cart);
            }
        }

        userRepo.save(user);

        List<CartRs> rsList = CartMapper.mapToCartMinRsList(responseList,
                adminSettingService.getGlobalDiscount(), adminSettingService.isDiscountEngineEnabled());

        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_SAVED_SUCCESSFUL, rsList));
    }

    // Retrieve verified carts (public) — apply discount for response
    @Override
    public BaseRs retrieveCarts(int limit, int offset) throws Exception {
        log.debug("Executing retrieveCarts() ->");

        int page = Math.max(0, offset / Math.max(1, limit));
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<CartBO> cartPage = cartRepo.findAllByVarifiedIsTrue(pageRequest);

        List<CartRs> rslist = CartMapper.mapToCartMinRsList(cartPage.getContent(),
                adminSettingService.getGlobalDiscount(), adminSettingService.isDiscountEngineEnabled());

        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rslist));
    }

    @Transactional
    @Override
    public BaseRs retrieveAddToCarts() throws Exception {
        log.debug("Executing retrieveAddToCarts() ->");

        UserBO userBO = userRepo.findById(AuthUtils.findLoggedInUser().getDocId())
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

        List<CartBO> cartBOs = userBO.getAddtoCart();

        List<CartRs> rsList = CartMapper.mapToCartMinRsList(cartBOs,
                adminSettingService.getGlobalDiscount(), adminSettingService.isDiscountEngineEnabled());

        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rsList));
    }

    @Override
    public BaseRs retrieveCartsByCategory(String category) {
        log.debug("Executing retrieveCartsByCategory() ->");

        List<CartBO> bos = cartRepo.findAllByCategoryAndVarifiedIsTrue(category);
        List<CartRs> rslist = CartMapper.mapToCartMinRsList(bos,
                adminSettingService.getGlobalDiscount(), adminSettingService.isDiscountEngineEnabled());

        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rslist));
    }

    @Override
    public BaseRs retrieveCart(String id) {
        log.debug("Executing retrieveCart(id) ->");

        Optional<CartBO> optBo = cartRepo.findById(Long.valueOf(id));
        CartBO cartBO = optBo.orElse(null);

        CartRs rs = CartMapper.mapToCartMinRs(cartBO,
                adminSettingService.getGlobalDiscount(), adminSettingService.isDiscountEngineEnabled());
        return ResponseUtils.success(new CartDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rs));
    }

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

        List<CartRs> rsList = CartMapper.mapToCartRsList(cartBOs,
                adminSettingService.getGlobalDiscount(), adminSettingService.isDiscountEngineEnabled());

        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rsList));
    }

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

        List<CartMaxRs> rsList = CartMapper.mapToCartAdminRsList(cartBOs, sellerBOs, vendorBOs,
                adminSettingService.getGlobalDiscount(), adminSettingService.isDiscountEngineEnabled());

        return ResponseUtils.success(new CartMaxDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rsList));
    }

    @Override
    public BaseRs searchCartsByPname(String pname, int offset, int limit) {
        log.debug("Executing searchCartsByPname() ->");

        Pageable pageable = PageRequest.of(Math.max(0, offset / Math.max(1, limit)), limit);
        Page<CartBO> cartPage = cartRepo.findByPnameContainingIgnoreCase(pname, pageable);

        List<CartRs> rslist = CartMapper.mapToCartMinRsList(cartPage.getContent(),
                adminSettingService.getGlobalDiscount(), adminSettingService.isDiscountEngineEnabled());

        String message = Utils.isEmpty(rslist) ? MessageCodes.MC_NO_RECORDS_FOUND : MessageCodes.MC_RETRIEVED_SUCCESSFUL;
        return ResponseUtils.success(new CartDataRsList(message, rslist));
    }

    @Override
    public BaseRs deleteCart(String id) throws Exception {
        log.debug("Executing deleteCart(id) ->");

        CartBO cartBO = cartRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_CART_NOT_FOUND));

        Long loggedId = AuthUtils.findLoggedInUser().getDocId();
        String fullName = AuthUtils.findLoggedInUser().getFullName();

        boolean isAdmin = adminRepo.findByIdAndFullName(loggedId, fullName) != null;
        boolean isSeller = sellerRepo.findByIdAndFullName(loggedId, fullName) != null;
        boolean isVendor = vendorRepo.findByIdAndFullName(loggedId, fullName) != null;

        if (!isAdmin && !isSeller && !isVendor) {
            return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
        }

        cartRepo.delete(cartBO);

        return ResponseUtils.success(new CartDataRs(MessageCodes.MC_DELETED_SUCCESSFUL));
    }
}