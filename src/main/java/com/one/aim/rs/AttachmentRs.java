package com.one.aim.rs;

import java.io.Serializable;

import com.one.constants.StringConstants;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttachmentRs implements Serializable {

    private static final long serialVersionUID = -4441996416869690126L;

    private long docId;

    private String name = StringConstants.EMPTY;

    private String title = StringConstants.EMPTY;

    private String type = StringConstants.EMPTY;

    private String by = StringConstants.EMPTY;

    private String on = StringConstants.EMPTY;

    private String description = StringConstants.EMPTY;

    public AttachmentRs(long docId, String name, String type) {
        super();
        this.docId = docId;
        this.name = name;
        this.type = type;
    }

}
