package com.baeldung.springcloudgateway.customfilters.gatewayapp.filters.global;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FirstPreLastPostGlobalFilter implements GlobalFilter, Ordered {

    final Logger logger = LoggerFactory.getLogger(FirstPreLastPostGlobalFilter.class);
    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("First Pre Global Filter");
        // ServerHttpRequest request = exchange.getRequest();
        
        ServerHttpRequest request = (ServerHttpRequest) exchange.getRequest();
        Flux<DataBuffer> xmlReqFlux = request.getBody();	
        
     
        
//        @SuppressWarnings("deprecation")
//		ServerRequest serverRequest = new DefaultServerRequest(exchange);
//        serverRequest.bodyToMono(String.class).subscribe(s -> {
//          System.out.println("Requesttttttttttttttttttttt :  " + s);
//        });
        
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                logger.info("Last Post Global Filter");
            }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
    
    
    private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest){
        // Get the request body
		Flux<DataBuffer> body = serverHttpRequest.getBody();
		StringBuilder sb = new StringBuilder();
		
		body.subscribe(buffer -> {
		   byte[] bytes = new byte[buffer.readableByteCount()];
		   buffer.read(bytes);
		   DataBufferUtils.release(buffer);
		   String bodyString = new String(bytes, StandardCharsets.UTF_8);
		   sb.append(bodyString);
		});
		return sb.toString();

}
    

 
}
