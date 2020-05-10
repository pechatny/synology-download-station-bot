package com.pechatnikov.telegram.bot.dsget.controller
import com.pechatnikov.telegram.bot.dsget.models.Response
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(produces = [(MediaType.APPLICATION_JSON_VALUE)])
class IndexController {

    @GetMapping(value = ["/"])
    public fun index(): ResponseEntity<Response> {
        return ResponseEntity.ok(Response("my message"))
    }


}