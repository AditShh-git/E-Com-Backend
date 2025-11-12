package com.one.aim.rs;

import java.io.Serializable;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonInclude(Include.NON_NULL)
public class FileRs implements Serializable {

    private static final long serialVersionUID = -8236803273268139245L;

    String docId;

    String name;

    String fileType;

    String mimeType;

    String contentType;

    String fileExtension;

}
