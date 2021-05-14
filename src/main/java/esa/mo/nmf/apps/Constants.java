package esa.mo.nmf.apps;

public final class Constants {

    // hide constructor to restrict instantiation
    private Constants() {}

    // experiment id
    public static final int EXPERIMENT_ID = 888;
    
    // error codes
    // FIXME: throw exceptions instead
    public static final int ERROR_LISTING_PARAMETERS_TO_FETCH                       = 1;
    public static final int ERROR_LISTING_AGGREGATIONS_UNKNOWN                      = 2;
    public static final int ERROR_LISTING_AGGREGATIONS                              = 3;
    public static final int ERROR_UPDATING_AGGREGATION                              = 4;
    public static final int ERROR_ADDAGGREGATION_DID_NOT_RETURN_AGGREGATION_ID      = 5;
    public static final int ERROR_CREATING_AGGREGATION                              = 6;
    public static final int ERROR_DISABLING_AGGREGATION_GENERATION                  = 7;

}