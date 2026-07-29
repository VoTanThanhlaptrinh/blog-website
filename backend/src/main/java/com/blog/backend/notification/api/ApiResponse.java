package com.blog.backend.notification.api;

public record ApiResponse<T> (T data, String message, int code){}
