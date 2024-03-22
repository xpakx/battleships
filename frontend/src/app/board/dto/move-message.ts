export interface MoveMessage {
    player: String;
    x: number;
    y: number;

    legal: boolean;

    result: "Miss" | "Hit" | "Sunk";

    message?: String;
    finished: boolean ;
    won: boolean ;
    winner?: String;
}