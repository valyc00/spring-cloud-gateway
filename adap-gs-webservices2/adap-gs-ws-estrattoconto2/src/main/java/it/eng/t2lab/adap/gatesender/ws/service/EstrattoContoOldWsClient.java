
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package it.eng.t2lab.adap.gatesender.ws.service;





import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/**
 * This class was generated by Apache CXF 3.1.12
 * 2019-09-03T10:29:16.385+02:00
 * Generated source version: 3.1.12
 * 
 */

@Service
public class EstrattoContoOldWsClient  {
	
	
	@Async
    public Future<Boolean> sendMail() throws InterruptedException {
        System.out.println("sending mail..");
        Thread.sleep(1000 * 10);
        System.out.println("sending mail completed");
        return new AsyncResult<Boolean>(true);
    }
}
