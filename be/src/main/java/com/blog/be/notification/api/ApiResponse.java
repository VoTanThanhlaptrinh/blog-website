package com.blog.be.notification.api;

public record ApiResponse<T> (T data, String message, int code){}
