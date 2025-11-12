package com.one.aim.rs;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.one.constants.StringConstants;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminRs implements Serializable {

	private static final long serialVersionUID = 1L;

	private String docId = StringConstants.EMPTY;

	private String userName;

	private String email;

	private String phoneNo;

	private String roll;

	// private List<AttachmentRs> atts = Collections.emptyList();
	private byte[] image;
}
