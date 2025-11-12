package com.one.aim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.bo.FileBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.service.FileService;
import com.one.vm.core.BaseRs;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class FileController {

	@Autowired
	FileService fileService;

	@Value("${dt.file.allow-content-types}")
	private List<String> ALLOW_CONTENT_TYPES;

	@PostMapping(value = "auth/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing REST Service - [POST /file/upload]");
		}

		if (!ALLOW_CONTENT_TYPES.contains(multipartFile.getContentType())) {
			log.error("Invalid Content Type - " + multipartFile.getContentType());
			log.error(ErrorCodes.EC_ALLOWED_FILE_TYPES);
			// throw new AllowedFileTypeException(ErrorCodes.EC_ALLOWED_FILE_TYPES);
			throw new Exception(ErrorCodes.EC_ALLOWED_FILE_TYPES);
		}

		BaseRs baseRs = fileService.uploadFile(multipartFile);
		return new ResponseEntity<>(baseRs, HttpStatus.OK);
	}

//    @PostMapping(value = "/file/uploadpdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> uploadPdfFile(@RequestParam("file") MultipartFile multipartFile)
//                    throws Exception {
//
//        if (log.isDebugEnabled()) {
//            log.debug("Executing REST Service - [POST /file/upload]");
//        }
//
//        if ((!ContentCheckUtils.isPdf(multipartFile.getContentType()))
//                        && (!ContentCheckUtils.isDocument(multipartFile.getContentType()))) {
//            log.error("Invalid Content Type - " + multipartFile.getContentType());
//            log.error(ErrorCodes.EC_ALLOWED_PDF_DOC_FILE);
//            throw new Exception(ErrorCodes.EC_ALLOWED_PDF_DOC_FILE);
//            //throw new AllowedPdfFileTypeException(ErrorCodes.EC_ALLOWED_PDF_DOC_FILE);
//        }
//
//        BaseRs baseRs = fileService.uploadFile(multipartFile);
//        return new ResponseEntity<>(baseRs, HttpStatus.OK);
//    }

    @GetMapping("/api/files/{id}/view")
	public ResponseEntity<?> downloadFileById(HttpServletResponse response, @PathVariable String fileId)
			throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing REST Service - [GET /file/{fileId}/download]");
		}

		byte[] bytes = new byte[0];
		FileBO fileBO = fileService.downloadFile(fileId);
		if (null != fileBO) {
			response.setContentType(fileBO.getContenttype());
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileBO.getName() + "\"");
			response.setHeader("Filename", fileBO.getName());
			response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
			response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Filename, Content-Type");

			bytes = fileBO.getInputstream();
		}
		return new ResponseEntity<>(bytes, HttpStatus.OK);
	}

	@DeleteMapping("auth/file/{fileId}")
	public ResponseEntity<?> deleteFileById(@PathVariable String fileId) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing REST Service - [DELETE /file/{fileId}]");
		}

		BaseRs baseRs = fileService.deleteFileById(fileId);
		return new ResponseEntity<>(baseRs, HttpStatus.OK);
	}

	@GetMapping("auth/file/img/{fileId}")
	public ResponseEntity<?> getImg(@PathVariable String fileId) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing REST Service - [GET /file/img/{fileId}]");
		}

		byte[] bytes = fileService.getContentFromGridFS(fileId);
		return new ResponseEntity<>(bytes, HttpStatus.OK);
	}
}
