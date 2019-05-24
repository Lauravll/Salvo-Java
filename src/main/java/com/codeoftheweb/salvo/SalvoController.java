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
        dto.put("created", gamePlayer.getDate().getTime());
        dto.put("gameState","PLAY");
        dto.put("gamePlayers", getGamePlayerList(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", getShipList(gamePlayer.getShips()));
        dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        dto.put("hits", getHitsList(gamePlayer, gamePlayer.getPlayer()));
        //dto.put("scores", getScoreList(gamePlayer.getGame().getScores()));
        return dto;
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate().getTime());
        dto.put("gamePlayers", getGamePlayerList(game.getGamePlayers()));
        dto.put("scores", getScoreList(game.getScores())); //llama al score del game
        //dto.put("salvoes", getSalvoList(game));
        return dto;
    }

    private Map<String, Object> getHitsList( GamePlayer gamePlayer, Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        for(Player auxPlayer:gamePlayer.getGame().getPlayers()){
            if (auxPlayer.getId() != player.getId())
                for(GamePlayer gp: gamePlayer.getGame().getGamePlayers()){
                    if (gp != gamePlayer){
                        //gp = oponnent, gamePlayer = self
                        //Tiros a sus barcos
                        dto.put("opponent", getHits(gamePlayer.getSalvoes(), gp.getShips()));
                        //Tiros a mis barcos
                        dto.put("self",getHits( gp.getSalvoes(), gamePlayer.getShips()));
                    }
                }
        }
        return dto;
    }

    private List<Map> getHits(Set<Salvo> salvoesopponent, Set<Ship> shipsself) {

        List<Salvo> salvoOrden = salvoesopponent.stream()
                .sorted(Comparator.comparingInt(Salvo::getTurn))
                // .sorted(Comparator.comparing(Salvo::getTurn))
                .collect(toList());

        List<Map> hits = new ArrayList<>(); //lista de mapa

        //Contadores
        Integer carrierDamage = 0;
        Integer battleshipDamage = 0;
        Integer submarineDamage = 0;
        Integer destroyerDamage = 0;
        Integer patrolboatDamage = 0;

        List<String> carrierLocation = new ArrayList<>();
        List<String> battleshipLocation = new ArrayList<>();
        List<String> submarineLocation = new ArrayList<>();
        List<String> destroyerLocation = new ArrayList<>();
        List<String> patrolboatLocation = new ArrayList<>();

        //Listas de ubicaciones de mis Ships
        carrierLocation = (shipsself.stream().filter(ship -> "carrier".equals(ship.getType().toLowerCase())).findAny().orElse(null)).getLocations();
        battleshipLocation = (shipsself.stream().filter(ship -> "battleship".equals(ship.getType().toLowerCase())).findAny().orElse(null)).getLocations();
        submarineLocation = (shipsself.stream().filter(ship -> "submarine".equals(ship.getType().toLowerCase())).findAny().orElse(null)).getLocations();
        destroyerLocation = (shipsself.stream().filter(ship -> "destroyer".equals(ship.getType().toLowerCase())).findAny().orElse(null)).getLocations();
        patrolboatLocation = (shipsself.stream().filter(ship -> "patrolboat".equals(ship.getType().toLowerCase())).findAny().orElse(null)).getLocations();

                //Recorro la lista de tiros del oponente
        for (Salvo salvo : salvoOrden) {
            Map<String, Object> damages = new LinkedHashMap<>();
            Map<String, Object> dto = new LinkedHashMap<>(); //dentro del for asi no se pisa
            Integer missed = salvo.getLocations().size(); //Tomo las posiciones del tiro para saber cuantos perdi
            //Contadores aciertos del turno
            Integer aciertoCarrier = 0;
            Integer aciertoSubmarine = 0;
            Integer aciertoDestroyer = 0;
            Integer aciertoPatrolBoat = 0;
            Integer aciertoBattleShip = 0;
            List<String> celdasAcertadas = new ArrayList<>();

            //Comparo los tiros realizados en el turno individualmente contra las localizaciones de mis barcos

            for (String salvoIndividual: salvo.getLocations()){

              if(carrierLocation.contains(salvoIndividual)){
                   aciertoCarrier++;
                   carrierDamage++;
                   missed--;
                   celdasAcertadas.add(salvoIndividual);
               }
              if(submarineLocation.contains(salvoIndividual)){
                  aciertoSubmarine++;
                  submarineDamage++;
                  missed--;
                  celdasAcertadas.add(salvoIndividual);
              }
                if(destroyerLocation.contains(salvoIndividual)){
                    aciertoDestroyer++;
                    destroyerDamage++;
                    missed--;
                    celdasAcertadas.add(salvoIndividual);
                }
                if(patrolboatLocation.contains(salvoIndividual)){
                    aciertoPatrolBoat++;
                    patrolboatDamage++;
                    missed--;
                    celdasAcertadas.add(salvoIndividual);
                }
                if(battleshipLocation.contains(salvoIndividual)){
                    aciertoBattleShip++;
                    battleshipDamage++;
                    missed--;
                    celdasAcertadas.add(salvoIndividual);
                }
            }

            //Contador de golpes en el turno
            damages.put("carrierHits", aciertoCarrier);
            damages.put("battleshipHits", aciertoBattleShip);
            damages.put("submarineHits", aciertoSubmarine);
            damages.put("destroyerHits", aciertoDestroyer);
            damages.put("patrolboatHits", aciertoPatrolBoat);
            //Contador degolpes en el juego
            damages.put("carrier", carrierDamage);
            damages.put("battleship", battleshipDamage);
            damages.put("submarine", submarineDamage);
            damages.put("destroyer", destroyerDamage);
            damages.put("patrolboat", patrolboatDamage);
            //Informacion del turno
            dto.put("turn", salvo.getTurn());
            dto.put("hitLocations", celdasAcertadas);
            //Valor de los contadores en el turno
            dto.put("damages", damages);
            //No acertados en el turno
            dto.put("missed", missed);

            hits.add(dto);
        }
        return hits;
    }



    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        dto.put("joinDate", gamePlayer.getDate().getTime());
        //dto.put("ships", MakeShipList(gamePlayer.getShips()));
        //dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        return dto;
    }

    //1) Método -> Crear lista de distintos players
    private List<Object> getPlayerList(){
        return playerRepository
                .findAll()
                .stream()
                .map(player -> makePlayerDTO(player)).collect(toList());
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



    private List<Map<String, Object>> getShipList(Set<Ship> ships){
        return ships
                .stream()
                .map(ship -> makeShipDTO(ship))
                .collect(toList());
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGameplayer().getPlayer().getId());
        dto.put("locations", salvo.getLocations());
        return dto;
    }

    private Map<String, Object> makeSalvoDTO2(Salvo salvo, int i, String tipo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("hitLocations", salvo.getLocations());
        dto.put("missed", i);
        dto.put("damage", salvo.getLocations().size());
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

    private List<Map<String, Object>> MakeSalvoList2(Set<Salvo> salvoes, int i, String tipo){
        return salvoes
                .stream()
                .map(salvo -> makeSalvoDTO2(salvo, i, tipo))
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
        dto.put("type", ship.getType().toLowerCase());
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
