package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Date date;

    @ManyToOne( fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @OneToMany(mappedBy="gameplayer", fetch= FetchType.EAGER)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy="gameplayer", fetch= FetchType.EAGER)
    private Set<Salvo> salvoes = new HashSet<>();

    public GamePlayer() { }

    public GamePlayer(Player player, Game game, Date date) {
        this.player = player;
        this.game = game;
        this.date = date;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public Set<Salvo> getSalvoes() {
        return salvoes;
    }

    /*
    public Score getScore(){
        return this.player.getScore(this.game);
    }
*/
    public void setSalvoes(Set<Salvo> salvoes) {
        this.salvoes = salvoes;
    }

    public void addShip(Ship ship) {
        ship.setGameplayer(this);
        ships.add(ship);
    }

    public void addSalvo(Salvo salvo) {
        salvo.setGameplayer(this);
        System.out.println(salvo.getGameplayer().getPlayer());
        salvoes.add(salvo);
    }

    @Override
    public String toString() {
        return "GamePlayer{" +
                "id=" + id +
                ", date=" + date +
                ", player=" + player +
                ", game=" + game +
                '}';
    }
}
