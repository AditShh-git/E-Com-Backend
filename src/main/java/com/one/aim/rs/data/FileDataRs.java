package com.one.aim.rs.data;

import com.one.aim.rs.FileRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileDataRs extends BaseDataRs {

    private static final long serialVersionUID = -2348083414931207606L;

    private Long fileId;

    private FileRs file;

    public FileDataRs(String message) {
        super(message);
    }

    public FileDataRs(String message, Long fileId) {
        super(message);
        this.fileId = fileId;
    }

    public FileDataRs(String message, FileRs file) {
        super(message);
        this.file = file;
    }

    public FileDataRs(String message, Long fileId, FileRs file) {
        super(message);
        this.fileId = fileId;
        this.file = file;
    }

}

