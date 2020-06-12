package gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import gateway.filter.ServiceExceptionFilter;

@SpringBootApplication
@EnableZuulProxy
@EnableEurekaClient
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public ServiceExceptionFilter exceptionFilter() {
		return new ServiceExceptionFilter();
	}
}


