package com.one.aim.service.impl;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.bo.AttachmentBO;
import com.one.aim.bo.FileBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.helper.FileHelper;
import com.one.aim.mapper.AttachmentMapper;
import com.one.aim.repo.FileRepo;
import com.one.aim.rq.AttachmentRq;
import com.one.aim.rs.FileRs;
import com.one.aim.rs.data.FileDataRs;
import com.one.aim.service.FileService;
import com.one.constants.StringConstants;
import com.one.utils.EncryptionUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;



@Slf4j
@Service("fileService")
public class FileServiceImpl implements FileService {

	@Value("${dt.file.upload-root-dir}")
	public void setDirStatic(String dir) {
		FileHelper.UPLOAD_ROOT_DIR = dir;
	}

	@Autowired
	FileRepo fileRepo;

	public BaseRs uploadFile(MultipartFile file) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing uploadFile(MultipartFile) ->");
		}

		if (file == null) {
			log.error("MultipartFile IS NULL ->");
			throw new FileNotFoundException(ErrorCodes.EC_FILE_NOT_FOUND);
		}
		FileBO fileBO = uploadFile(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
		if (fileBO == null) {
			log.error("FileBO IS NULL");
			// throw new FileUploadFailedException(ErrorCodes.EC_FILE_IMPORT_FAILED);
		}

		FileRs fileRs = new FileRs();
		fileRs.setDocId(String.valueOf(fileBO.getId()));
		fileRs.setName(fileBO.getName());
		fileRs.setContentType(fileBO.getContenttype());
		String message = MessageCodes.MC_FILEUPLOAD_SUCCESSFUL;
		return ResponseUtils.success(new FileDataRs(message, fileBO.getId(), fileRs));
	}

	@Override
	public FileBO downloadFile(String id) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing downloadFile(id) ->");
		}

		FileBO fileBO = fileRepo.findByIdAndEnabledIsTrue(Long.valueOf(id));
		if (fileBO == null) {
			log.error("FileBO IS NULL");
			throw new FileNotFoundException(ErrorCodes.EC_FILE_NOT_FOUND);
		}
		String finalPath = FileHelper.prepareChunksDir(fileBO.getPath());
		File folder = new File(finalPath);
		if (folder == null || !folder.exists()) {
			log.error("Folder does not exist - " + finalPath);
			return null;
		}
		File[] listOfFiles = folder.listFiles();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				Files.copy(file.toPath(), os);
			}
		}
		// ByteArrayInputStream ifs = new ByteArrayInputStream(os.toByteArray());
		byte[] fileBytes = os.toByteArray();
		fileBO.setInputstream(fileBytes);
		return fileBO;
	}

	@Override
	public BaseRs deleteFileById(String fileId) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing deleteFileById(FileId) ->");
		}

		if (Utils.isEmpty(fileId)) {
			log.error(ErrorCodes.EC_REQUIRED_DOCID);
			// throw new RequiredDocIdException(ErrorCodes.EC_REQUIRED_DOCID);
		}
		FileBO fileBO = fileRepo.findByIdAndEnabledIsTrue(Long.valueOf(fileId));
		if (fileBO == null) {
			log.error(ErrorCodes.EC_FILE_NOT_FOUND);
			throw new FileNotFoundException(ErrorCodes.EC_FILE_NOT_FOUND);
		}
		// fileBO.setEnabled(false);
		fileRepo.save(fileBO);
		return ResponseUtils.success(MessageCodes.MC_DELETED_SUCCESSFUL);
	}

	public FileBO uploadFile(InputStream inputStream, String fileName, String contentType) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing uploadFile(InputStream, FileName, ContentType) ->");
		}

		try {
			if (inputStream == null || Utils.isEmpty(fileName)) {
				log.error("InputStream IS NULL ->");
				throw new FileNotFoundException(ErrorCodes.EC_FILE_NOT_FOUND);
			}
			byte[] fileBytes = IOUtils.toByteArray(inputStream);
			long tempSize = fileBytes.length;
			long actualSize = fileBytes.length;

			// byte[] allBytes = new byte[(int) fileSize];
			// inputStream.read(allBytes);
			String md5 = EncryptionUtils.makeMD5String(fileBytes);
			FileBO extFileBO = fileRepo.findTop1ByMd5AndEnabledIsTrue(md5);
			FileBO fileBO = new FileBO();
			fileBO.setMd5(md5);
			boolean isExist = FileHelper.isFileExists(extFileBO);
			if (extFileBO == null || !isExist) {
				String subDir = StringConstants.EMPTY;
				if (extFileBO != null && Utils.isNotEmpty(extFileBO.getPath())) {
					subDir = extFileBO.getPath();
				} else {
					subDir = FileHelper.prepareSubDir(StringConstants.EMPTY, LocalDate.now());
				}
				File chunksDir = new File(FileHelper.prepareChunksDir(subDir));
				// if the directory does not exist, create it
				if (!chunksDir.exists()) {
					try {
						chunksDir.mkdirs();
					} catch (SecurityException se) {
						log.error("SecurityException in uploadFile(InputStream, FileName, ContentType) - " + se);
						throw new Exception(se);
					}
				}
				int chunkSize = FileHelper.getChunkSize(tempSize);
				int nChunks = 0, readLength = chunkSize;
				try {
					int srcPos = 0;
					while (tempSize > 0) {
						if (tempSize <= chunkSize) {
							readLength = (int) tempSize;
						}
						byte[] byteChunkPart = new byte[readLength];
						System.arraycopy(fileBytes, srcPos, byteChunkPart, 0, readLength);
						srcPos += readLength;
						tempSize -= readLength;
						nChunks++;
						String newFileName = chunksDir + File.separator + nChunks + ".part";
						FileOutputStream filePart = new FileOutputStream(new File(newFileName));
						filePart.write(byteChunkPart);
						filePart.flush();
						filePart.close();
						byteChunkPart = null;
						filePart = null;
					}
					inputStream.close();
				} catch (IOException ex) {
					log.error("IOException in uploadFile(InputStream, FileName, ContentType) - " + ex.getMessage());
					throw new IOException(ex);
				}

				fileBO.setName(fileName);
				fileBO.setContenttype(contentType);
				fileBO.setPath(subDir);
				fileBO.setNoofchunks(nChunks);
				fileBO.setSize(actualSize);
				fileRepo.save(fileBO);
			} else {
				fileBO.setName(fileName);
				fileBO.setContenttype(contentType);
				fileBO.setPath(extFileBO.getPath());
				fileBO.setNoofchunks(extFileBO.getNoofchunks());
				fileBO.setSize(actualSize);
				fileRepo.save(fileBO);
			}
			return fileBO;
		} catch (Exception e) {
			log.error("Exception in uploadFile(InputStream, FileName, ContentType) - " + e);
			throw new Exception(e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				log.error("Exception in uploadFile(InputStream, FileName, ContentType) in Finally - " + e);
			}
		}
	}

    @Override
    public byte[] getContentFromGridFS(String fileId) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing getContentFromGridFS(fileId) ->");
        }

        if (Utils.isEmpty(fileId)) {
            return new byte[0];
        }

        FileBO fileBO = fileRepo.findByIdAndEnabledIsTrue(Long.valueOf(fileId));
        if (fileBO == null) {
            return null;
        }

        // FIRST: Return stored BLOB (for invoice PDFs)
        if (fileBO.getInputstream() != null && fileBO.getInputstream().length > 0) {
            return fileBO.getInputstream();
        }

        //  FALLBACK: Old chunk-based file reading
        String finalPath = FileHelper.prepareChunksDir(fileBO.getPath());
        File folder = new File(finalPath);

        if (!folder.exists()) {
            log.error("Folder does not exist - " + finalPath);
            return null;
        }

        File[] listOfFiles = folder.listFiles();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                Files.copy(file.toPath(), os);
            }
        }

        return os.toByteArray();
    }


    @Override
	    public List<AttachmentBO> prepareAttBOs(List<AttachmentRq> rsList, String userName) {

	        if (log.isDebugEnabled()) {
	            log.debug("Executing prepareAttachmentBOs(List<AttachmentRs>, userName) ->");
	        }

	        try {
	            if (Utils.isEmpty(rsList)) {
	                return Collections.<AttachmentBO>emptyList();
	            }
	            // TODO need to check duplicate files
	            List<AttachmentBO> bos = new ArrayList<>();
	            // Set<String> attachmentFileNames = new HashSet<>();
	            for (AttachmentRq rs : rsList) {
	                // if (attachmentFileNames.contains(rs.getName())) {
	                // continue;
	                // } else {
	                // attachmentFileNames.add(rs.getName());
	                long fileDocId =rs.getDocId();
	                FileBO file = fileRepo.findByIdAndEnabledIsTrue(fileDocId);
	                if (null != file) {
	                    AttachmentBO bo = AttachmentMapper.prepareAttachmentBO(rs, file, userName);
	                    if (null == bo) {
	                        continue;
	                    }
	                    bos.add(bo);
	                }
	                // }
	            }
	            return bos;
	        } catch (Exception e) {
	            log.error("Exception in prepareAttachmentBOs(List<AttachmentRs>, userName) ->" + e);
	            return Collections.<AttachmentBO>emptyList();
	        }
	    }

    @Override
    public FileBO uploadAndReturnFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            log.error("File is null or empty in uploadAndReturnFile()");
            throw new FileNotFoundException("File is empty or missing");
        }

        // Reuse your existing uploadFile(InputStream, name, type) logic
        return uploadFile(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
    }

    @Override
    public FileBO uploadBytes(byte[] data, String filename) throws IOException {

        FileBO file = new FileBO();

        file.setName(filename);
        file.setContenttype("application/pdf");
        file.setSize(data.length);
        file.setInputstream(data);
        file.setEnabled(true);

        // You may set path, md5, noofchunks if needed
        file.setPath(null);
        file.setMd5(null);
        file.setNoofchunks(1);

        return fileRepo.save(file);
    }



}
