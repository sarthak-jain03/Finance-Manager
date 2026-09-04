package com.sarthak.finance.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarthak.finance.model.TransactionType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private TransactionType type;

    @JsonIgnore
    private boolean defaultCategory;

    public CategoryResponse(Long id, String name, TransactionType type, boolean defaultCategory) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.defaultCategory = defaultCategory;
    }

    @JsonProperty("isDefault")
    public boolean isDefaultCategory() {
        return defaultCategory;
    }

    @JsonProperty("custom")
    public boolean isCustom() {
        return !defaultCategory;
    }
}
