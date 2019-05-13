package com.codeoftheweb.salvo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    private int turn;

    @ManyToOne( fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gameplayer;

    @ElementCollection
    @CollectionTable(name="salvoLocation")
    @Column(name="shipLocation_id")
    private List<String> locations = new ArrayList<>();

    public Salvo() {
    }

    public Salvo(int turn, GamePlayer gameplayer, List<String> locations) {
        this.turn = turn;
        this.gameplayer = gameplayer;
        this.locations = locations;
    }

    public Long getId() {
        return id;
    }

    public int getTurn() {
        return turn;
    }

    @JsonIgnore
    public GamePlayer getGameplayer() {
        return gameplayer;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public void setGameplayer(GamePlayer gameplayer) {
        this.gameplayer = gameplayer;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
}
