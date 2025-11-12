package com.one.aim.helper;

import java.io.File;
import java.time.LocalDate;
import java.util.UUID;

import com.one.aim.bo.AttachmentBO;
import com.one.aim.bo.FileBO;
import com.one.constants.StringConstants;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileHelper {

    private FileHelper() {
        throw new IllegalStateException("FileHelper");
    }

    public static String UPLOAD_ROOT_DIR;

    private static String DIR_CHUNKS = "chunks";

    private final static int CHUNK_SIZE = 1024 * 255;

    public static String prepareSubDir(String context, LocalDate date) {

        if (log.isDebugEnabled()) {
            log.debug("Executing prepareSubDir(context, date) ->");
        }

        try {
            String dir = StringConstants.EMPTY;
            if (Utils.isNotEmpty(context)) {
                // TODO
            }
            if (date == null) {
                date = LocalDate.now();
            }
            dir = dir + File.separator + date.getYear() + File.separator + date.getMonthValue()
                            + File.separator + date.getDayOfMonth() + File.separator
                            + UUID.randomUUID();
            return dir;
        } catch (Exception e) {
            log.error("Exception in prepareSubDir(context, date) ->" + e);
            return null;
        }
    }

    public static String prepareFileDir(String subDir) {

        if (log.isDebugEnabled()) {
            log.debug("Executing prepareFileDir(subDir) ->");
        }

        try {
            return appendSeparator(UPLOAD_ROOT_DIR, subDir);
        } catch (Exception e) {
            log.error("Exception in prepareFileDir(subDir) ->" + e);
            return StringConstants.EMPTY;
        }
    }

    public static String prepareChunksDir(String subDir) {

        if (log.isDebugEnabled()) {
            log.debug("Executing prepareChunksDir(subDir) ->");
        }

        try {
            String finalPath = appendSeparator(appendSeparator(UPLOAD_ROOT_DIR, subDir), DIR_CHUNKS);

            File dir = new File(finalPath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    log.info("Created upload directory: {}", dir.getAbsolutePath());
                } else {
                    log.warn("Could not create upload directory: {}", dir.getAbsolutePath());
                }
            }
            return finalPath;
        } catch (Exception e) {
            log.error("Exception in prepareChunksDir(subDir) ->", e);
            return StringConstants.EMPTY;
        }
    }


    public static int getChunkSize(long fileSize) {

        if (log.isDebugEnabled()) {
            log.debug("Executing getChunkSize(fileSize) ->");
        }

        try {
            if (fileSize <= CHUNK_SIZE) {
                return (int) fileSize / 2;
            }
            return CHUNK_SIZE;
        } catch (Exception e) {
            log.error("Exception in getChunkSize(fileSize) ->" + e);
            return -1;
        }
    }

    public static AttachmentBO prepareAttachmentBO(FileBO fileBO) {

        if (log.isDebugEnabled()) {
            log.debug("Executing prepareAttachmentBO(FileBO) ->");
        }

        try {
            if (fileBO == null) {
                return null;
            }
            AttachmentBO attachmentBO = new AttachmentBO();
            attachmentBO.setDocid(fileBO.getId());
            attachmentBO.setName(fileBO.getName());
            attachmentBO.setType(fileBO.getContenttype());
            return attachmentBO;
        } catch (Exception e) {
            log.error("Exception in prepareAttachmentBO(FileBO) ->" + e);
            return null;
        }
    }

    private static String appendSeparator(String prefixPath, String suffixPath) {

        if (log.isDebugEnabled()) {
            log.debug("Executing appendSeparator(PrefixPath, SuffixPath) ->");
        }
        try {
            prefixPath = Utils.getValidString(prefixPath);
            suffixPath = Utils.getValidString(suffixPath);
            if (prefixPath.endsWith(File.separator)) {
                prefixPath = prefixPath.substring(0, prefixPath.lastIndexOf(File.separator));
            }
            if (prefixPath.endsWith(StringConstants.FORWARD_SLASH)) {
                prefixPath = prefixPath.substring(0,
                                prefixPath.lastIndexOf(StringConstants.FORWARD_SLASH));
            }
            if (!suffixPath.startsWith(File.separator)) {
                suffixPath = File.separator + suffixPath;
            }
            return prefixPath + suffixPath;
        } catch (Exception e) {
            log.error("Exception in appendSeparator(PrefixPath, SuffixPath) -" + e);
            return StringConstants.EMPTY;
        }
    }

    public static boolean isFileExists(FileBO fileBO) {

        if (log.isDebugEnabled()) {
            log.debug("Executing isFileExists(FileBO) ->");
        }

        try {
            if (fileBO == null || Utils.isEmpty(fileBO.getPath())) {
                return false;
            }
            String finalPath = prepareChunksDir(fileBO.getPath());
            File folder = new File(finalPath);
            if (folder == null || !folder.exists()) {
                log.error("Folder does not exist - " + finalPath);
                return false;
            }
            File[] listOfFiles = folder.listFiles();
            if (Utils.isEmpty(listOfFiles) || listOfFiles.length != fileBO.getNoofchunks()) {
                log.error("File Chunks Not Exist - " + finalPath);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Exception In isFileExists(FileBO)");
            return false;
        }
    }

    public static boolean isFileExists(String filename) {

        if (log.isDebugEnabled()) {
            log.debug("Executing isFileExists ->");
        }

        try {
            if (filename == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Exception In isFileExists ->");
            return false;
        }
    }
}
