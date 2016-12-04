package br.com.spring.consul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import br.com.spring.consul.properties.ConsulSampleProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(ConsulSampleProperties.class)
public class ConsulSampleApplication {

	public static void main(final String[] args) {
		SpringApplication.run(ConsulSampleApplication.class, args);
	}
}
