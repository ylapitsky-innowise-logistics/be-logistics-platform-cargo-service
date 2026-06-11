package com.innowise.logistics.cargoservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;           // список элементов
    private int currentPage;         // текущая страница (с 0)
    private int pageSize;            // размер страницы
    private long totalItems;         // всего элементов
    private int totalPages;          // всего страниц
    private boolean hasNext;         // есть ли следующая
    private boolean hasPrevious;     // есть ли предыдущая

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
