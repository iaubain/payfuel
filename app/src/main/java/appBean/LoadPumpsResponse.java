package appBean;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

import models.UrlPumps;

/**
 * Created by Owner on 5/3/2016.
 */
public class LoadPumpsResponse {
    @JsonProperty("PumpDetailsModel")
    private
    List<UrlPumps> urlPumpsList;
    @JsonProperty("message")
    private
    String message;
    @JsonProperty("statusCode")
    private
    int statusCode;

    public List<UrlPumps> getUrlPumpsList() {
        return urlPumpsList;
    }

    public void setUrlPumpsList(List<UrlPumps> urlPumpsList) {
        this.urlPumpsList = urlPumpsList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
