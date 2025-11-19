package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.List;

import com.one.aim.bo.UserBO;
import com.one.aim.rs.UserRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

//this data for front-end person
@Slf4j
public class UserMapper {

    public static UserRs mapToUserRs(UserBO bo) {

        UserRs rs = new UserRs();

        rs.setDocId(bo.getId());
        rs.setFullName(bo.getFullName());
        rs.setPhoneNo(bo.getPhoneNo());
        rs.setEmail(bo.getEmail());
        rs.setRoll(bo.getRole());

        // NEW — convert fileId → URL
        if (bo.getImageFileId() != null) {
            rs.setImageUrl("/api/files/private/" + bo.getImageFileId() + "/view");
        }

        return rs;
    }

    public static List<UserRs> mapToUserRsList(List<UserBO> bos) {
        return bos.stream()
                .map(UserMapper::mapToUserRs)
                .toList();
    }
}

