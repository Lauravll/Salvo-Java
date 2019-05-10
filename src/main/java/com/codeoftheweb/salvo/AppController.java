package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

//Controla repositorio, crea arquitectura
@RestController
public class AppController {

    //Le dice al framework que en un repositorio y lo trate como tal y lo linkea a la clase que lo llama
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GamePlayerRepository gamepRepository;

}
