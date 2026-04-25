package com.intellidocs.intellidocs_ai.config;


import com.pgvector.PGvector;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.springframework.context.annotation.Configuration;

public class HibernateVectorConfig {
    // pgvector registers itself automatically when the JDBC driver
    // detects the vector column type — no manual config needed for
    // basic float[] mapping with Spring AI's pgvector starter
    // This class is a placeholder for future custom type overrides
}
