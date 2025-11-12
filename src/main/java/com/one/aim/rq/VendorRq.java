package com.one.aim.rq;

import com.one.vm.core.BaseVM;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VendorRq extends BaseVM {

	private static final long serialVersionUID = 1L;

	private String docId;

	private String userName;

	private String email;

	private String phoneNo;

	private String password;

	private String gst;

	private String adhaar;

	private String pancard;

	private String isVarified;
	
	

	// private List<AttachmentRq> elExemptionAtts = Collections.emptyList();
	
	private byte[] image;





	
}
