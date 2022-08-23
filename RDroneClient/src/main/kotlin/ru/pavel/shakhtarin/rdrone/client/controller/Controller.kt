package ru.pavel.shakhtarin.rdrone.client.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.pavel.shakhtarin.rdrone.client.service.DroneService

@RestController
class Controller(private val droneService: DroneService) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/")
    fun initDrones(@RequestParam amountOfDrones: Int) {
        logger.info("Amount: $amountOfDrones")
        droneService.initDrones(amountOfDrones)
    }

}