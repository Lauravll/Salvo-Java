package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;
    private GamePlayerRepository gameplayerRepository;

    @RequestMapping("/games")
    public List<Object> getGamesId() {
        return gameRepository.findAll().stream().map(game -> makeGameDTO(game)).collect(toList());
    }

    @RequestMapping("/game_view")
    public List<Object> getGames() {
        return gameRepository
                .findAll()
                .stream()
                .map(game -> makeGameDTO(game)).collect(toList());
    }

    //Obtiene el objeto json data transfer con los barcos de un juego en particular, necesita saber cual es el game player en particular
    @RequestMapping("/game_view/{id}")
    private Map<String, Object> getGames(@PathVariable Long id) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        Game g = gameRepository.getOne(id) ;
        dto.put("id", g.getId());
        dto.put("gamePlayers", g.getGamePlayers().
                stream()
                .map(gamePlayer -> makeGamePlayerDTO(gamePlayer))
                .collect(toList()));
        return dto;
    }

    @RequestMapping("/game_view2/{id}")
    private Map<String, Object> getGames2(@PathVariable Long id) {
        System.out.println(gameplayerRepository.findById(id).get());
        return  makeGamePlayerDTO(gameplayerRepository.findById(id).get());
    }


    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers().
                stream()
                .map(gamePlayer -> makeGamePlayerDTO(gamePlayer))
                .collect(toList()));
        return dto;
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamep) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamep.getId());
        dto.put("player", makePlayerDTO(gamep.getPlayer()));
        return dto;
    }

    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getEmail());
        return dto;
    }

}
