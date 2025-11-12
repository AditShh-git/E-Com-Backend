package com.one.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(info = @Info(title = "E-commerce API", version = "1.0.0", description = "This is the E-commerce API is for internal use",

		contact = @Contact(name = "One Aim Pvt. Ltd.", url = "http://www.oneaim.com/", email = "mailto:info@oneaim.com"), license = @License(name = "Copyright Aim One. All Rights Reserved"
		    , url = "https://www.Aim Pvt.com/")), servers = {
				@Server(description = "WESHOPIFY-AUTHN-SERVICE-LOCAL-ENV", url = "http://localhost:8989/aimdev") }, security = {
						@SecurityRequirement(name = "BearerAuth") })
@SecurityScheme(name = "BearerAuth", description = "JWT Authorization", bearerFormat = "JWT", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class SwaggerConfig {

}