package br.com.spring.consul.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("br.com.spring.consul.properties")
public class ConsulSampleProperties {

	private String name;
	private String specificProperty;
}
