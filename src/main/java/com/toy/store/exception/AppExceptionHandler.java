package com.toy.store.exception;

import com.toy.store.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全域異常處理器
 */
@ControllerAdvice
public class AppExceptionHandler {

    /**
     * 處理 API 異常 (回傳 JSON 或 頁面)
     */
    @ExceptionHandler(AppException.class)
    public Object handleAppException(AppException e, HttpServletRequest request) {
        return buildErrorModelAndView(e, request);
    }

    /**
     * 處理所有其他異常
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ApiResponse.error("系統錯誤: " + e.getMessage());
        }
        return buildErrorModelAndView(e, request);
    }

    private Object buildErrorModelAndView(Exception e, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ApiResponse.error(e.getMessage());
        }
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.addObject("url", request.getRequestURL());
        mav.addObject("message", e.getMessage());
        mav.setViewName("error");
        return mav;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/") || request.getHeader("X-Requested-With") != null;
    }
}
