package net.nemerosa.ontrack.repository.jpa

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(basePackageClasses = [JPAConfig::class])
@EntityScan("net.nemerosa.ontrack.model")
@Configuration
class JPAConfig
