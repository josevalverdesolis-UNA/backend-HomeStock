package cr.ac.una.homestock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HomestockBackendApplication

fun main(args: Array<String>) {
	runApplication<HomestockBackendApplication>(*args)
}
