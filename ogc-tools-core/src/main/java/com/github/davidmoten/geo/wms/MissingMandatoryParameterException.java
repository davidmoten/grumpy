package com.github.davidmoten.geo.wms;

public class MissingMandatoryParameterException extends Exception {

    private static final long serialVersionUID = -9206288232141856630L;

    public MissingMandatoryParameterException(String parameter) {
        super(parameter + " is a mandatory parameter and was missing");
    }

}
