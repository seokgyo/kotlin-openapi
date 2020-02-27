package com.example.demo

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class SwaggerController {
    @GetMapping(value = ["/swagger/{*spec}", "/swagger-ui/{*spec}"])
    fun index(@PathVariable spec: String, model: Model): String {
        model.addAttribute("oas", "/oas/${spec}.yaml")
        return "swagger-ui"
    }
}
