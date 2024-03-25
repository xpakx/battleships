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


#[derive(Debug, Clone, Copy, Eq, PartialEq)]
enum FieldType {
    Free,
    Bonus,
    Obstacle,
}

struct Position {
    pos: Pos,
    field_type: FieldType,
}

fn place_ship(size: &usize, density: &Vec<Vec<usize>>, board: &Vec<Vec<FieldType>>) -> Vec<Vec<usize>> {
    // TODO
    // Vertical
    let mut density: Vec<Vec<usize>> = density.clone();
    for i in 0..board.len() {
        for j in 0..(board[i].len()-size) {
            let positions = get_ship_positions(size, i, j, Orientation::Vertical, board);
            update_density(&mut density, &positions);
        }
    }
    return density;
}

fn update_density(density: &mut Vec<Vec<usize>>, positions: &Vec<Position>) {
    if is_free(positions) {
        let to_add = get_new_density(positions);
        for pos in positions {
            density[pos.pos.x][pos.pos.y] += to_add;
        }
    }
}

fn is_free(positions: &Vec<Position>) -> bool {
    positions.iter().all(|pos| {
        match pos.field_type {
            FieldType::Obstacle => false,
            _ => true,
        }
    })
}

fn has_bonus(positions: &Vec<Position>) -> bool {
    positions.iter().all(|pos| {
        match pos.field_type {
            FieldType::Bonus => true,
            _ => false,
        }
    })
}

fn get_new_density(positions: &Vec<Position>) -> usize {
    match has_bonus(positions) {
        true => 20,
        false => 1,
    }
}

fn get_ship_positions(size: &usize, x: usize, y: usize, dir: Orientation, board: &Vec<Vec<FieldType>>) -> Vec<Position> {
    match dir {
        Orientation::Vertical => {
            let mut fields: Vec<Position> = vec![];
            if x+size > board.len() {
                return fields
            }
            for i in x..(x+size-1) {
                fields.push(Position { pos: Pos { x: i, y }, field_type: board[i][y] });
            }
            fields
        },
        Orientation::Horizontal => {
            let mut fields: Vec<Position> = vec![];
            if y+size > board[x].len() {
                return fields
            }
            for i in y..(y+size-1) {
                fields.push(Position { pos: Pos { x, y: i }, field_type: board[x][i] });
            }
            fields
        },
    }
}
