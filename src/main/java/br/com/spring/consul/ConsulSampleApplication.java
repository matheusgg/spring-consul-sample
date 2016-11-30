package br.com.spring.consul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ConsulSampleApplication {

	public static void main(final String[] args) {
		SpringApplication.run(ConsulSampleApplication.class, args);
	}
}
