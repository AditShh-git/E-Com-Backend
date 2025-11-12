package com.one.aim.rq;

import java.io.Serializable;

import com.one.constants.StringConstants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentRq implements Serializable {

	private static final long serialVersionUID = -867319517915820398L;

	private Long docId;

	private String name = StringConstants.EMPTY;

	private String title = StringConstants.EMPTY;

	private String description = StringConstants.EMPTY;
}
