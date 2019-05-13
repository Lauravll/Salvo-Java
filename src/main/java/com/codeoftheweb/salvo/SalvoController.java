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
    @Autowired
    private SalvoRepository salvoRepository;

    @RequestMapping("/games")
    public List<Object> getGamesId() {
        return gamePlayerRepository
                .findAll()
                .stream()
                .map(game -> makeGameDTO(game)).collect(toList());
    }

    @RequestMapping("/game_view")
    public List<Object> getGames() {
        return gamePlayerRepository
                .findAll()
                .stream()
                .map(game -> makeGamePlayerDTO(game)).collect(toList());
    }

    @RequestMapping("/game_view/{id}")
    private Map<String, Object> getGames(@PathVariable Long id) {
        return makeGameDTO(gamePlayerRepository.findById(id).get());
    }

    private Map<String, Object> makeGameDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("creationDate", gamePlayer.getDate().getTime());
        dto.put("gamePlayers", getGamePlayerList(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", gamePlayer.getShips());
        dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        //dto.put("salvoes3", getSalvoList2(gamePlayer.getGame()));
        return dto;
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        return dto;
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getId());
        dto.put("player", salvo.getGameplayer().getPlayer().getId());
        dto.put("locations", salvo.getLocations());
        return dto;
    }

    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getEmail());
        return dto;
    }

    private List<Map<String, Object>> getGamePlayerList(Set<GamePlayer> gamePlayers){
        return gamePlayers
                .stream()
                .map(gamePlayer -> makeGamePlayerDTO(gamePlayer))
                .collect(toList());
    }

    private List<Map<String, Object>> MakeSalvoList(Set<Salvo> salvoes){
        return salvoes
                .stream()
                .map(salvo -> makeSalvoDTO(salvo))
                .collect(toList());
    }
/*
    private List<Map<String, Object>> getSalvoList2(Game game){
        Set<GamePlayer> games = game.getGamePlayers();
        Set<Salvo> s = new HashSet<Salvo>();
        for(GamePlayer g: games){
           for(Salvo sal: g.getSalvoes()){
               s.add(sal);
           }
        }
        return makeSalvoList(s);

    }
*/
    private List<Map<String,Object>> getSalvoList(Game game){
        List<Map<String,Object>> myList = new ArrayList<>();
        //Array de json se crea una lista
        game.getGamePlayers().forEach(gamePlayer -> myList.addAll(MakeSalvoList(gamePlayer.getSalvoes())));
        return myList;
    }


}
