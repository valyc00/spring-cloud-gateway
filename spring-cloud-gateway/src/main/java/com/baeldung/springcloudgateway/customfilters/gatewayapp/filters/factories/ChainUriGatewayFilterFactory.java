package com.baeldung.springcloudgateway.customfilters.gatewayapp.filters.factories;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Locale.LanguageRange;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class ChainUriGatewayFilterFactory extends AbstractGatewayFilterFactory<ChainUriGatewayFilterFactory.Config> {

    final Logger logger = LoggerFactory.getLogger(ChainUriGatewayFilterFactory.class);

    private final WebClient client;

    public ChainUriGatewayFilterFactory(WebClient client) {
        super(Config.class);
        this.client = client;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("languageServiceEndpoint", "defaultLanguage");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return client.get()
                //.uri(config.getLanguageServiceEndpoint())
            	// .uri((String)exchange.getAttribute("URL_TO_SEND"))
            		.uri(getUri(exchange))
                .exchange()
                .flatMap(response -> {
                    return (response.statusCode()
                        .is2xxSuccessful()) ? response.bodyToMono(String.class) : Mono.just(config.getDefaultLanguage());
                })
                
                .map(range -> {
                   
                    return exchange;
                })
                .flatMap(chain::filter);

        };
    }

    private String getUri(ServerWebExchange exchange) {
		// TODO Auto-generated method stub
    	Map<String, Object> attributes = exchange.getAttributes();
    	System.out.println(attributes);
    	String attribute = exchange.getAttribute("URL_TO_SEND");
    	System.out.println("setting url:"+attribute);
    	return attribute;
	}

	public static class Config {
        private String languageServiceEndpoint;
        private String defaultLanguage="2";

        public Config() {
        }

        public String getLanguageServiceEndpoint() {
            return languageServiceEndpoint;
        }

        public void setLanguageServiceEndpoint(String languageServiceEndpoint) {
            this.languageServiceEndpoint = languageServiceEndpoint;
        }

        public String getDefaultLanguage() {
            return defaultLanguage;
        }

        public void setDefaultLanguage(String defaultLanguage) {
            this.defaultLanguage = defaultLanguage;
        }
    }
}
