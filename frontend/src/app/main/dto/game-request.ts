export interface GameRequest {
    type: "AI" | "USER";
    opponent?: String;
    rules: "Polish" | "Classic";
    aiType?: "Random" | "Greedy" | "Parity" | "None";
}