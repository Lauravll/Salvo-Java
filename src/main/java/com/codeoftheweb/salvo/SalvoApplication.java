package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	//Marca tareas prioritarias para el springboot, va invocando secuencialmetne clases . Por eso las entity los carga primero y los trata como tabla, y los autowired que asume que estan relacionados con otros objetos
	//Antes de que arranque la ejecucion
	@Bean
	public CommandLineRunner initData(PlayerRepository repository, GameRepository repository2, GamePlayerRepository repository3, ShipRepository repository4) {

		return (args) -> {

			Player p1 = new Player("j.bauer@ctu.gov", "p1");
			Player p2 = new Player ("c.obrian@ctu.gov", "p2");
			Player p3 = new Player ("c.oconan@ctu.gov", "p3");
			repository.save(p1);
			repository.save(p2);
			repository.save(p3);

			Date date = new Date();
			Date newDate = Date.from(date.toInstant().plusSeconds(3600 ));
			Date newDate2 = Date.from(date.toInstant().plusSeconds(7200));

			Game g = new Game(date);
			Game g2 = new Game(newDate);
			Game g3 = new Game(newDate2);

			repository2.save(g);
			repository2.save(g2);
			repository2.save(g3);

			GamePlayer gp = new GamePlayer(p1, g, date);
			GamePlayer gp2 = new GamePlayer(p2, g, date);
			GamePlayer gp4 = new GamePlayer(p3, g, date);
			GamePlayer gp3 = new GamePlayer(p2, g2, newDate);

			repository3.save(gp);
			repository3.save(gp2);
			repository3.save(gp3);
			repository3.save(gp4);

			List<String> shipLocations1 = new ArrayList<>();
			shipLocations1.add("A1");
			shipLocations1.add("H1");
			shipLocations1.add("H2");
			shipLocations1.add("H3");


			Ship s = new Ship("Patrol Boat", gp, shipLocations1);
			Ship s2 = new Ship("Submarine", gp2, shipLocations1);
			Ship s3 = new Ship("Destroyer", gp3, shipLocations1);
			Ship s4 = new Ship("Destroyer", gp3, shipLocations1);
			Ship s5 = new Ship("Destroyer", gp3, shipLocations1);

			repository4.save(s);
			repository4.save(s2);
			repository4.save(s3);
			repository4.save(s4);

			gp.addShip(s5);


		};

	}

}
