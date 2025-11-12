package com.one.aim.bo;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Embeddable
@Table(name = "admin_attachments")
public class AttachmentBO implements Serializable {

    private static final long serialVersionUID = 2303097610048429829L;

    private Long docid;

    private String name;

    private String title;

    private String type;

    @Column(length = 255, nullable = true)
    private String description;

    private LocalDateTime on;

    private String by;

    public AttachmentBO(Long docid, String name, String type) {
        super();
        this.docid = docid;
        this.name = name;
        this.type = type;
    }

}
