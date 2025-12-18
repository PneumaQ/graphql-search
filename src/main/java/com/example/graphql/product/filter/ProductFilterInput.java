package com.example.graphql.product.filter;

import com.example.graphql.platform.filter.StringFilter;
import java.util.List;

public record ProductFilterInput(
    StringFilter name,
    List<ProductAttributeFilterInput> attributes
) {}
