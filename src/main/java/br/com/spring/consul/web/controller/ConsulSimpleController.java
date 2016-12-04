package br.com.spring.consul.web.controller;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.spring.consul.properties.ConsulSampleProperties;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(method = GET, produces = TEXT_PLAIN_VALUE)
public class ConsulSimpleController {

	private final ConsulSampleProperties properties;

	@RequestMapping("/hello")
	public String hello() {
		return this.properties.getName() + " " + this.properties.getSpecificProperty();
	}

}
