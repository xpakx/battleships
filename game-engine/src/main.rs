fn main() {
    println!("Hello, world!");
}

struct Pos {
    x: i32,
    y: i32,
}

struct Ship {
    head: Pos,
    tail: Pos,
    size: i32,
}

struct BoardDefinition {
    width: i32,
    height: i32,
}

enum Field {
    Empty,
    Hit,
    Drown,
    Miss,
}

struct BoardState {
    board: Vec<Vec<Field>>,
    remaining_ships: Vec<i32>,
}

trait Engine {
    fn get_name(&self) -> String;
    fn place_ships(&self, board: &BoardDefinition, ships: Vec<i32>) -> Vec<Ship>;
    fn get_shot(&self, board: &BoardState) -> Pos;
}

struct RandomEngine {}

impl Engine for RandomEngine {
    fn get_name(&self) -> String {
        String::from("Random Engine")
    }

    fn place_ships(&self, _board: &BoardDefinition, _ships: Vec<i32>) -> Vec<Ship> {
        vec![]
    }

    fn get_shot(&self, _board: &BoardState) -> Pos {
        Pos {x: 0, y: 0}
    }
}
