package com.file_exchange.http;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpResponse {
    private int statusCode;
    private String statusText;
    private String contentType;
    private byte[] body;

  public  static  HttpResponse ok(byte[] body, String type) {
        HttpResponse r = new HttpResponse();
        r.statusCode = 200;
        r.statusText = "OK";
        r.contentType = type;
        r.body = body;
        return r;
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

