package com.keemerz.klaverjas.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.keemerz.klaverjas.domain.Bid.*;
import static com.keemerz.klaverjas.domain.Seat.*;
import static com.keemerz.klaverjas.domain.Suit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class BiddingTest {

    private Bidding baseBidding;

    @BeforeEach
    void setUp() {
        baseBidding = new Bidding(HEARTS,
                Map.of(
                        NORTH, PASS,
                        EAST, PASS,
                        SOUTH, PLAY));
    }

    @Test
    public void rotateForSouth() {
        // no real rotation since current player was already SOUTH
        Bidding rotatedBidding = baseBidding.rotateForSeat(SOUTH);

        assertThat(rotatedBidding.getProposedTrump(), is(HEARTS));
        assertThat(rotatedBidding.getBids().get(NORTH), is(baseBidding.getBids().get(NORTH)));
        assertThat(rotatedBidding.getBids().get(EAST), is(baseBidding.getBids().get(EAST)));
        assertThat(rotatedBidding.getBids().get(SOUTH), is(baseBidding.getBids().get(SOUTH)));
        assertThat(rotatedBidding.getBids().get(WEST), is(baseBidding.getBids().get(WEST)));
    }

    @Test
    public void rotateForNorth() {
        Bidding rotatedBidding = baseBidding.rotateForSeat(NORTH);

        assertThat(rotatedBidding.getProposedTrump(), is(HEARTS));
        assertThat(rotatedBidding.getBids().get(NORTH), is(baseBidding.getBids().get(SOUTH)));
        assertThat(rotatedBidding.getBids().get(EAST), is(baseBidding.getBids().get(WEST)));
        assertThat(rotatedBidding.getBids().get(SOUTH), is(baseBidding.getBids().get(NORTH)));
        assertThat(rotatedBidding.getBids().get(WEST), is(baseBidding.getBids().get(EAST)));
    }

    @Test
    public void rotateForEast() {
        Bidding rotatedBidding = baseBidding.rotateForSeat(EAST);

        assertThat(rotatedBidding.getProposedTrump(), is(HEARTS));
        assertThat(rotatedBidding.getBids().get(NORTH), is(baseBidding.getBids().get(WEST)));
        assertThat(rotatedBidding.getBids().get(EAST), is(baseBidding.getBids().get(NORTH)));
        assertThat(rotatedBidding.getBids().get(SOUTH), is(baseBidding.getBids().get(EAST)));
        assertThat(rotatedBidding.getBids().get(WEST), is(baseBidding.getBids().get(SOUTH)));
    }

    @Test
    public void rotateForWest() {
        Bidding rotatedBidding = baseBidding.rotateForSeat(WEST);

        assertThat(rotatedBidding.getBids().get(NORTH), is(baseBidding.getBids().get(EAST)));
        assertThat(rotatedBidding.getBids().get(EAST), is(baseBidding.getBids().get(SOUTH)));
        assertThat(rotatedBidding.getBids().get(SOUTH), is(baseBidding.getBids().get(WEST)));
        assertThat(rotatedBidding.getBids().get(WEST), is(baseBidding.getBids().get(NORTH)));
    }

}