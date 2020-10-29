package it.eng.t2lab.adap.gatesender.ws.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.phase.Phase;

public class ComAroundSoapResponceInterceptor  
        extends  AbstractSoapInterceptor {

    public ComAroundSoapResponceInterceptor() {
        super(Phase.PREPARE_SEND);
    }

    public void handleMessage(SoapMessage message) {
    	//System.out.println("handleMessage");
        Map<String, String> hmap = new HashMap<String, String>();
        hmap.put("S", "http://schemas.xmlsoap.org/soap/envelope/");
                message.put("soap.env.ns.map", hmap);
                message.put("disable.outputstream.optimization", true);
    }
}