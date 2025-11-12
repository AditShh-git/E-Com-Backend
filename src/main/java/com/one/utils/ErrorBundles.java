package com.one.utils;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorBundles {

	private static final Properties appErrors = new Properties();

	private static final String ERR_PROP_FILE = "classpath:/bundles/application_errors.properties";

	private static final String ERR_FILE_LOAD_FAILED = "Unable to load application_errors.properties file.";

	static {
		// CLEAR PROPERTIES
		appErrors.clear();

		loadApplicationErrors();

	}

	public static void loadApplicationErrors() {

		try {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource resource = resolver.getResource(ERR_PROP_FILE);
			appErrors.load(resource.getInputStream());
		} catch (IOException ex) {
			log.error(ERR_FILE_LOAD_FAILED);
			throw new RuntimeException(ERR_FILE_LOAD_FAILED, ex);
		}
	}

	public static String getProperty(String propertyName) {
		String str = appErrors.getProperty(propertyName);
		if (Utils.isEmpty(str)) {
			str = propertyName;
		}
		return str;
	}
}
