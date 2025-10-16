package cr.ac.una.homestock.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {
    @GetMapping("/")
    fun root(): String = "redirect:/swagger-ui"
}

