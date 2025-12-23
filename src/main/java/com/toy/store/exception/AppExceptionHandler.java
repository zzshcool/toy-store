package com.toy.store.exception;

import com.toy.store.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * 全域異常處理器
 * 針對 API 請求返回 JSON 格式，針對網頁請求返回錯誤頁面
 */
@ControllerAdvice
public class AppExceptionHandler {

    /**
     * 處理自定義業務異常 (AppException)
     */
    @ExceptionHandler(AppException.class)
    public Object handleAppException(AppException e, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
        return buildErrorModelAndView(e, request);
    }

    /**
     * 處理所有其他未捕獲異常
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        if (isApiRequest(request)) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            String message = "系統內部錯誤: " + e.getMessage();

            // 處理標籤層次的權限問題 (如果有的話)
            if (e.getMessage() != null && e.getMessage().contains("請先登入")) {
                status = HttpStatus.UNAUTHORIZED;
            }

            return ResponseEntity
                    .status(status)
                    .body(ApiResponse.error(message));
        }
        return buildErrorModelAndView(e, request);
    }

    private ModelAndView buildErrorModelAndView(Exception e, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.addObject("url", request.getRequestURL());
        mav.addObject("message", e.getMessage());
        mav.setViewName("error");
        return mav;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/") ||
                uri.contains("/draw") ||
                uri.contains("/purchase") ||
                uri.contains("/spin") ||
                uri.contains("/dig") ||
                "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
