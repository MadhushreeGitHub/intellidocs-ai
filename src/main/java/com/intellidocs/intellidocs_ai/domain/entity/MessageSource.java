package com.intellidocs.intellidocs_ai.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "message_sources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "chunk_id", nullable=false)
    private UUID chunkId;

    private Double score;

}
