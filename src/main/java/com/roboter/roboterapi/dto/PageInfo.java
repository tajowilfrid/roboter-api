package com.roboter.roboterapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
public class PageInfo {

    private int number;           // Current page number (e.g. 1)
    private int size;            // Elements per page (e.g. 5)
    private long totalElements; // Total number of all actions (e.g. 20)
    private int totalPages;    // Total number of pages (e.g. 4)
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