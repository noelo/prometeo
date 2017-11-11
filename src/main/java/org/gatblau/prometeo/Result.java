package org.gatblau.prometeo;

public class Result {
    private String _result;
    private Event _error;

    public Result() {
        _result = "OK";
    }

    public Result(String errorMsg) {
        _result = errorMsg;
    }

    public Result(Event error) {
        _result = "ERROR";
        _error = error;
    }

    public String getResult() {
        return _result;
    }

    public void setResult(String value) {
        _result = value;
    }

    public Event getError() {
        return _error;
    }

    public void setError(Event error) {
        _error = error;
    }
}
