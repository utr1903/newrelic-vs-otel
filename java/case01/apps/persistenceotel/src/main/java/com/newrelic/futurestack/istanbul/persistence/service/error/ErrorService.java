package com.newrelic.futurestack.istanbul.persistence.service.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ErrorService {

  private final Logger logger = LoggerFactory.getLogger(ErrorService.class);

  public class Calculator implements Runnable {

    private int numElems;

    public Calculator(int numElems) {
      this.numElems = numElems;
    }

    public void run() {
      for (int i = 0; i < numElems; ++i) {
        double res = Math.pow(100, 7.5) * Math.sqrt(90001) * Math.sin(12314) / Math.pow(48, 10.2);
      }
    }
  }

  public void causeError(String error) throws Exception {
    if (error.equalsIgnoreCase("validation-500")) {
      logger.error("Intentional error kicked off.");
      throw new Exception();
    } else if (error.equalsIgnoreCase("validation-cpu")) {
      int numElems = 2000000;
      logger.warn("Looping over " + numElems + ".");
      Runnable r = new Calculator(numElems);
      new Thread(r).start();
    }
  }
}
