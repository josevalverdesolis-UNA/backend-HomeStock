package cr.ac.una.homestock.data.repository

import cr.ac.una.homestock.data.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProductJpaRepository: JpaRepository<ProductEntity, Long>

