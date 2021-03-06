package com.codeoftheweb.salvo;

import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

// Los handler (manejadores) indican a donde mandar la salida ya sea consola o archivo
// En este caso ConsoleHandler envia los logs a la consola


import static java.util.stream.Collectors.toList;

//Un controlador en Spring es una clase con métodos para ejecutarse cuando se reciben solicitudes con patrones de URL específico
//facilita la definición de un servicio web que devuelve recursos JSON personalizados a un cliente en lugar de HTML
@RestController
@RequestMapping("/api")
public class SalvoController {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

   // private static final Logger LOGGER = Logger.getLogger(SalvoController.class.getName());
  // private static Logger LOGGER = LoggerFactory.getLogger(SalvoController.class.getName());
    //private static Logger LOGGER = LogManager.getLogger(SalvoController.class.getName());
    ///private Logger LOGGER = LogManager.getLogger(SalvoController.class.getName());
    //have one singleton instance that every class shares.
    //tells Spring to automatically create an instance of PersonRepository and store it in the instance variable personRepository.
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
    @Autowired
    private ScoreRepository scoreRepository;

    //~~~~~~~~~~~~~~~~~~~~~~~~Autentificacion del jugador~~~~~~~~~~~~~~~~~~~~~~~~
    @RequestMapping("/games")
    public Map<String, Object> authenticationPlayer(Authentication authentication) throws IOException {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticationPlayer = getAuthentication(authentication);
        //LOGGER.log(Level.INFO, () -> "Player autenticado -> " + authenticationPlayer);

        /*LOGGER.setLevel(Level.INFO);
        LOGGER.severe("Se ha producido un error");
        FileHandler fileXml = new FileHandler("Logging.xml");
        LOGGER.addHandler(fileXml);*/
        //LOGGER.log(Level.INFO, () -> "Player autenticado -> " + authenticationPlayer);
       // LOGGER.info("Initializing class client with properties ---> {}");
        logger.info("Pruebo ");
        logger.trace("Pruebo ");

        if (authenticationPlayer == null)
            dto.put("player", "Invitado");
        else
            dto.put("player", makePlayerDTO(authenticationPlayer));
        dto.put("games" , getGames());
        return dto;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~Método extra~~~~~~~~~~~~~~~~~~~~~~~~
    private Player getAuthentication(Authentication authentication) {
        if(authentication == null || authentication instanceof AnonymousAuthenticationToken){
            return null;
        }
        else{
            return (playerRepository.findByEmail(authentication.getName()));
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~List y DTO de Player~~~~~~~~~~~~~~~~~~~~~~~~
    //Utilizado anteriormente por leaderboard Crear lista de distintos players
    private List<Object> getPlayerList(){
        return playerRepository
                .findAll()
                .stream()
                .sorted(Comparator.comparingLong(Player::getId))
                .map(player -> makePlayerDTO(player)).collect(toList());
    }

    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getEmail()); //username
        //dto.put("password", player.getPassword());
        // dto.put("score", makeScoreDTO(player));
        return dto;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~List y DTO de Games~~~~~~~~~~~~~~~~~~~~~~~~
    public List<Object> getGames() {
        return gameRepository
                .findAll()
                .stream()
                .map(game ->makeGameDTO(game)).collect(toList());
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

    //~~~~~~~~~~~~~~~~~~~~~~~~List y DTO de GamePlayer para /games~~~~~~~~~~~~~~~~~~~~~~~~
    private List<Map<String, Object>> getGamePlayerList(Set<GamePlayer> gamePlayers){
        return gamePlayers
                .stream()
                .sorted(Comparator.comparingLong(GamePlayer::getId))
                .map(gamePlayer -> makeGamePlayerDTO(gamePlayer))
                .collect(toList());
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer())); //utilizada arriba en la autentificacion
        dto.put("joinDate", gamePlayer.getDate().getTime());
        //dto.put("ships", MakeShipList(gamePlayer.getShips()));
        //dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        return dto;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~List y DTO de Score~~~~~~~~~~~~~~~~~~~~~~~~
    private List<Map<String,Object>> getScoreList(Set<Score> scores){
        return scores
                .stream()
                .map(score -> ScoreDTO(score)).collect(toList());
    }

    public Map<String, Object> ScoreDTO(Score score){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerID", score.getPlayer().getId());
        //dto.put("name", score.getPlayer().getEmail());
        dto.put("score", score.getScore());
        dto.put("finishedDate", score.getFinishDate().getTime());
        return dto;
    }

    //Dto de Score anteriormente utilizado por leaderboard
    public Map<String, Object> makeScoreDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("name", player.getEmail());
        dto.put("total", player.getScore());
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost", player.getLosses(player.getScores()));
        dto.put("tied", player.getTies(player.getScores()));
        return dto;
    }
    //Ademas de autentificar al jugador estos métodos traen la información del jugador si esta ok y luego trae todos los gamePlayers correspondientes
    //~~~~~~~~~~~~~~~~~~~~~~~~Fin de la autentidicacion de su jugador~~~~~~~~~~~~~~~~~~~~~~~~


    //~~~~~~~~~~~~~~~~~~~~~~~~Vista del juego luego de la autentificacion~~~~~~~~~~~~~~~~~~~~~~~~
    @RequestMapping("/game_view/{id}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable Long id, Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).get();
        Player player = gamePlayer.getPlayer();
        //System.out.println(player);
        Player authenticationPlayer = getAuthentication(authentication);
        //System.out.println(authenticationPlayer);
        if (authenticationPlayer == null)
            return new ResponseEntity<>(makeMap("error", "No hay un jugador logueado"), HttpStatus.FORBIDDEN);
        if (gamePlayer == null)
            return new ResponseEntity<>(makeMap("error", "No hay un juego creado"), HttpStatus.FORBIDDEN);
        if(authenticationPlayer.getId() == player.getId()){
            return new ResponseEntity<>(gameViewDTO(gamePlayerRepository.findById(id).get()), HttpStatus.ACCEPTED);}
        else{
            return new ResponseEntity<>(makeMap("error", "Usuario no autorizado"), HttpStatus.UNAUTHORIZED);
        }
    }

    private GamePlayer getOpponent(GamePlayer gamePlayer) {
        return gamePlayer.getGame().getGamePlayers().stream()
                .filter(gp -> !gp.equals(gamePlayer))
                .findFirst()
                .orElse(new GamePlayer());
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~DTO de GamePlayer para /game_view~~~~~~~~~~~~~~~~~~~~~~~~
    private Map<String, Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("created", gamePlayer.getDate().getTime());
        dto.put("gameState", getGameState(gamePlayer));
        dto.put("gamePlayers", getGamePlayerList(gamePlayer.getGame().getGamePlayers())); //utilizada en games
        if (gamePlayer.getShips() == null )
            dto.put("ships", new ArrayList());
        else
            dto.put("ships", getShipList(gamePlayer.getShips()));
        if (gamePlayer.getSalvoes() == null || getOpponent(gamePlayer).getSalvoes() == null )
            dto.put("salvoes", new ArrayList());
        else
            dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        dto.put("hits", getHitsList(gamePlayer, getOpponent(gamePlayer)));
        //dto.put("scores", getScoreList(gamePlayer.getGame().getScores()));
        return dto;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~List y DTO de Ship~~~~~~~~~~~~~~~~~~~~~~~~
    private List<Map<String, Object>> getShipList(Set<Ship> ships){
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

    //~~~~~~~~~~~~~~~~~~~~~~~~List y DTO de Salvoes~~~~~~~~~~~~~~~~~~~~~~~~
    //Lista de mapas, desde game busco todos gameplayers y desde los gameplayers los salvoes
    private List<Map<String,Object>> getSalvoList(Game game){
        List<Map<String,Object>> myList = new ArrayList<>();
        //Array de json se crea una lista
        game.getGamePlayers().forEach(gamePlayer -> myList.addAll(makeSalvoList(gamePlayer.getSalvoes())));
        return myList;
    }

    private List<Map<String, Object>> makeSalvoList(Set<Salvo> salvoes){
        return salvoes
                .stream()
                .sorted(Comparator.comparingInt(Salvo::getTurn))
                .map(salvo -> makeSalvoDTO(salvo))
                .collect(toList());
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGameplayer().getPlayer().getId());
        dto.put("locations", salvo.getLocations());
        return dto;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~List y DTO de Hits~~~~~~~~~~~~~~~~~~~~~~~~
    private  Map<String, Object> getHitsList(GamePlayer selfGP,GamePlayer opponentGP){
        Map<String, Object> dto = new LinkedHashMap<>();
        if (opponentGP == null){
            dto.put("self", new ArrayList<>());
            dto.put("opponent", new ArrayList<>());}
        else {
            dto.put("self", getHits(opponentGP.getSalvoes(), selfGP.getShips()));
            dto.put("opponent", getHits(selfGP.getSalvoes(), opponentGP.getShips())); }
        return dto;
    }

    private List <Map> getHits(Set <Salvo> salvoesopponent, Set <Ship> shipsself) {

        List <Salvo> salvoOrden = salvoesopponent.stream().sorted(Comparator.comparingInt(Salvo::getTurn)).collect(toList());
        // .sorted(Comparator.comparing(Salvo::getTurn))

        List <Map> hits = new ArrayList < > (); //lista de map
        Map <String, Integer> damageTotal = new HashMap <String, Integer> ();

        //Recorro la lista de tiros del oponente
        for (Salvo salvo: salvoOrden) {

            Map < String, Object > damageIndiv = new LinkedHashMap < > ();
            Map < String, Object > damages = new LinkedHashMap < > ();
            Map < String, Object > dto = new LinkedHashMap < > (); //dentro del for asi no se pisa

            //Del salvo obtengo las localizaciones y las convierto en stream el cual tiene la funcion de flatmap que convierte la lista en un stream de stream de strings las cuales contienen las localizaciones de los ships comparadas con las localizaciones del salvo
            //List<String> celdasAcertadas = salvo.getLocations().stream().flatMap(salvoLoc -> shipsself.stream().flatMap(ship -> ship.getLocations().stream().filter(shipLoc -> { return shipLoc.equals(salvoLoc); }))).collect(toList());
            List<String> celdasAcertadas = salvo.getLocations().stream().flatMap(salvoLocIndiv -> shipsself.stream().flatMap(ship -> ship.getLocations().stream().filter(shipLocIndiv -> shipLocIndiv.equals(salvoLocIndiv)))).collect(toList());

            //Contador de daños totales e individuales
            for (Ship ship: shipsself) {
                Integer contadorDaño = damageTotal.get(ship.getType().toLowerCase());
                Integer daño = (int) ship.getLocations().stream().filter(shipLoc -> celdasAcertadas.contains(shipLoc)).count();
                if (contadorDaño != null) {
                    damageTotal.put(ship.getType().toLowerCase(), contadorDaño + daño);
                } else {
                    damageTotal.put(ship.getType().toLowerCase(), daño);
                }
                damageIndiv.put(ship.getType().toLowerCase() + "Hits", daño);
            }

            int missed = salvo.getLocations().size() - celdasAcertadas.size();

            damages.put("carrierHits", damageIndiv.get("carrierHits"));
            damages.put("battleshipHits", damageIndiv.get("battleshipHits"));
            damages.put("submarineHits", damageIndiv.get("submarineHits"));
            damages.put("destroyerHits", damageIndiv.get("destroyerHits"));
            damages.put("patrolboatHits", damageIndiv.get("patrolboatHits"));

            damages.put("carrier", damageTotal.get("carrier"));
            damages.put("battleship", damageTotal.get("battleship"));
            damages.put("submarine", damageTotal.get("submarine"));
            damages.put("destroyer", damageTotal.get("destroyer"));
            damages.put("patrolboat", damageTotal.get("patrolboat"));
            //Informacion del turno
            dto.put("turn", salvo.getTurn());
            dto.put("hitLocations", celdasAcertadas);
            dto.put("damages", damages);
            dto.put("missed", missed);
            hits.add(dto);
        }
        return hits;
    }

    //Metodo extra
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~Fin game_view~~~~~~~~~~~~~~~~~~~~~~~~

    //~~~~~~~~~~~~~~~~~~~~~~~~RequestMapping para loguearse~~~~~~~~~~~~~~~~~~~~~~~~
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String username, @RequestParam String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Faltan datos"), HttpStatus.FORBIDDEN);
        }
        if (username.length()>25)
            return new ResponseEntity<>(makeMap("errorCaract", "Maximo de caracteres excedido"), HttpStatus.FORBIDDEN);

        if (password.length()>25)
            return new ResponseEntity<>(makeMap("errorc", "Maximo de caracteres excedido"), HttpStatus.FORBIDDEN);
        if (playerRepository.findByEmail(username) !=  null) {
            return new ResponseEntity<>(makeMap("error","El unombre ya se encuentra en uso."), HttpStatus.FORBIDDEN);
        }
        playerRepository.save(new Player(username, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~RequestMapping para crear el Juego~~~~~~~~~~~~~~~~~~~~~~~~
    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
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

    //~~~~~~~~~~~~~~~~~~~~~~~~RequestMapping para unirse a un Juego~~~~~~~~~~~~~~~~~~~~~~~~
    @RequestMapping(path="/game/{id}/players" , method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long id, Authentication actualUser) {
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

    //~~~~~~~~~~~~~~~~~~~~~~~~RequestMapping para conseguir los jugadores de un juego~~~~~~~~~~~~~~~~~~~~~~~~
    @RequestMapping(path = "/game/{Id}/players")
    private List<Map<String, Object>> getPlayersfromGame(@PathVariable Long Id){
        return (gameRepository
                .findById(Id)).get().getPlayers()
                .stream()
                .map(player -> makePlayerDTO(player))
                .collect(toList());
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~RequestMapping que muestra la información de todos los GamePlayers~~~~~~~~~~~~~~~~~~~~~~~~
    //No fue pedido
    @RequestMapping("/game_view")
    public List<Object> getAllGames() {
        return gamePlayerRepository
                .findAll()
                .stream()
                .map(game -> gameViewDTO(game)).collect(toList());
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~RequestMapping para mostrar el leaderBoard~~~~~~~~~~~~~~~~~~~~~~~~
    //Anteriormente usado
    @RequestMapping("/leaderBoard")
    public List<Object> getScores() {
        return getPlayerList();
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~RequestMapping para añadir ships~~~~~~~~~~~~~~~~~~~~~~~~
    @RequestMapping(value = "/games/players/{id}/ships", method = RequestMethod.POST)
    private ResponseEntity<Map<String,Object>> AddShips(@PathVariable long id, @RequestBody Set<Ship> ships,   Authentication authentication) {

        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);
        Player loggedPlayer = getAuthentication(authentication);
        //System.out.println(ships);
        if (loggedPlayer == null)
            return new ResponseEntity<>(makeMap("error", "No hay un usuario logueado"), HttpStatus.UNAUTHORIZED);
        if (gamePlayer == null)
            return new ResponseEntity<>(makeMap("error", "No hau un juego creado"), HttpStatus.UNAUTHORIZED);
        if (WrongGamePlayer(id, gamePlayer, loggedPlayer)) {
            return new ResponseEntity<>(makeMap("error", "GamePlayer equivocado"), HttpStatus.UNAUTHORIZED);
        } else {
            if (gamePlayer.getShips().isEmpty()) {
                for(Ship s: ships){
                    s.setGameplayer(gamePlayer);
                    gamePlayer.addShip(s);
                    shipRepository.save(s);
                }
                return new ResponseEntity<>(makeMap("OK", "Barcos guardados!"), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(makeMap("error", "El jugador ya tiene barcos"), HttpStatus.FORBIDDEN);
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~RequestMapping para añadir salvoes~~~~~~~~~~~~~~~~~~~~~~~~
    @RequestMapping(path = "/games/players/{id}/salvoes", method = RequestMethod.POST)
    private ResponseEntity<Map<String,Object>> AddSalvos(@PathVariable long id, @RequestBody Salvo salvo, Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);
        //System.out.println(gamePlayer.getSalvoes());
        //System.out.println(salvo);
        Player loggedPlayer = getAuthentication(authentication);
        if (loggedPlayer == null)
            return new ResponseEntity<>(makeMap("error", "No hay un jugador logueado"), HttpStatus.UNAUTHORIZED);
        if (gamePlayer == null)
            return new ResponseEntity<>(makeMap("error", "No hay un gameplayer"), HttpStatus.UNAUTHORIZED);
        if (WrongGamePlayer(id, gamePlayer, loggedPlayer)) {
            return new ResponseEntity<>(makeMap("error", "GamePlayer equivocado"), HttpStatus.UNAUTHORIZED);
        }
        else {
            if (salvoTurn(gamePlayer.getSalvoes(), salvo) == false) {
                salvo.setTurn(gamePlayer.getSalvoes().size() +1);
                salvo.setGameplayer(gamePlayer);
                gamePlayer.addSalvo(salvo);
                salvoRepository.save(salvo);
                //System.out.println(salvo);
                return new ResponseEntity<>(makeMap("ok", "Salvoes saved"), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(makeMap("error", "Player already has salvoes"), HttpStatus.FORBIDDEN);
            }
        }
    }

    //Metodo extra


    private boolean WrongGamePlayer(long id, GamePlayer gamePlayer, Player player){
        boolean corretGP= gamePlayer.getPlayer().getId() != player.getId();
        return corretGP;
    }

    //Metodo extra
    private boolean salvoTurn (Set<Salvo> salvos, Salvo salvo){
        boolean haveSalvo = false;
        for(Salvo s:salvos){
            if  (s.getTurn() == salvo.getTurn()){
                haveSalvo = true;
            }
        }
        /* boolean result = salvos.stream().anyMatch(salvoAux ->  salvoAux.getTurn() == salvo.getTurn()) ;*/
        return haveSalvo;
    }

    private String getGameState(GamePlayer selfGamePlayer){
        GamePlayer opponentGamePlayer = selfGamePlayer.getGame().getGamePlayers().stream().filter(gpo ->gpo.getId() != selfGamePlayer.getId()).findFirst().orElse(null);
        if (selfGamePlayer.getShips().size() == 0){
            return "UBICAR BARCOS";
        }
        if (opponentGamePlayer == null || opponentGamePlayer.getShips() == null){
            return "ESPERANDO OPONENTE";
        }
        int turn = getCurrentTurn(selfGamePlayer, opponentGamePlayer);
        if (opponentGamePlayer.getShips().size() == 0){
            return "ESPERE";
        }
        if(selfGamePlayer.getSalvoes().size() == opponentGamePlayer.getSalvoes().size()){
            Player selfPlayer = selfGamePlayer.getPlayer();
            Game game = selfGamePlayer.getGame();
            //Si ambos tienen todos los ships sunk
            if (totallyShipsSunk(selfGamePlayer.getShips(), opponentGamePlayer.getSalvoes()) && totallyShipsSunk(opponentGamePlayer.getShips(), selfGamePlayer.getSalvoes())){
                Score score = new Score(0.5f, new Date(), selfPlayer, game);
                if(!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "EMPATÓ";
            }
            //Si self tiene todos los ships sunk
            if (totallyShipsSunk(selfGamePlayer.getShips(),opponentGamePlayer.getSalvoes())){
                Score score = new Score(0, new Date(), selfPlayer, game);
                if(!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "PERDIÓ";
            }
            //si opponent tiene todos los ships sunk
            if(totallyShipsSunk(opponentGamePlayer.getShips(), selfGamePlayer.getSalvoes())){
                Score score = new Score(1, new Date(), selfPlayer, game);
                if(!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "GANÓ";
            }
        }
        if (selfGamePlayer.getSalvoes().size() != turn){
            return "JUEGUE";
        }
        return "ESPERE";
    }


    private boolean totallyShipsSunk(Set <Ship> shipsself, Set <Salvo> salvoesopponent) {
        boolean barcosHundidos = false;

        List <Salvo> salvoOrden = salvoesopponent.stream().sorted(Comparator.comparingInt(Salvo::getTurn)).collect(toList());

        Map <String, Integer> damageTotal = new HashMap <String, Integer> ();

        for (Salvo salvo: salvoOrden) {

            //Instancio una lista de strings. Primero obtengo las localizaciones del salvo de mi enemigo, luego de obtener la localizacion individual voy a realizar una comparacion y posterior filtralizacion con las localizaciones individuales de mis ships
            //Flatmap proyecta por cada elemento de entrada una lista de elementos de salida y los concatena en un stream(secuencia de objetos)
            List<String> celdasAcertadas = salvo.getLocations().stream().flatMap(salvoLocIndiv -> shipsself.stream().flatMap(ship -> ship.getLocations().stream().filter(shipLocIndiv -> shipLocIndiv.equals(salvoLocIndiv)))).collect(toList());

            for (Ship ship: shipsself) {
                Integer contadorDaño = damageTotal.get(ship.getType().toLowerCase());
                Integer daño = (int) ship.getLocations().stream().filter(shipLoc -> celdasAcertadas.contains(shipLoc)).count();
                if (contadorDaño != null) {
                    damageTotal.put(ship.getType().toLowerCase(), contadorDaño + daño);
                } else {
                    damageTotal.put(ship.getType().toLowerCase(), daño);
                }
            }
            //System.out.println("damage"+damageTotal);
            if (damageTotal.get("carrier") == 5 && damageTotal.get("battleship") == 4 && damageTotal.get("submarine") == 3 && damageTotal.get("destroyer") == 3 && damageTotal.get("patrolboat") == 2){
                barcosHundidos = true;
            }

        }
        return barcosHundidos;
    }

    private boolean existScore (Score score, Game game){
        boolean exist = false;
        Set<Score> scores = game.getScores();
        for (Score s: scores){
            if (score.getPlayer().getEmail().equals(s.getPlayer().getEmail())){
                exist = true;
            }
        }
        return exist;
    }

    private int getCurrentTurn(GamePlayer selfGamePlayer, GamePlayer opponentGamePlayer){
        int selfSalvoes = selfGamePlayer.getSalvoes().size();
        int opponentSalvoes = opponentGamePlayer.getSalvoes().size();

        int total = selfSalvoes + opponentSalvoes;
        //Si los turnos son pares
        if (total % 2 == 0){
            return total/2 +1;
        }
        //si son impares
        return (int) (total/2.0 + 0.5);
    }

}