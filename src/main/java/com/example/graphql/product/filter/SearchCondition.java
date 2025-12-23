package com.example.graphql.product.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCondition {
    private String field;
    
    // Operators
    private String eq;
    private String contains;
    private String startsWith;
    private List<String> in;
    private Double gt;
    private Double lt;
    private Double gte;
    private Double lte;
    
    // Recursive logic
    private List<SearchCondition> and;
    private List<SearchCondition> or;
    private SearchCondition not;
}