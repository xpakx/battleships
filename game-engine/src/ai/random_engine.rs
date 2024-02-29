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

        for size in ships {
            let mut head;
            let mut tail;
            loop {
                let x = rng.gen_range(0..width);
                let y = rng.gen_range(0..height);

                head = Pos {x, y};
                tail = match rng.gen_ratio(1, 2) {
                    true => Pos {x: x + size - 1, y},
                    false => Pos {x, y: y + size - 1},
                };
                
                let on_board = tail.x < width && tail.y < height;

                let overlapping = placed_ships.iter().any(|ship| {
                    let x_start = if ship.head.x > 0 { ship.head.x - 1 } else { ship.head.x };
                    let y_start = if ship.head.y > 0 { ship.head.y - 1 } else { ship.head.y };
                    // TODO
                });

                if !overlapping && on_board {
                    break;
                }
            }

            placed_ships.push(Ship {
                head,
                tail,
                size,
            });
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
