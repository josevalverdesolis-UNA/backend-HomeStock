package cr.ac.una.homestock.domain.repository

import cr.ac.una.homestock.domain.model.Product

interface ProductRepository {
    fun findAll(): List<Product>
    fun findById(id: Long): Product?
    fun save(product: Product): Product
    fun deleteById(id: Long)
}

