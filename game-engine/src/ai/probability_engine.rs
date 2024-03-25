use crate::data::Orientation;
use crate::{BoardDefinition, Ship, Pos, Field, BoardState, validator};
use crate::ai::Engine;

use rand::prelude::*;

pub struct ProbabilityDensityEngine {
    rng: ThreadRng,
}

impl ProbabilityDensityEngine {
    pub fn new() -> ProbabilityDensityEngine {
        ProbabilityDensityEngine {
            rng: thread_rng(),
        }
    }
}

impl Engine for ProbabilityDensityEngine {
    fn get_name(&self) -> String {
        String::from("Parity Engine")
    }

    fn place_ships(&mut self, board: &BoardDefinition) -> Vec<Ship> {
        let ships: &Vec<usize> = &board.sizes;
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
        let placeable: Vec<Vec<FieldType>> = board.board.iter().map(
            |row| {
                row.iter().map(|field| match field {
                    Field::Hit => FieldType::Bonus,
                    Field::Empty => FieldType::Free,
                    _ => FieldType::Obstacle,
                }).collect()
            }
            ).collect();

        let mut density: Vec<Vec<usize>> = Vec::with_capacity(board.board.len());
        for _ in 0..board.board[0].len() {
            let row: Vec<usize> = vec![0; board.board.len()];
            density.push(row);
        }

        let ships = &board.remaining_ships;
        for ship in ships {
            density = place_ship(ship, &density, &placeable);
        }

        let max_density = density.iter().flat_map(|row| row.iter()).max().unwrap_or(&0);

        let positions: Vec<Pos> = density.iter().enumerate().flat_map(|(x, row)| {
            row.iter().enumerate().filter_map(move |(y, &val)| {
                if val == *max_density {
                    Some(Pos { x, y })
                } else {
                    None
                }
            })
        }).collect();

        let index = self.rng.gen_range(0..positions.len());
        positions[index]
    }
}

enum FieldType {
    Free,
    Bonus,
    Obstacle,
}

fn place_ship(_size: &usize, density: &Vec<Vec<usize>>, _board: &Vec<Vec<FieldType>>) -> Vec<Vec<usize>> {
    // TODO
    return density.clone();
}
