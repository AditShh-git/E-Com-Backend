package com.one.aim.mapper;

import com.one.aim.bo.AdminBO;
import com.one.aim.rs.AdminRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

//this data for front-end person
@Slf4j
public class AdminMapper {

    public static AdminRs mapToAdminRs(AdminBO bo) {

        AdminRs rs = new AdminRs();

        rs.setDocId(String.valueOf(bo.getId()));
        rs.setUserName(bo.getFullName());
        rs.setPhoneNo(bo.getPhoneNo());
        rs.setEmail(bo.getEmail());
        rs.setRoll(bo.getRole());

        // NEW — map fileId → URL
        if (bo.getImageId() != null) {
            rs.setImageUrl("/api/files/private/" + bo.getImageId() + "/view");
        }

        return rs;
    }

}
