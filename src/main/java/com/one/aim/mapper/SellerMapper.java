package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.List;

import com.one.aim.bo.SellerBO;
import com.one.aim.bo.VendorBO;
import com.one.aim.rs.SellerRs;
import com.one.aim.rs.VendorRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SellerMapper {

    public static SellerRs mapToSellerRs(SellerBO bo) {

        if (bo == null) return null;

        SellerRs rs = new SellerRs();

        rs.setDocId(String.valueOf(bo.getId()));
        rs.setSellerId(bo.getSellerId());

        rs.setUserName(bo.getFullName());
        rs.setEmail(bo.getEmail());
        rs.setPhoneNo(bo.getPhoneNo());
        rs.setGst(bo.getGst());
        rs.setAdhaar(bo.getAdhaar());
        rs.setPanCard(bo.getPanCard());
        rs.setRole(bo.getRole());
        rs.setVerified(bo.isVerified());
        rs.setLocked(bo.isLocked());
        rs.setEmailVerified(bo.isEmailVerified());
        rs.setCreatedAt(bo.getCreatedAt());

        if (bo.getImageFileId() != null) {
            rs.setImageUrl("/api/files/private/" + bo.getImageFileId() + "/view");
        }

        return rs;
    }


    public static List<SellerRs> mapToSellerRsList(List<SellerBO> bos) {
        if (bos == null || bos.isEmpty()) {
            log.warn("SellerBO list is empty");
            return new ArrayList<>();
        }

        return bos.stream()
                .map(SellerMapper::mapToSellerRs)
                .toList();
    }
}
