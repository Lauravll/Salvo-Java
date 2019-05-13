package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	//Marca tareas prioritarias para el springboot, va invocando secuencialmetne clases . Por eso las entity los carga primero y los trata como tabla, y los autowired que asume que estan relacionados con otros objetos
	//Antes de que arranque la ejecucion
	//turn an instance of a bean (clase normal). Spring save the instance for autowired injection.
	@Bean
	//tells Spring to run initData() and saves the value returned. initData() creates and returns a lambda expression
	//he return type of initData() is CommandLineRunner. CommandLineRunner is a functional interface, i.e., an interface with just one method, run()
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository){

		return (args) -> {

			Player player1 = new Player("j.bauer@ctu.gov", "p1");
			Player player2 = new Player ("c.obrian@ctu.gov", "p2");
			Player player3 = new Player ("c.oconan@ctu.gov", "p3");
			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);

			Date date = new Date();
			Date newDate = Date.from(date.toInstant().plusSeconds(3600 ));
			Date newDate2 = Date.from(date.toInstant().plusSeconds(7200));

			Game game = new Game(date);
			Game game2 = new Game(newDate);
			Game game3 = new Game(newDate2);

			gameRepository.save(game);
			gameRepository.save(game2);
			gameRepository.save(game3);

			GamePlayer gamePlayer = new GamePlayer(player1, game, date);
			GamePlayer gamePlayer2 = new GamePlayer(player2, game, date);
			GamePlayer gamePlayer3 = new GamePlayer(player3, game2, date);
			GamePlayer gamePlayer4 = new GamePlayer(player2, game2, newDate);

			gamePlayerRepository.save(gamePlayer);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);

			List<String> shipLocations1 = new ArrayList<>();
			shipLocations1.add("H4");
			shipLocations1.add("H3");

			List<String> shipLocations2 = new ArrayList<>();
			shipLocations2.add("A2");
			shipLocations2.add("B2");
			shipLocations2.add("C2");
			shipLocations2.add("D2");

			List<String> shipLocations3 = new ArrayList<>();
			shipLocations3.add("A1");
			shipLocations3.add("B1");

			List<String> shipLocations4 = new ArrayList<>();
			shipLocations4.add("H");
			shipLocations4.add("I1");
			shipLocations4.add("J1");


			Ship ship = new Ship("Patrol Boat", gamePlayer2, shipLocations1);
			Ship ship2 = new Ship("Submarine", gamePlayer, shipLocations2);
			Ship ship3 = new Ship("Destroyer", gamePlayer2, shipLocations3);
			Ship ship4 = new Ship("Carrier", gamePlayer, shipLocations4);


			shipRepository.save(ship);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);

			//agrego barco
			gamePlayer.addShip(ship3);
			gamePlayer2.addShip(ship2);
			gamePlayer3.addShip(ship);
			gamePlayer4.addShip(ship4);


			List<String> salvoLocations = new ArrayList<>();
			salvoLocations.add("H");

			List<String> salvoLocations2 = Arrays.asList("A2", "A3");

			List<String> salvoLocations3 = new ArrayList<>();
			salvoLocations3.add("A1");

			//Juego 1
			Salvo salvo = new Salvo(1, gamePlayer2, salvoLocations );
			Salvo salvo2 = new Salvo(2, gamePlayer, salvoLocations2 );
			Salvo salvo3 = new Salvo(2, gamePlayer, salvoLocations3 );

			//Juego 2
			Salvo salvo4 = new Salvo(2, gamePlayer3, salvoLocations2 );

			salvoRepository.save(salvo);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);

			Date dates = new Date();
			float f = (float) 0.5;
			Score score = new Score(f, dates, player1, game );
			Score score2 = new Score((float)30, dates, player2, game);
			Score score3 = new Score(12, dates, player1, game);

			scoreRepository.save(score);
			scoreRepository.save(score2);
			scoreRepository.save(score3);

			player1.addScore(score);
			player2.addScore(score2);
			player1.addScore(score3);




		};

	}

}
