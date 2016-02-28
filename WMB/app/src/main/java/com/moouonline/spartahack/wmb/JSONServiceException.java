package com.moouonline.spartahack.wmb;

/**
 * Created by perrych2 on 2/27/16.
 */
public class JSONServiceException extends RuntimeException {
    public JSONServiceException() {
      super();
    }

    public JSONServiceException(String message) {
        super(message);
    }

    public JSONServiceException(String message, Throwable thrown) {
        super(message, thrown);
    }

    public JSONServiceException(Throwable thrown) {
        super(thrown);
    }

}
