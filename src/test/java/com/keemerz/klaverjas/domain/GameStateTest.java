package com.keemerz.klaverjas.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.keemerz.klaverjas.domain.Rank.*;
import static com.keemerz.klaverjas.domain.Seat.*;
import static com.keemerz.klaverjas.domain.Suit.*;
import static com.keemerz.klaverjas.domain.Team.NS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    public void shuffleShouldDeal8CardsToEachSeat() {
        GameState gameState = GameState.createNewGame();
        gameState.dealHands();

        for (Seat seat : Seat.values()) {
            assertThat(gameState.getHands().get(seat).size(), is(8));
        }
    }

    @Test
    public void fillSeatShouldStartNorth() {
        GameState gameState = GameState.createNewGame();
        String testId = "testPlayerId1";

        gameState.fillSeat(new TestPlayerBuilder().withPlayerId(testId).build());

        assertThat(gameState.determinePlayerIds().size(), is(1));
        assertThat(gameState.determinePlayerIds().get(0), is(testId));

        assertThat(gameState.getPlayers().size(), is(1));
        assertThat(gameState.getPlayers().get(NORTH).getPlayerId(), is(testId));
    }

    @Test
    public void fillSeatShouldContinueClockwise() {
        GameState gameState = GameState.createNewGame();
        String testId1 = "testPlayerId1";
        String testId2 = "testPlayerId2";

        gameState.fillSeat(new TestPlayerBuilder().withPlayerId(testId1).build()); // North
        gameState.fillSeat(new TestPlayerBuilder().withPlayerId(testId2).build()); // East

        assertThat(gameState.determinePlayerIds().size(), is(2));
        assertTrue(gameState.determinePlayerIds().contains(testId1));
        assertTrue(gameState.determinePlayerIds().contains(testId2));

        assertThat(gameState.getPlayers().size(), is(2));
        assertThat(gameState.getPlayers().get(NORTH).getPlayerId(), is(testId1));
        assertThat(gameState.getPlayers().get(EAST).getPlayerId(), is(testId2));
    }

    @Test
    public void fillSeatShouldNoOpWhenAllSeatsArePopulated() {
        GameState gameState = GameState.createNewGame();
        gameState.fillSeat(new TestPlayerBuilder().build());
        gameState.fillSeat(new TestPlayerBuilder().build());
        gameState.fillSeat(new TestPlayerBuilder().build());
        gameState.fillSeat(new TestPlayerBuilder().build());
        assertThat(gameState.getPlayers().size(), is(4));

        // adding a 5th player
        String fifthPlayerId = "fifthPlayerId";
        gameState.fillSeat(new TestPlayerBuilder().withPlayerId(fifthPlayerId).build());

        // 5th player should not be in the list of players
        assertTrue(gameState.getPlayers().values().stream()
                .noneMatch(player -> player.getPlayerId().equals(fifthPlayerId)));
    }

    @Test
    public void lastCardInTrickShouldStartNextTrickWithSouthToPlay() {
        GameState gameState = new TestGameStateBuilder()
                .withGameId("someGameId")
                .withDealer(NORTH)
                .withBidding(new TestBiddingBuilder()
                        .withFinalTrump(HEARTS)
                        .withFinalBidBy(EAST)
                        .build())
                .withHand(WEST, new ArrayList<>())
                .withHand(NORTH, new ArrayList<>())
                .withHand(EAST, new ArrayList<>())
                .withHand(SOUTH, Arrays.asList(
                        Card.of(HEARTS, KING),
                        Card.of(SPADES, SEVEN),
                        Card.of(SPADES, EIGHT),
                        Card.of(CLUBS, SEVEN),
                        Card.of(CLUBS, EIGHT),
                        Card.of(DIAMONDS, SEVEN),
                        Card.of(DIAMONDS, EIGHT)))
                .withCurrentTrick(new TestTrickBuilder()
                        .withTrump(HEARTS)
                        .withCardPlayed(WEST, Card.of(HEARTS, SEVEN))
                        .withCardPlayed(NORTH, Card.of(HEARTS, EIGHT))
                        .withCardPlayed(EAST, Card.of(HEARTS, QUEEN))
                        .withStartingPlayer(EAST)
                        .build())
                .build();

        gameState.processCard(SOUTH, Card.of(HEARTS, KING));

        // score should be scored, game should be fresh
        assertThat(gameState.getGameScores().size(), is(0));
        assertThat(gameState.getDealer(), is(NORTH));
        assertThat(gameState.getTurn(), is(SOUTH));
        assertThat(gameState.getCurrentTrick().getTrickWinner(), is(SOUTH));

        gameState.processCard(SOUTH, Card.of(SPADES, SEVEN));

        assertThat(gameState.getTurn(), is(WEST));
        assertThat(gameState.getCurrentTrick().getStartingPlayer(), is(SOUTH));
        assertThat(gameState.getCurrentTrick().getCardsPlayed().get(SOUTH), is(Card.of(SPADES, SEVEN)));
        assertNull(gameState.getCurrentTrick().getTrickWinner());
    }

    @Test
    public void lastCardInLastTrickShouldStartFreshGame() {
       GameState gameState = new TestGameStateBuilder()
               .withGameId("someGameId")
               .withDealer(NORTH)
               .withBidding(new TestBiddingBuilder()
                    .withFinalTrump(HEARTS)
                    .withFinalBidBy(EAST)
                    .build())
               .withPreviousTricks(buildSevenPreviousTricks())
               .withHand(WEST, new ArrayList<>())
               .withHand(NORTH, new ArrayList<>())
               .withHand(EAST, new ArrayList<>())
               .withHand(SOUTH, Arrays.asList(Card.of(HEARTS, KING)))
               .withCurrentTrick(new TestTrickBuilder()
                       .withTrump(HEARTS)
                       .withCardPlayed(WEST, Card.of(HEARTS, SEVEN))
                       .withCardPlayed(NORTH, Card.of(HEARTS, EIGHT))
                       .withCardPlayed(EAST, Card.of(HEARTS, QUEEN))
                       .withStartingPlayer(EAST)
                       .build())
               .build();

       gameState.processCard(SOUTH, Card.of(HEARTS, KING));

       // score should be scored, game should be fresh
       assertThat(gameState.getGameScores().size(), is(1));
       assertThat(gameState.getGameScores().get(0).getScores().get(NS), is(7));
       assertThat(gameState.getDealer(), is(EAST));
       assertThat(gameState.getTurn(), is(EAST));
       assertNull(gameState.getBidding());
       assertNull(gameState.getCurrentTrick());
       assertTrue(gameState.getHands().values().stream().allMatch(Objects::isNull));
       assertThat(gameState.getGameId(), is("someGameId"));
    }

    private List<Trick> buildSevenPreviousTricks() {
        List<Trick> previousTricks = new ArrayList<>();

        // non trump cards all go to East
        for (Suit suit : Arrays.asList(CLUBS, DIAMONDS, SPADES)) {
            previousTricks.add(new TestTrickBuilder()
                    .withStartingPlayer(WEST)
                    .withTrump(HEARTS)
                    .withCardPlayed(WEST, Card.of(suit, ACE))
                    .withCardPlayed(NORTH, Card.of(suit, QUEEN))
                    .withCardPlayed(EAST, Card.of(suit, KING))
                    .withCardPlayed(SOUTH, Card.of(suit, TEN))
                    .withTrickWinner(WEST)
                    .build());
            previousTricks.add(new TestTrickBuilder()
                    .withStartingPlayer(WEST)
                    .withTrump(HEARTS)
                    .withCardPlayed(WEST, Card.of(suit, JACK))
                    .withCardPlayed(NORTH, Card.of(suit, SEVEN))
                    .withCardPlayed(EAST, Card.of(suit, EIGHT))
                    .withCardPlayed(SOUTH, Card.of(suit, NINE))
                    .withTrickWinner(WEST)
                    .build());
        }

        previousTricks.add(new TestTrickBuilder()
                .withStartingPlayer(WEST)
                .withTrump(HEARTS)
                .withCardPlayed(WEST, Card.of(HEARTS, JACK))
                .withCardPlayed(NORTH, Card.of(HEARTS, TEN))
                .withCardPlayed(EAST, Card.of(HEARTS, ACE))
                .withCardPlayed(SOUTH, Card.of(HEARTS, NINE))
                .withTrickWinner(WEST)
                .build());

        return previousTricks;
    }
}