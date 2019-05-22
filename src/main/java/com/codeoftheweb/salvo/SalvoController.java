package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    private PlayerRepository playerRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;
/*
    @RequestMapping("/games")
    public List<Object> getGames2() {
        return gamePlayerRepository
                .findAll()
                .stream()
                .map(game -> gameViewDTO(game)).collect(toList());
    }
*/
    @RequestMapping("/games")
    public Map<String, Object> getGames2(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticationPlayer = getAuthentication(authentication);
        if (authenticationPlayer == null)
            dto.put("player", "Guest");
        else
           dto.put("player", makePlayerDTO(authenticationPlayer));
        dto.put("games" , getGames());
        return dto;
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createJuego(Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);
        if(authenticatedPlayer == null){
            return new ResponseEntity<>(makeMap("error","No estas autorizado"), HttpStatus.FORBIDDEN);
        } else {
            Date date = Date.from(java.time.ZonedDateTime.now().toInstant());
            Game auxGame = new Game(date);
            gameRepository.save(auxGame);

            GamePlayer auxGameP = new GamePlayer(authenticatedPlayer,auxGame,date);
            gamePlayerRepository.save(auxGameP);
            return new ResponseEntity<>(makeMap("gpid", auxGameP.getId()), HttpStatus.CREATED);
        }
    }


    @RequestMapping("/game_view")
    public List<Object> getGameView() {
        return gamePlayerRepository
                .findAll()
                .stream()
                .map(game -> makeGamePlayerDTO(game)).collect(toList());
    }

    public List<Object> getGamesA() {
        return gamePlayerRepository
                .findAll()
                .stream()
                .map(game -> gameViewDTO(game)).collect(toList());
    }

    public List<Object> getGames() {
        return gameRepository
                .findAll()
                .stream()
                .map(game ->makeGameDTO(game)).collect(toList());
    }

    @RequestMapping("/game_view/{id}")
    public ResponseEntity<Map<String, Object>> getGames(@PathVariable Long id, Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).get();
        Player player = gamePlayer.getPlayer();
        System.out.println(player);
        //System.out.println();
        Player authenticationPlayer = getAuthentication(authentication);
        //System.out.println(authenticationPlayer);
        if(authenticationPlayer == player){

            return new ResponseEntity<>(gameViewDTO(gamePlayerRepository.findById(id).get()), HttpStatus.ACCEPTED);}
        else{
            return new ResponseEntity<>(makeMap("error", "Usuario no autorizado"), HttpStatus.UNAUTHORIZED);
        }

    }

    @RequestMapping(path="/game/{id}/players" , method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getGamesWhithPlayers(@PathVariable Long id, Authentication actualUser) {
        //System.out.println(id);
        actualUser = SecurityContextHolder.getContext().getAuthentication();
        Player authenticationPlayer = getAuthentication(actualUser);
        Game game = gameRepository.findById(id).get();
        //System.out.println("id juego"+game.getId());
        if(authenticationPlayer == null){
            return new ResponseEntity<>(makeMap("error", "Usuario no autorizado"), HttpStatus.UNAUTHORIZED);
        }
        else{
            //System.out.println("Usuario autorizado");
            if (game == null){
                return new ResponseEntity<>(makeMap("error", "Juego no encontrado"), HttpStatus.FORBIDDEN);
            }
            else{
                //System.out.println("GamePlayer existe");
                List<Player> list = game.getPlayers();
                if (list.size() >= 2){
                    return new ResponseEntity<>(makeMap("error", "El juego ya tiene dos jugadores"), HttpStatus.FORBIDDEN);
                }
                else{
                    //System.out.println("Hay espacio disponible");
                    GamePlayer nuevog1 = new GamePlayer(authenticationPlayer, game, new Date());
                    gamePlayerRepository.save(nuevog1);
                    return new ResponseEntity<>(makeMap("gpid", nuevog1.getId()), HttpStatus.CREATED);
                    }
            }
        }
    }

    @RequestMapping(value = "/games/players/{id}/ships", method = RequestMethod.POST)
    private ResponseEntity<Map<String,Object>> AddShips(@PathVariable long id,
                                                        @RequestBody Set<Ship> ships,
                                                        Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);
        Player loggedPlayer = getAuthentication(authentication);
        if (loggedPlayer == null)
            return new ResponseEntity<>(makeMap("error", "no player logged in"), HttpStatus.UNAUTHORIZED);
        if (gamePlayer == null)
            return new ResponseEntity<>(makeMap("error", "no such gamePlayer"), HttpStatus.UNAUTHORIZED);
        if (WrongGamePlayer(id, gamePlayer, loggedPlayer)) {
            return new ResponseEntity<>(makeMap("error", "Wrong GamePlayer"), HttpStatus.UNAUTHORIZED);
        } else {
            if (gamePlayer.getSalvoes().isEmpty()) {
                ships.forEach(ship -> ship.setGameplayer(gamePlayer));
                gamePlayer.setShips(ships);
                shipRepository.saveAll(ships);
                return new ResponseEntity<>(makeMap("ok", "Ships saved"), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(makeMap("error", "Player already has ships"), HttpStatus.FORBIDDEN);
            }
        }
    }

    @RequestMapping(path = "/games/players/{id}/salvoes", method = RequestMethod.POST)
    private ResponseEntity<Map<String,Object>> AddSalvos(@PathVariable long id,
                                                         @RequestBody Salvo salvo,
                                                        Authentication authentication) {

        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);
        System.out.println(gamePlayer.getSalvoes());
        Player loggedPlayer = getAuthentication(authentication);

        if (loggedPlayer == null)
            return new ResponseEntity<>(makeMap("error", "no player logged in"), HttpStatus.UNAUTHORIZED);
        if (gamePlayer == null)
            return new ResponseEntity<>(makeMap("error", "no such gamePlayer"), HttpStatus.UNAUTHORIZED);

        if (WrongGamePlayer(id, gamePlayer, loggedPlayer)) {
            return new ResponseEntity<>(makeMap("error", "Wrong GamePlayer"), HttpStatus.UNAUTHORIZED);
        }
        else {
            if (salvoTurn(gamePlayer.getSalvoes(), salvo) == false) {
                gamePlayer.addSalvo(salvo);
                salvoRepository.save(salvo);
                return new ResponseEntity<>(makeMap("ok", "Salvoes saved"), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(makeMap("error", "Player already has salvoes"), HttpStatus.FORBIDDEN);
            }
        }



    }

    private boolean salvoTurn (Set<Salvo> salvos, Salvo salvo){
        boolean haveSalvo = false;
        for(Salvo s:salvos){
            if  (s.getTurn() == salvo.getTurn()){
                haveSalvo = true;

            }
        }
        /*
        boolean result = salvos.stream().anyMatch(salvoAux ->  salvoAux.getTurn() == salvo.getTurn()) ;
        */
       return haveSalvo;
    }

    private boolean WrongGamePlayer(long id, GamePlayer gamePlayer, Player player){

        boolean corretGP= gamePlayer.getPlayer().getId() != player.getId();
        return corretGP;
    }



    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    //players
    @RequestMapping("/leaderBoard")
    public List<Object> getScores() {
        return getPlayerList();
    }

    ///game_view/{id}
    private Map<String, Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("creationDate", gamePlayer.getDate().getTime());
        dto.put("scores", getScoreList(gamePlayer.getGame().getScores()));
        dto.put("gamePlayers", getGamePlayerList(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", gamePlayer.getShips());
        dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        return dto;
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate().getTime());
        dto.put("scores", getScoreList(game.getScores())); //llama al score del game
        dto.put("gamePlayers", getGamePlayerList(game.getGamePlayers()));
        //dto.put("salvoes", getSalvoList(game));
        return dto;
    }


    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        dto.put("ships", MakeShipList(gamePlayer.getShips()));
        dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        return dto;
    }

    //1) Método -> Crear lista de distintos players
    private List<Object> getPlayerList(){
        return playerRepository
                .findAll()
                .stream()
                .map(player -> makePlayerDTO(player)).collect(toList());
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGameplayer().getPlayer().getId());
        dto.put("locations", salvo.getLocations());
        return dto;
    }

    //Utilizado por /games
    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getEmail());
        //dto.put("password", player.getPassword());
       // dto.put("score", makeScoreDTO(player));
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

    private List<Map<String,Object>> getScoreList(Set<Score> scores){
        return scores
                .stream()
                .map(score -> ScoreDTO(score)).collect(toList());
    }

    public Map<String, Object> ScoreDTO(Score score){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerID", score.getPlayer().getId());
        dto.put("name", score.getPlayer().getEmail());
        dto.put("score", score.getScore());
        dto.put("finishedDate", score.getFinishDate());
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

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String username, @RequestParam String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }
        if (playerRepository.findByEmail(username) !=  null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }
        playerRepository.save(new Player(username, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
   }

    private Player getAuthentication(Authentication authentication) {
        if(authentication == null || authentication instanceof AnonymousAuthenticationToken){
            return null;
        }
        else{
            return (playerRepository.findByEmail(authentication.getName()));
        }
    }

    private List<Map<String, Object>> MakeShipList(Set<Ship> ships){
        return ships
                .stream()
                .map(ship -> makeShipDTO(ship))
                .collect(toList());
    }

    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    @RequestMapping("/")
    public List<Object> getPlayers() {
        return playerRepository
                .findAll()
                .stream()
                .map(player -> makePlayerDTO(player))
                .collect(toList());
    }

}
