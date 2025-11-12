package com.one.utils;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageBundles {

	private static final Properties appMessages = new Properties();

	private static final String MSG_PROP_FILE = "classpath:/bundles/application_message.properties";

	private static final String ERR_FILE_LOAD_FAILED = "Unable to load application_message.properties file.";

	static {
		appMessages.clear();
		loadApplicationMessages();
	}

	public static void loadApplicationMessages() {

		try {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource resource = resolver.getResource(MSG_PROP_FILE);
			appMessages.load(resource.getInputStream());
		} catch (IOException ex) {
			log.error(ERR_FILE_LOAD_FAILED);
			throw new RuntimeException(ERR_FILE_LOAD_FAILED, ex);
		}
	}

	public static String getMessage(String propertyName) {
		String str = appMessages.getProperty(propertyName);
		if (Utils.isEmpty(str)) {
			log.error(propertyName + " - Property Not Found");
			return propertyName;
		}
		return str;
	}
}
