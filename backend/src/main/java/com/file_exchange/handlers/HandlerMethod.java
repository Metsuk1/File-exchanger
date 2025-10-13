package com.file_exchange.handlers;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Getter
@Setter
public class HandlerMethod {
    final Object controller;
    final Method method;
    final String path;
    final String httpMethod;

    public HandlerMethod(Object controller, Method method, String path, String httpMethod) {
        this.controller = controller;
        this.method = method;
        this.path = path;
        this.httpMethod = httpMethod;
    }

}
