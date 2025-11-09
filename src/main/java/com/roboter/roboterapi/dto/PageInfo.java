package com.roboter.roboterapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor // Erstellt einen Konstruktor mit allen Feldern
public class PageInfo {

    private int number;        // Aktuelle Seitenzahl (z.B. 1)
    private int size;          // Elemente pro Seite (z.B. 5)
    private long totalElements; // Gesamtzahl aller Aktionen (z.B. 20)
    private int totalPages;    // Gesamtanzahl der Seiten (z.B. 4)
    private boolean hasNext;
    private boolean hasPrevious;

    // Hilfsmethode, um ein PageInfo-Objekt aus einem Spring "Page"-Objekt zu erstellen
    public static PageInfo fromPage(Page<?> page) {
        return new PageInfo(
            page.getNumber() + 1, // Spring Page ist 0-basiert, die API soll 1-basiert sein
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}