package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static java.util.stream.Collectors.toList;

//Un controlador en Spring es una clase con métodos para ejecutarse cuando se reciben solicitudes con patrones de URL específico
//facilita la definición de un servicio web que devuelve recursos JSON personalizados a un cliente en lugar de HTML
@RestController
@RequestMapping("/api")
public class SalvoController {

    //have one singleton instance that every class shares.
    //ells Spring to automatically create an instance of PersonRepository and store it in the instance variable personRepository.
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private SalvoRepository salvoRepository;
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @RequestMapping("/games")
    public List<Object> getGames2() {
        return gameRepository
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
        return  gameViewDTO(gamePlayerRepository.findById(id).get());
    }

    //players
    @RequestMapping("/leaderBoard")
    public List<Object> getScores() {

        return getPlayerList();
    }

    //Un objeto de transferencia de datos (DTO) es una estructura de Java creada solo para organizar los datos para transferirlos a otro sistema. Se crea según sea necesario, sin información irrelevante o privada, y sin referencias circulares.
    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate().getTime());
       dto.put("gamePlayers", getGamePlayerList(game.getGamePlayers())); //playerlit


        //dto.put("ships", gamePlayer.getShips());
        //dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        dto.put("score", getScoreList(game.getScores()));
        return dto;
    }

    private Map<String, Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("creationDate", gamePlayer.getDate().getTime());
        dto.put("gamePlayers", getGamePlayerList(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", gamePlayer.getShips());
        dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        return dto;
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        return dto;
    }
/*
    private Map<String, Object> makeLeaderBoardDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getEmail());
        dto.put("score", makeScoreDTO(player));
        return dto;
    }
*/
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
        dto.put("score", makeScoreDTO(player));
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

    private List<Map<String,Object>> getSalvoList(Game game){
        List<Map<String,Object>> myList = new ArrayList<>();
        //Array de json se crea una lista
        game.getGamePlayers().forEach(gamePlayer -> myList.addAll(MakeSalvoList(gamePlayer.getSalvoes())));
        return myList;
    }

    //1) Método -> Crear lista de distintos players
    private List<Object> getPlayerList(){

        return playerRepository
                .findAll()
                .stream()
                .map(player -> makePlayerDTO(player)).collect(toList());


    }

    //1) Método -> Crear lista de distintos players
    private List<Map<String,Object>> getScoreList(Set<Score> scores){
        return scores
                .stream()
                .map(score -> ScoreDTO(score)).collect(toList());

    }


    public Map<String, Object> ScoreDTO(Score score){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("name", score.getPlayer().getEmail());
        dto.put("score", score.getScore());
        //dto.put("player", makePlayerDTO(score.getPlayer()));
        dto.put("finishDate", score.getFinishDate());
       // dto.put("score", makeScoreDTO(score.getPlayer()));
        return dto;
    }


    public Map<String, Object> makeScoreDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("name", player.getEmail());
        dto.put("total", player.getScore());
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost", player.getLosses(player.getScores()));
        dto.put("tied", player.getTies(player.getScores()));
        return dto;
    }

}
