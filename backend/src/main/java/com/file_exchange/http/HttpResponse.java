package com.file_exchange.http;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class HttpResponse {
    private int statusCode;
    private String statusText;
    private String contentType;
    private byte[] body;
    private Map<String, String> headers = new HashMap<>();

  public  static  HttpResponse ok(byte[] body, String type) {
        HttpResponse r = new HttpResponse();
        r.statusCode = 200;
        r.statusText = "OK";
        r.contentType = type;
        r.body = body;
        return r;
    }

    public static HttpResponse ok(byte[] body, String type,Map<String, String> headers) {
        HttpResponse r = ok(body, type);
        if (headers != null) {
            r.headers.putAll(headers);
        }
        return r;
    }

    public HttpResponse withHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public static HttpResponse notFound() {
        HttpResponse r = new HttpResponse();
        r.statusCode = 404;
        r.statusText = "Not Found";
        r.contentType = "text/plain";
        r.body = "Not Found".getBytes();
        return r;
    }

    public static HttpResponse serverError() {
        HttpResponse r = new HttpResponse();
        r.statusCode = 500;
        r.statusText = "Internal Server Error";
        r.contentType = "text/plain";
        r.body = "Server Error".getBytes();
        return r;
    }

    public static HttpResponse badRequest(String message) {
        HttpResponse r = new HttpResponse();
        r.statusCode = 400;
        r.statusText = "Bad Request";
        r.contentType = "text/plain";
        r.body = message.getBytes();
        return r;
    }

    public static HttpResponse forbidden() {
        HttpResponse r = new HttpResponse();
        r.statusCode = 403;
        r.statusText = "Forbidden";
        r.contentType = "text/plain";
        r.body = "Access Denied".getBytes();
        return r;
    }
}

