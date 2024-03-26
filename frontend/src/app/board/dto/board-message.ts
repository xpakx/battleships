export interface BoardMessage {
    username1: String;
    username2: String;
    ai: boolean;

    state1: ("Sunk" | "Hit" | "Miss" | "Empty")[][];
    state2: ("Sunk" | "Hit" | "Miss" | "Empty")[][];
    currentPlayer: String;
    gameStarted: boolean;

    error?: String;
}