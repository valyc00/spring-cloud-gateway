package it.eng.t2lab.adap.gatesender.ws.config;

import javax.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import it.eng.t2lab.adap.gatesender.ws.service.EstrattoContoAppImpl;


@Configuration
public class ApplicationConfig {


	@Bean
	public ServletRegistrationBean<CXFServlet> dispatcherServlet() {
		return new ServletRegistrationBean<CXFServlet>(new CXFServlet(), "/EstrattoContoAppImpl/*");
	}

	@Bean(name=Bus.DEFAULT_BUS_ID)
	public SpringBus springBus(LoggingFeature loggingFeature) {

		SpringBus cxfBus = new  SpringBus();
		cxfBus.getFeatures().add(loggingFeature);
		return cxfBus;
	}

	@Bean
	public LoggingFeature loggingFeature() {

		LoggingFeature loggingFeature = new LoggingFeature();
		loggingFeature.setPrettyLogging(true);

		return loggingFeature;
	}

	@Bean
	public Endpoint endpoint(Bus bus, EstrattoContoAppImpl estrattoContoAppImpl) {

		EndpointImpl endpoint = new EndpointImpl(bus, estrattoContoAppImpl);
		endpoint.publish("/EstrattoContoService");

		return endpoint;
	}


	@Bean
	public RestTemplateBuilder restTemplateBuilder() {
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();

		//		
		//		restTemplateBuilder.setConnectTimeout(Duration.ofMillis(connectionTimeout));
		//		restTemplateBuilder.setReadTimeout(Duration.ofMillis(readTimeout));

		return restTemplateBuilder;
	}

}