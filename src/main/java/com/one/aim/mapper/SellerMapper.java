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
        if (bo == null) {
            log.warn("SellerBO is NULL");
            return null;
        }

        SellerRs rs = new SellerRs();

        rs.setDocId(String.valueOf(bo.getId()));
        rs.setUserName(bo.getFullName());
        rs.setPhoneNo(bo.getPhoneNo());
        rs.setPanCard(bo.getPanCard());
        rs.setGst(bo.getGst());
        rs.setAdhaar(bo.getAdhaar());
        rs.setEmail(bo.getEmail());
        rs.setRole(bo.getRole());
        rs.setVerified(bo.isVerified());

        //  Handle image reference (via fileService)
        if (bo.getImageFileId() != null) {
            rs.setImageUrl("/api/files/" + bo.getImageFileId() + "/view");
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

