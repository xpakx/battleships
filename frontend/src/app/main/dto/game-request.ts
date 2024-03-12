export interface GameRequest {
    type: String;
    opponent?: String;
    rules: "Polish" | "Classic";
    aiType?: "Random" | "Greedy" | "None";
}