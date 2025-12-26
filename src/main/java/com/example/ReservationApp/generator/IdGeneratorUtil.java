package com.example.ReservationApp.generator;

import java.time.Year;

import jakarta.persistence.EntityManager;

public class IdGeneratorUtil {
    
    public static String generateUserId(EntityManager entityManager, String rolePrefix) {
        
        Long seq = ((Number) entityManager
                .createNativeQuery("SELECT nextval('user_seq')")
                .getSingleResult())
                .longValue();
        String seqFormatted = String.format("%03d", seq); 
        String year = String.valueOf(Year.now().getValue());
        return rolePrefix + year + seqFormatted;
    }

    public static String generateProductId(EntityManager entityManager, String categoryCode) {
    Long seq = ((Number) entityManager
            .createNativeQuery("SELECT COALESCE(MAX(SUBSTRING(product_id, LENGTH(:code)+1)::int), 0) + 1 FROM products WHERE product_id LIKE :codePattern")
            .setParameter("code", categoryCode)
            .setParameter("codePattern", categoryCode + "%")
            .getSingleResult())
            .longValue();

    return categoryCode + String.format("%03d", seq);
}
}
