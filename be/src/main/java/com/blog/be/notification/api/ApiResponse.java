package com.blog.be.common.api;

public record ApiResponse<T> (T data, String message, int code){}
