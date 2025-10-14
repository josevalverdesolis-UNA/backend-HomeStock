package cr.ac.una.homestock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackendHomeStockApplication

fun main(args: Array<String>) {
    runApplication<BackendHomeStockApplication>(*args)
}

