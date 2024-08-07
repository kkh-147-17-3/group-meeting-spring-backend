package com.sideprj.groupmeeting.dto;

public record CustomResponse<T>(int code, T data, String message) {
}
