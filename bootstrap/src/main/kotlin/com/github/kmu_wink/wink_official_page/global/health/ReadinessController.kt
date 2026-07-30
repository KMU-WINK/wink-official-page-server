package com.github.kmu_wink.wink_official_page.global.health

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicBoolean

@Component
class ApplicationReadiness {
    private val ready = AtomicBoolean(false)

    @EventListener(ApplicationReadyEvent::class)
    fun markReady() {
        ready.set(true)
    }

    fun isReady(): Boolean = ready.get()
}

@RestController
class ReadinessController(
    private val readiness: ApplicationReadiness,
) {
    @GetMapping("/health/ready")
    fun ready(): ResponseEntity<Void> =
        if (readiness.isReady()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }
}
