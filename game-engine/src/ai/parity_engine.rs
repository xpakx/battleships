use crate::data::Orientation;
use crate::{BoardDefinition, Ship, Pos, Field, BoardState, validator, get_ship_sizes, RuleSet};
use crate::ai::Engine;

use rand::prelude::*;

pub struct ParityEngine {
    rng: ThreadRng,
}

impl ParityEngine {
    pub fn new() -> ParityEngine {
        ParityEngine {
            rng: thread_rng(),
        }
    }
}

impl Engine for ParityEngine {
    fn get_name(&self) -> String {
        String::from("Parity Engine")
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

            if validator::check_ships_are_on_board(board, &placed_ships) && validator::check_ship_placement(board, &placed_ships) {
                break;
            }
            placed_ships.clear();
        }

        placed_ships
    }

    fn get_shot(&mut self, board: &BoardState) -> Pos {
        // exploitation
        let mut hit_positions = Vec::new();

        for (x, row) in board.board.iter().enumerate() {
            for (y, field) in row.iter().enumerate() {
                if field == &Field::Hit {
                    hit_positions.push(Pos {x, y});
                }
            }
        }

        let mut candidates = Vec::new();
        if !hit_positions.is_empty() {
            for e in hit_positions {
                if e.x > 0 && board.board[e.x as usize - 1][e.y as usize] == Field::Empty {
                    candidates.push(Pos {x: e.x-1, y: e.y});
                }
                if e.y > 0 && board.board[e.x as usize][e.y as usize - 1] == Field::Empty {
                    candidates.push(Pos {x: e.x, y: e.y-1});
                }
                if e.x < board.definition.height-1 && board.board[e.x as usize + 1][e.y as usize] == Field::Empty {
                    candidates.push(Pos {x: e.x+1, y: e.y});
                }
                if e.y < board.definition.width-1 && board.board[e.x as usize][e.y as usize + 1] == Field::Empty {
                    candidates.push(Pos {x: e.x, y: e.y+1});
                }
            }

            let index = self.rng.gen_range(0..candidates.len());
            return candidates[index]
        }

        // exploration
        let mut empty_positions = Vec::new();
        let shortest_ship = get_ship_sizes(RuleSet::Classic).iter().min().unwrap().to_owned(); // TODO: only remaining ships

        for (x, row) in board.board.iter().enumerate() {
            for (y, field) in row.iter().enumerate() {
                let num = x * board.definition.width + y;
                if field == &Field::Empty && num % shortest_ship == 0 {
                    empty_positions.push(Pos {x, y});
                }
            }
        }
        let index = self.rng.gen_range(0..empty_positions.len());
        empty_positions[index]
    }
}
