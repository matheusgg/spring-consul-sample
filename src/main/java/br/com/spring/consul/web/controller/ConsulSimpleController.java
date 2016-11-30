package br.com.spring.consul.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created on 23/03/2016.
 */
@RestController
@RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
public class ConsulSimpleController {

	@Value("${someKey}")
	private String someProperty;

	@RequestMapping(method = GET, value = "property")
	public String property() {
		return this.someProperty;
	}

}
