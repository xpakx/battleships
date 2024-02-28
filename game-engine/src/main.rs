use rand::prelude::*;

fn main() {
    let mut engine = RandomEngine::new();
    println!("{}", engine.get_name());
    let shot = engine.get_shot(
        &BoardState 
        { 
            board: vec![
                vec![Field::Empty, Field::Hit, Field::Empty, Field::Drown], 
                vec![Field::Empty, Field::Empty, Field::Empty, Field::Miss], 
                vec![Field::Empty, Field::Empty, Field::Empty, Field::Empty], 
                vec![Field::Empty, Field::Empty, Field::Empty, Field::Empty], 
            ], 
            remaining_ships: vec![1, 1, 2]
        }
        );
    println!("{}, {}", shot.x, shot.y);
}

#[derive(Clone, Copy)]
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

#[derive(PartialEq)]
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
    fn get_shot(&mut self, board: &BoardState) -> Pos;
}



struct RandomEngine {
    rng: ThreadRng,
}

impl RandomEngine {
    fn new() -> RandomEngine {
        RandomEngine {
            rng: thread_rng(),
        }
    }
}

impl Engine for RandomEngine {
    fn get_name(&self) -> String {
        String::from("Random Engine")
    }

    fn place_ships(&self, _board: &BoardDefinition, _ships: Vec<i32>) -> Vec<Ship> {
        vec![]
    }

    fn get_shot(&mut self, board: &BoardState) -> Pos {
        let mut empty_positions = Vec::new();

        for (x, row) in board.board.iter().enumerate() {
            for (y, field) in row.iter().enumerate() {
                if field == &Field::Empty {
                    empty_positions.push(Pos {x: x as i32, y: y as i32});
                }
            }
        }

        if empty_positions.is_empty() {
            return Pos { x: -1, y: -1 };
        }
        let index = self.rng.gen_range(0..empty_positions.len());
        empty_positions[index]
    }
}
