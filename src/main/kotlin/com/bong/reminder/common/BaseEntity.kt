package com.bong.reminder.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    final var id: Long? = null
        private set

    @CreatedDate
    @Column(nullable = false, updatable = false)
    final var createdAt: Instant = Instant.EPOCH
        private set

    @LastModifiedDate
    @Column(nullable = false)
    final var updatedAt: Instant = Instant.EPOCH
        private set
}
