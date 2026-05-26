package com.deathbound.model;

import com.deathbound.config.GameSettings;

public class RoundManager {
    private static final float PLAYER_ONE_START_X = 110f;
    private static final float RIGHT_SIDE_MARGIN = 156f;

    public enum Phase {
        FIGHTING,
        ROUND_OVER,
        MATCH_OVER
    }

    private Phase phase = Phase.FIGHTING;
    private int roundNumber = 1;
    private int playerOneRoundWins;
    private int playerTwoRoundWins;
    private final GameSettings settings = GameSettings.getInstance();
    private float timeLeft = settings.getRoundTime();
    private String resultMessage = "ROUND 1";

    public void update(float delta, Fighter playerOne, Fighter playerTwo) {
        if (phase != Phase.FIGHTING) {
            return;
        }

        timeLeft = Math.max(0f, timeLeft - delta);
        if (!playerOne.isAlive() || !playerTwo.isAlive() || timeLeft == 0f) {
            finishRound(playerOne, playerTwo);
        }
    }

    public void continueAfterRound(Fighter playerOne, Fighter playerTwo, Arena arena) {
        if (phase == Phase.MATCH_OVER) {
            playerOneRoundWins = 0;
            playerTwoRoundWins = 0;
            roundNumber = 1;
        } else if (phase == Phase.ROUND_OVER) {
            roundNumber++;
        }

        resetRound(playerOne, playerTwo, arena);
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean isFighting() {
        return phase == Phase.FIGHTING;
    }

    public boolean isWaitingForContinue() {
        return phase == Phase.ROUND_OVER || phase == Phase.MATCH_OVER;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public int getPlayerOneRoundWins() {
        return playerOneRoundWins;
    }

    public int getPlayerTwoRoundWins() {
        return playerTwoRoundWins;
    }

    public int getTimerSeconds() {
        return (int) Math.ceil(timeLeft);
    }

    public String getResultMessage() {
        return resultMessage;
    }

    private void finishRound(Fighter playerOne, Fighter playerTwo) {
        playerOne.stopAction();
        playerTwo.stopAction();

        int winner = determineWinner(playerOne, playerTwo);
        if (winner == 1) {
            playerOneRoundWins++;
            resultMessage = "PLAYER 1 WINS ROUND";
        } else if (winner == 2) {
            playerTwoRoundWins++;
            resultMessage = "PLAYER 2 WINS ROUND";
        } else {
            resultMessage = "DRAW ROUND";
        }

        if (playerOneRoundWins >= settings.getRoundsToWin()) {
            phase = Phase.MATCH_OVER;
            resultMessage = "PLAYER 1 WINS MATCH";
        } else if (playerTwoRoundWins >= settings.getRoundsToWin()) {
            phase = Phase.MATCH_OVER;
            resultMessage = "PLAYER 2 WINS MATCH";
        } else {
            phase = Phase.ROUND_OVER;
        }
    }

    private int determineWinner(Fighter playerOne, Fighter playerTwo) {
        if (!playerOne.isAlive() && !playerTwo.isAlive()) {
            return 0;
        }
        if (!playerOne.isAlive()) {
            return 2;
        }
        if (!playerTwo.isAlive()) {
            return 1;
        }
        if (playerOne.getHealth() > playerTwo.getHealth()) {
            return 1;
        }
        if (playerTwo.getHealth() > playerOne.getHealth()) {
            return 2;
        }
        return 0;
    }

    private void resetRound(Fighter playerOne, Fighter playerTwo, Arena arena) {
        float safeRightX = Math.max(0f, arena.getWidth() - playerTwo.getWidth());
        float preferredRightX = Math.max(PLAYER_ONE_START_X + playerOne.getWidth() + 120f, arena.getWidth() - RIGHT_SIDE_MARGIN);
        float playerTwoStartX = Math.min(safeRightX, preferredRightX);
        playerOne.resetForRound(PLAYER_ONE_START_X, arena.getFloorY(), true);
        playerTwo.resetForRound(playerTwoStartX, arena.getFloorY(), false);
        timeLeft = settings.getRoundTime();
        phase = Phase.FIGHTING;
        resultMessage = "ROUND " + roundNumber;
    }
}
