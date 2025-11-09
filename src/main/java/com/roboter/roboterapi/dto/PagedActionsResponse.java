package com.roboter.roboterapi.dto;

import com.roboter.roboterapi.model.RobotAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.hateoas.Link;
import java.util.List;

@Data
@AllArgsConstructor // Erstellt einen Konstruktor mit allen Feldern
public class PagedActionsResponse {

    private PageInfo page;
    private List<RobotAction> actions;
    private List<Link> links; // Die Links für "next" und "previous"
    
}