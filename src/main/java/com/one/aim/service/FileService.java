package com.one.aim.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.one.aim.bo.AttachmentBO;
import com.one.aim.bo.FileBO;
import com.one.aim.rq.AttachmentRq;
import com.one.vm.core.BaseRs;

public interface FileService {

	public BaseRs uploadFile(MultipartFile file) throws Exception;

	public FileBO downloadFile(String id) throws Exception;

	public BaseRs deleteFileById(String fileId) throws Exception;

	public byte[] getContentFromGridFS(String fileId) throws Exception;

	public List<AttachmentBO> prepareAttBOs(List<AttachmentRq> vms, String userName);

    FileBO uploadAndReturnFile(MultipartFile file) throws Exception;

}
