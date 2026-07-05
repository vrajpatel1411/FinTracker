package org.vrajpatel.personalexpense.responseDto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Getter @Setter
public class CategoriesDTO implements Serializable {
    private Map<String, BigDecimal> categories;

    public CategoriesDTO() {
        categories = new HashMap<>();
    }
}
