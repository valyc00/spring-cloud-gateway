/**
 * 
 */
package com.baeldung.springcloudgateway.customfilters.gatewayapp.filters.factories;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.validation.constraints.NotEmpty;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;


/**
 * @author Philippe
 *
 */
// @Component
@Order(2)
public class GoldenCustomerRoutePredicateFactory extends AbstractRoutePredicateFactory<GoldenCustomerRoutePredicateFactory.Config> {

    
    public GoldenCustomerRoutePredicateFactory( ) {
        super(Config.class);
        
    }


    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("isGolden","customerIdCookie");
    }


    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        
        return (ServerWebExchange t) -> {
        	
    		ServerRequest serverRequest = new DefaultServerRequest(t);
             serverRequest.bodyToMono(String.class).subscribe(s -> {
            System.out.println("Requesttttttttttttttttttttt :  " + s);
            
            });
             
        	Map<String, Object> attributes = t.getAttributes();
        	System.out.println(attributes);
        	String attribute = t.getAttribute("URL_TO_SEND");
        	System.out.println("setting url:"+attribute);
        	if(config.getCustomerIdCookie().equals(attribute)){
        		return true;
        	}
        	else {
        		return false;
        	}
        };
    }
    
    
    @Validated
    public static class Config {        
        boolean isGolden = true;
        
        @NotEmpty
        String customerIdCookie = "customerId";
        
        
        public Config() {}
        
        public Config( boolean isGolden, String customerIdCookie) {
            this.isGolden = isGolden;
            this.customerIdCookie = customerIdCookie;
        }
        
        public boolean isGolden() {
            return isGolden;
        }
        
        public void setGolden(boolean value) {
            this.isGolden = value;
        }

        /**
         * @return the customerIdCookie
         */
        public String getCustomerIdCookie() {
            return customerIdCookie;
        }

        /**
         * @param customerIdCookie the customerIdCookie to set
         */
        public void setCustomerIdCookie(String customerIdCookie) {
            this.customerIdCookie = customerIdCookie;
        }
        
        
        
    }
    
}