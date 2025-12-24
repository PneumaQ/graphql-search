package com.example.graphql.platform.filter;

import lombok.Data;
import java.util.List;

@Data
public class SearchConditionInput {
    private String field;
    private String eq;
    private String contains;
    private String startsWith;
    private List<String> in;
    private Double gt;
    private Double lt;
    private Double gte;
    private Double lte;
    private List<SearchConditionInput> and;
    private List<SearchConditionInput> or;
    private SearchConditionInput not;
}
