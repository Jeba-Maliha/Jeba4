package com.dsinnovators.keyword.driven.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@Slf4j
public class RestApiCall {

    Boolean responseValidated = false;

    public void init(){
        responseValidated = false;
    }

    public Boolean assertJSON(){
        return responseValidated;
    }

    public Map<String, String> getRequest(String apiUrl, String expectedResponse, MultivaluedMap headerMap,String  keyword) {
        String responseBody = "";
        Map<String,String> map = new HashMap<>();
        try {
            Client client = ClientBuilder.newClient();
            responseBody = client.target(apiUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .headers(headerMap)
                    .get(String.class);
            log.info("get response:: "+responseBody);

            if (expectedResponse != null && keyword.equals("rest")) {
                JSONObject obj = new JSONObject(responseBody);
                JSONObject expectedResponseObject= new JSONObject(expectedResponse);
                validateJsonObject(obj, expectedResponseObject);
                log.info("is response validated :: "+responseValidated);
            }
            else if((expectedResponse != null && keyword.equals("reverse_rest"))){
                JSONObject obj = new JSONObject(responseBody);
                JSONObject expectedResponseObject= new JSONObject(expectedResponse);
                responseValidated = jsonCompare(expectedResponseObject,obj);
            }
            else{
                responseValidated = true;
            }

            map.put("responseBody",responseBody);
            map.put("responseValidated",responseValidated ? "true": "false");

        } catch (Exception ex){
            log.info("exception in rest GET call");
            map.put("responseBody",ex.getMessage());
            map.put("responseValidated","false");
        }
        return map;

    }



    public Boolean postRequest(String apiUrl, String payload,MultivaluedMap headerMap){
        String response ="";
        try {
            Client client = ClientBuilder.newClient();
            response = client.target(apiUrl)
                .request(MediaType.APPLICATION_JSON)
                .headers(headerMap)
                .post(Entity.json(payload),String.class);
        } catch (Exception ex){
            log.info("exception in rest POST call");
            return false;
        }
        log.info("Post response:: "+response);
        return true;
    }

    public Boolean putRequest(String apiUrl, String payload,MultivaluedMap headerMap){
        String responseBody = "";
        try {
            Client client = ClientBuilder.newClient();
            responseBody = client.target(apiUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .headers(headerMap)
                    .put(Entity.json(payload),String.class);
        } catch (Exception ex){
            log.info("exception in rest PUT call");
            return false;
        }
        log.info("Put response:: "+responseBody);
        return true;
    }

    public Boolean deleteRequest(String apiUrl, MultivaluedMap headerMap) throws JSONException {
        String responseBody = "";

        try {
            Client client = ClientBuilder.newClient();
            responseBody = client.target(apiUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .headers(headerMap)
                    .delete(String.class);

        } catch (Exception ex){
            log.info("exception in rest DELETE call");
            return false;
        }
        log.info("delete response:: "+responseBody);
        return true;
    }


    public void validateJsonObject(JSONObject jsonObj, JSONObject expectedResponseObject) throws JSONException, JsonProcessingException {

        Iterator<?> jsonKeys = jsonObj.keys();
        while (jsonKeys.hasNext()) {
            String keyStr = (String) jsonKeys.next();
            try {
                Object keyvalue = jsonObj.get(keyStr);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(keyStr, keyvalue);
                if (jsonCompare(jsonObject, expectedResponseObject)) {
                    log.info("JRLOG:: json object matched");
                    responseValidated = true;
                    return;
                } else if (keyvalue instanceof JSONObject)
                    validateJsonObject((JSONObject) keyvalue, expectedResponseObject);
                else if (keyvalue instanceof JSONArray) {
                    JSONArray arr = (JSONArray) keyvalue;
                    for (int i = 0; i < arr.length(); i++) {
                        Object arrObj = arr.get(i);
                        if (arrObj instanceof JSONObject) {
                            validateJsonObject((JSONObject) arrObj, expectedResponseObject);
                        }
                    }
                }
            } catch (Exception ex) {
                log.info("Exception in validateJsonObject() :: "+ex.getMessage());
            }
        }
    }


    public  Boolean jsonCompare(JSONObject jsonObj, JSONObject expectedResponseObject) throws JsonProcessingException {
        jsonObj.remove("version");
        expectedResponseObject.remove("version");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree1 = mapper.readTree(jsonObj.toString());
        JsonNode tree2 = mapper.readTree(expectedResponseObject.toString());
        return tree1.equals(tree2);
    }


}
