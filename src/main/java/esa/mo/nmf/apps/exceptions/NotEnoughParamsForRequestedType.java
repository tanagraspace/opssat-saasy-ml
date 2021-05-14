package esa.mo.nmf.apps.exceptions;

public class NotEnoughParamsForRequestedType extends Exception {
    private static final long serialVersionUID = 1L;

    public NotEnoughParamsForRequestedType() {
        super();
    }

    public NotEnoughParamsForRequestedType(String errorMessage) {
        super(errorMessage);
    }
    
    public NotEnoughParamsForRequestedType(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
