package it.eng.t2lab.adap.gatesender.ws.utils;

import java.net.URI;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import it.eng.t2lab.adap.gatesender.ws.service.Employee;
import it.telecomitalia.ec.presentation.EcRequestType;
import it.telecomitalia.ec.presentation.EcResponseType;
import it.telecomitalia.ec.presentation.EcResponseType.EventCdCList;
import it.telecomitalia.ec.presentation.ErrorResponseType;
import it.telecomitalia.ec.presentation.EsitoType;
import it.telecomitalia.ec.presentation.EventCdCType;

@Service
public class RestSender {
	
	
	@Value( "${adap.gs.rest.url}" )
	private String urlRest;
	
	@Value( "${adap.gs.rest.connection.timeout}" )
	private int connectionTimeout;
	
	@Value( "${adap.gs.rest.read.timeout}" )
	private int readTimeout;
	
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	private final static Logger logger = LoggerFactory.getLogger(RestSender.class);
	

	public EcResponseType send(EcRequestType ecRequest) {
		
		logger.debug("send");
		
		
		it.telecomitalia.ec.presentation.EcResponseType resp =new EcResponseType();
    	
		resp.setEsito(EsitoType.OK );
		
		
		logger.debug("urlRest:" + urlRest);
		String body;
		URI uri = null;
		try {

			Employee employee = new Employee();
			employee.setId(3);
			employee.setName("ficooo");

			uri = new URI(urlRest);

			RestTemplate restTemplate = restTemplateBuilder.build();
			
			 restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
		        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
		                .getRequestFactory();
		        rf.setReadTimeout(readTimeout);
		        rf.setConnectTimeout(connectionTimeout);
			
			
			
		    logger.debug("before call:");
		    long start = System.currentTimeMillis();
			ResponseEntity<String> result = restTemplate .postForEntity(uri, employee, String.class);
			 long stop = System.currentTimeMillis(); 
			logger.debug("receive:" + result+": time::"+(stop-start));
			body = result.getBody();
			
			
			EventCdCList cdList = new  EventCdCList();
			List<EventCdCType> eventCdC = cdList.getEventCdC();
			EventCdCType eventCdCType = new EventCdCType();
			GregorianCalendar gcal = new GregorianCalendar();
			XMLGregorianCalendar xgcal = null;
			try {
				xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			} catch (DatatypeConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			eventCdCType.setActivationDateTime(xgcal );
			eventCdCType.setCategoryDescription(body);
			eventCdCType.setEcEventID("ID");
			eventCdCType.setMSISDN("1234567");
			
			
			eventCdC.add(eventCdCType );
			resp.setEventCdCList(cdList);
			
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resp.setEsito(EsitoType.ERROR );
			ErrorResponseType error = new ErrorResponseType();
			error.setErrorCode("01");
			error.setErrorDescription("err connection");
			resp.setErrorResponse(error );
			
			return resp;
		}

		
		return resp;
	

	}

}
