package com.baeldung.springcloudgateway.customfilters.gatewayapp.filters.factories;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.baeldung.springcloudgateway.customfilters.gatewayapp.filters.factories.LoggingGatewayFilterFactory.Config;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Filtro che permette di estrarre delle informazioni dal Body di un messaggio SOAP ed inserirle tra i parametri della transazione server, 
 * memorizzandoli nell'HashMap dell'attributo **requestParameters**
 * 
 * Il  filtro è configurabile per potersi adattare a tutte le tipologie di WS-SOAP, di seguito un esempio di configurazione:
 * 
 * 	- name: SoapBodyReader
 *           args:
 *             namespaces:
 *               mig: http://olo2olo.npfibra.it/migrazione/
 *               soapenv: http://schemas.xmlsoap.org/soap/envelope/
 *             services:
 *               OLO_NPNotification:
 *                 sourceOperator: //codiceOperatoreDonor
 *                 requestType: //tipoNotifica
 *               OLO_NPRequest:
 *                 sourceOperator: //codiceOperatoreRec
 *                 requestType: //tipoNotifica
 * 
 * In automatico inserisce il parametro **ServiceName** con il nome del servizio SOAP invocato (nome del primo tag xml contenuto nel soap:body).
 * 
 * Nella sezione **namespaces** devono essere configurati i namespaces che vengono utilizzati nelle successive espressioni xpath. Mel precedente esempio
 * vengono configurati 2 namespaces **soapenv** e **mig** con il rispettivo URI univoco. Nell'esempio in realtà non è necessario definirli in quanto nelle
 * espressioni xpath successive non vengono utilizzati, ma sono stati introdotti per riportare un esempio.
 * 
 * Nella sezione **services** vengono configurati gli eventuali parametri da estrarre e la relativa espressione XPATH con cui estrarli.
 * Sono suddivici per **ServiceName** (Es.: OLO_NPNotification, OLO_NPRequest).<br>
 * Per ogni ServiceName viene riportata una serie di coppie chiavi-valore dove la chiave rappresenta il nome del parametro ed il valore l'espressione XPATH con
 * cui estrarre il valore
 * 
 * @author danlanzi
 *
 */
@Component
@Order(-1)
public class SoapBodyReaderGatewayFilterFactory extends AbstractGatewayFilterFactory<SoapBodyReaderGatewayFilterFactory.Config> {

	@Autowired
	protected RouteLocatorBuilder localRouteLocatorBuilder;

	/**
	 * default HttpMessageReader
	 */
	private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public SoapBodyReaderGatewayFilterFactory() {
		super(Config.class);
	}

	public static class Config {
		/**
		 * Definizione dei namespaces utilizzati negli XPATH
		 */
		
		
		
		
		private HashMap<String,String> namespaces;

		private HashMap<String,HashMap<String,String>> services;
		
		public HashMap<String, String> getNamespaces() {
			return namespaces;
		}

		public void setNamespaces(HashMap<String, String> namespaces) {
			this.namespaces = namespaces;
		}

		public HashMap<String, HashMap<String, String>> getServices() {
			return services;
		}

		public void setServices(HashMap<String, HashMap<String, String>> services) {
			this.services = services;
		}
		
	}

	@Override
	public GatewayFilter apply(Config config) {
		// grab configuration from Config object
		return (exchange, chain) -> {

			

			ServerHttpRequest request = exchange.getRequest();

			if (request.getMethod() == HttpMethod.POST) {
				
				return readBody(exchange, chain, config);
			}

			return chain.filter(exchange);
		};
	}

	private  Mono<Void> readBody(ServerWebExchange exchange, GatewayFilterChain chain, Config config) {
		/**
		 * join the body
		 */
		return DataBufferUtils.join(exchange.getRequest().getBody())
				.flatMap(dataBuffer -> {
					/*
					 * read the body Flux<DataBuffer>, and release the buffer
					 * //TODO when SpringCloudGateway Version Release To G.SR2,this can be update with the new version's feature
					 * see PR https://github.com/spring-cloud/spring-cloud-gateway/pull/1095
					 */
					byte[] bytes = new byte[dataBuffer.readableByteCount()];
					dataBuffer.read(bytes);
					DataBufferUtils.release(dataBuffer);
					Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
						DataBuffer buffer =
								exchange.getResponse().bufferFactory().wrap(bytes);
						DataBufferUtils.retain(buffer);
						return Mono.just(buffer);
					});
					/**
					 * repackage ServerHttpRequest
					 */
					ServerHttpRequest mutatedRequest =
							new ServerHttpRequestDecorator(exchange.getRequest()) {
						@Override
						public Flux<DataBuffer> getBody() {
							return cachedFlux;
						}
					};
					/**
					 * mutate exchage with new ServerHttpRequest
					 */
					ServerWebExchange mutatedExchange =
							exchange.mutate().request(mutatedRequest).build();
					/**
					 * read body string with default messageReaders
					 */
					return ServerRequest.create(mutatedExchange, messageReaders)
							.bodyToMono(String.class)
							.doOnNext(objectValue -> {
								
								
								String requestBody = objectValue;
								
								System.out.println("requestBody:"+requestBody);
								
								
								if(requestBody.contains("vale")) {
									
							
									mutatedExchange.getAttributes().put("URL_TO_SEND", "http://localhost:8092");
								}
								else {
									mutatedExchange.getAttributes().put("URL_TO_SEND", "http://localhost:8090");
								}
								
								
//								String GATEWAY_REQUEST_URL_ATTR="aa";
//								Map<String, Object> attributes = exchange.getAttributes();
//								
//								
//								Route route = exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute");
//								URI uri = route.getUri();
//								System.out.println("URI:"+uri);
//								
//								String target = exchange.getRequest().getURI().toString();
//								System.out.println("target:"+target);
//								
//								Route route2  = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
//								
//
//								Object attribute1 = exchange.getAttribute("service_route");
//								
//								//exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, new URI("https://www.google.com"));
//								mutatedExchange.getAttributes().put("pippo", "pluto");
//								
//								attributes = exchange.getAttributes();
//								System.out.println(route);
//								
//								
//								
//								InputStream is = new ByteArrayInputStream(((String)objectValue).getBytes());
//								try {
//									SOAPMessage request = MessageFactory.newInstance().createMessage(null, is);
//
//									final Iterator<?> bodyElements = request.getSOAPPart().getEnvelope().getBody().getChildElements();
//									while (bodyElements.hasNext()) {
//										Object element = bodyElements.next();
//										if (element instanceof SOAPElement) {
//											SOAPElement soapElement = (SOAPElement) element;
//
//											String serviceName = soapElement.getElementName().getLocalName();
//											
//											 
//											
//												// Se sono configurati dei dati da estrarre per il service Name
//										
//											
//											XPath xPath = XPathFactory.newInstance().newXPath();
//											
//
//										}
//									}
//
//								} catch (IOException | SOAPException   e) {
//									
//								} 

								

							}).then(chain.filter(mutatedExchange));
				});

	}


}

