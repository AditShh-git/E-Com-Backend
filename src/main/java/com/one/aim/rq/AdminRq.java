package com.one.aim.rq;

import java.util.Collections;
import java.util.List;

import com.one.vm.core.BaseVM;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AdminRq extends BaseVM {

	private static final long serialVersionUID = 1L;

	private String docId;

	private String userName;

	private String email;

	private String phoneNo;

	private String password;

	// private List<AttachmentRq> elExemptionAtts = Collections.emptyList();

	private MultipartFile image;
}
