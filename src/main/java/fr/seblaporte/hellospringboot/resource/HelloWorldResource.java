package fr.seblaporte.hellospringboot.resource;

import fr.seblaporte.hellospringboot.resource.dto.HelloWordDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldResource {

    @GetMapping
    public HelloWordDto Hello() {
        HelloWordDto helloWordDto = new HelloWordDto();
        helloWordDto.setMessage("Hello World");

        return helloWordDto;
    }

}
