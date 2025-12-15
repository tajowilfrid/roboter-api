package com.roboter.roboterapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
public class PageInfo {

    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    // method to create a PageInfo object from a Spring Page object
    public static PageInfo fromPage(Page<?> page) {
        return new PageInfo(
            page.getNumber() + 1, // Spring Page is 0-based and the API is 1-based
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}