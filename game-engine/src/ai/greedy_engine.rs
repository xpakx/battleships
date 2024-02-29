use crate::{BoardDefinition, Ship, Pos, Field, BoardState};
use crate::ai::Engine;

use rand::prelude::*;

pub struct GreedyEngine {
    rng: ThreadRng,
}

impl GreedyEngine {
    pub fn new() -> GreedyEngine {
        GreedyEngine {
            rng: thread_rng(),
        }
    }
}

impl Engine for GreedyEngine {
    fn get_name(&self) -> String {
        String::from("Greedy Engine")
    }

    fn place_ships(&mut self, _board: &BoardDefinition, _ships: Vec<usize>) -> Vec<Ship> {
        vec![]
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

        for (x, row) in board.board.iter().enumerate() {
            for (y, field) in row.iter().enumerate() {
                if field == &Field::Empty {
                    empty_positions.push(Pos {x, y});
                }
            }
        }
        let index = self.rng.gen_range(0..empty_positions.len());
        empty_positions[index]
    }
}
