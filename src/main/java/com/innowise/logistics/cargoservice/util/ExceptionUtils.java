package com.innowise.logistics.cargoservice.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ExceptionUtils {

    // Вспомогательный метод для получения пути
    public static String getRequestPath() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest()
                .getRequestURI();
    }

}
