package com.one.aim.rq;

import java.util.Collections;
import java.util.List;

import com.one.vm.core.BaseVM;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartRq extends BaseVM {

	private static final long serialVersionUID = 1L;

	private String docId;

	private String pName;

	private String description;

	private long price;

	private String category;

	private boolean enabled = true;

	private String isVarified;

	private int offer;

	private int totalItem;

	// private List<AttachmentRq> atts = Collections.emptyList();

	private byte[] image;

	//public boolean isVerified;

}
