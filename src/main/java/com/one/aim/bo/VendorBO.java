package com.one.aim.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vendor")
public class VendorBO {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String fullName;

	private String email;

	private String phoneNo;

	private String gst;

	private String adhaar;

	private String pancard;

	private String password;

	private boolean isVarified = false;

	private boolean login = false;

	private String roll;

//	@ElementCollection(fetch = FetchType.LAZY)
//	@CollectionTable(name = "admin_attachments", joinColumns = @JoinColumn(name = "admin_id"))
//	private List<AttachmentBO> atts;
	@Lob
	@Column(name = "image", columnDefinition = "LONGBLOB")
	private byte[] image;

}
