package com.roboter.roboterapi.dto;

import com.roboter.roboterapi.model.RobotAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.hateoas.Link;
import java.util.List;

@Data
@AllArgsConstructor // Create constructor with all fields
public class PagedActionsResponse {

    private PageInfo page;
    private List<RobotAction> actions;
    private List<Link> links; // Links for "next" and "previous"
    
}