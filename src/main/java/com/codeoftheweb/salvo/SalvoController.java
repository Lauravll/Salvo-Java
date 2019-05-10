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
    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @RequestMapping("/games")
    public List<Object> getGamesId() {
        return gameRepository.findAll().stream().map(game -> makeGameDTO(game)).collect(toList());
    }

    @RequestMapping("/game_view")
    public List<Object> getGames() {
        return gamePlayerRepository
                .findAll()
                .stream()
                .map(game -> makeGamePlayerDTO(game)).collect(toList());
    }

    /*
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
    */

    @RequestMapping("/game_view/{id}")
    private Map<String, Object> getGames(@PathVariable Long id) {

        return makeGamePlayerDTO(gamePlayerRepository.findById(id).get());
    }


    @RequestMapping("/game_view2/{id}")
    private Map<String, Object> getGames2(@PathVariable Long id) {
        System.out.println(gamePlayerRepository.findById(id).get());
        return  makeGamePlayerDTO(gamePlayerRepository.findById(id).get());
    }


    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers().
                stream()
                .map(gamePlayer -> makeGamePlayerDTO(gamePlayer))
                .collect(toList()));
        dto.put("ships", game.getGamePlayers().
                stream()
                .map(gamePlayer -> makeShipDTO(gamePlayer.getShips()))
                .collect(toList()));
        return dto;
    }

    private Map<String, Object> makeShipDTO(Set<Ship> ships) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        for(Ship s: ships){
            dto.put("type", s.getType());
            dto.put("locations", s.getShipLocations());
        }
        return dto;
    }

    private Map<String, Object> makeShipDTO2(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getShipLocations());
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
