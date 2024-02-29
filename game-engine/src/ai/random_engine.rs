use rand::prelude::*;
use crate::data::Orientation;
use crate::{BoardDefinition, Ship, Pos, Field, BoardState, validator};
use crate::ai::Engine;

pub struct RandomEngine {
    rng: ThreadRng,
}

impl RandomEngine {
    pub fn new() -> RandomEngine {
        RandomEngine {
            rng: thread_rng(),
        }
    }
}

impl Engine for RandomEngine {
    fn get_name(&self) -> String {
        String::from("Random Engine")
    }

    fn place_ships(&mut self, board: &BoardDefinition, ships: Vec<usize>) -> Vec<Ship> {
        let mut rng = thread_rng();

        let width = board.width;
        let height = board.height;

        let mut placed_ships: Vec<Ship> = Vec::new();

        loop {
            for size in ships.iter() {
                let x = rng.gen_range(0..width);
                let y = rng.gen_range(0..height);

                let head = Pos {x, y};
                let orientation = match rng.gen_ratio(1, 2) {
                    true => Orientation::Horizontal,
                    false => Orientation::Vertical,
                };

                placed_ships.push(Ship {
                    head,
                    size: size.clone(),
                    orientation,
                });

            }
            
            let test = placed_ships.iter().all(|ship| {
                match ship.orientation {
                    Orientation::Horizontal => {
                        ship.head.y + ship.size - 1 < board.width
                    },
                    Orientation::Vertical => {
                        ship.head.x + ship.size - 1 < board.height
                    },
                }
            });

            if test && validator::check_ship_placement(board, &placed_ships) {
                break;
            }
            placed_ships.clear();
        }

        placed_ships
    }

    fn get_shot(&mut self, board: &BoardState) -> Pos {
        let mut empty_positions = Vec::new();

        for (x, row) in board.board.iter().enumerate() {
            for (y, field) in row.iter().enumerate() {
                if field == &Field::Empty {
                    empty_positions.push(Pos {x, y});
                }
            }
        }

        if empty_positions.is_empty() {
            return Pos { x: 0, y: 0 };
        }
        let index = self.rng.gen_range(0..empty_positions.len());
        empty_positions[index]
    }
}
