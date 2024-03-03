mod data;
mod ai;
mod validator;
use data::*;
use ai::random_engine::*;
use ai::greedy_engine::*;
use ai::Engine;
use validator::check_ship_placement;
use crate::validator::check_all_ships_are_placed;
use crate::validator::check_ships_are_on_board;

fn main() {
    let mut engine = RandomEngine::new();
    println!("{}", engine.get_name());
    let board_definition = BoardDefinition { width: 10, height: 10, adjacent_ships_allowed: false };
    let sizes = vec![1, 1, 1, 1, 2, 2, 2, 3, 3, 4];
    let ships = engine.place_ships(&board_definition, sizes.clone());
    println!("{:?}", ships);
    println!("{:?}", check_ship_placement(&board_definition, &ships));
    println!("{:?}", check_ships_are_on_board(&board_definition, &ships));
    println!("{:?}", check_all_ships_are_placed(&ships, sizes.clone()));
    print_board(ships.clone(), &board_definition);
    let shot = engine.get_shot(
        &BoardState { 
            board: vec![
                vec![Field::Empty, Field::Hit, Field::Empty, Field::Sunk], 
                vec![Field::Empty, Field::Empty, Field::Empty, Field::Miss], 
                vec![Field::Empty, Field::Empty, Field::Empty, Field::Empty], 
                vec![Field::Empty, Field::Empty, Field::Empty, Field::Empty], 
            ], 
            remaining_ships: vec![1, 1, 2],
            definition: board_definition,
        }
        );
    println!("{}, {}", shot.x, shot.y);
    let result = hit_result(&ships, &shot);
    println!("{:?}", result);


    let mut engine = GreedyEngine::new();
    println!("{}", engine.get_name());
    let board_definition = BoardDefinition { width: 10, height: 10, adjacent_ships_allowed: true };
    let ships = engine.place_ships(&board_definition, vec![4, 3, 3, 2, 2, 2, 1, 1, 1, 1]);
    println!("{:?}", ships);
    println!("{:?}", check_ship_placement(&board_definition, &ships));
    print_board(ships, &board_definition);
    let shot = engine.get_shot(
        &BoardState { 
            board: vec![
                vec![Field::Empty, Field::Hit, Field::Empty, Field::Sunk], 
                vec![Field::Empty, Field::Empty, Field::Empty, Field::Miss], 
                vec![Field::Empty, Field::Empty, Field::Empty, Field::Empty], 
                vec![Field::Empty, Field::Empty, Field::Empty, Field::Empty], 
            ], 
            remaining_ships: vec![1, 1, 2],
            definition: board_definition,
        }
        );
    println!("{}, {}", shot.x, shot.y);
}

fn print_board(ships: Vec<Ship>, board_definition: &BoardDefinition) {
    let width = board_definition.width as usize;
    let height = board_definition.height as usize;

    let mut board = vec![vec!['.'; width]; height];

    for ship in ships {
        if ship.orientation == Orientation::Horizontal {
            for y in ship.head.y..ship.head.y+ship.size {
                board[ship.head.x][y] = 'X';
            }
        } else {
            for x in ship.head.x..ship.head.x+ship.size {
                board[x][ship.head.y] = 'X';
            }
        }
    }

    for row in board.iter() {
        println!("{}", row.iter().collect::<String>());
    }
}

#[derive(Debug)]
pub enum HitResult {
    Hit(Ship),
    Miss,
}

pub fn hit_result(ships: &Vec<Ship>, pos: &Pos) -> HitResult {
    for ship in ships {
        let mut ship_positions = Vec::new();
        for i in 0..ship.size {
            match ship.orientation {
                Orientation::Horizontal => {
                    ship_positions.push(Pos { x: ship.head.x, y: ship.head.y + i });
                }
                Orientation::Vertical => {
                    ship_positions.push(Pos { x: ship.head.x + i, y: ship.head.y });
                }
            }
        }
        if ship_positions.contains(pos) {
            return HitResult::Hit(*ship);
        }
    }
    HitResult::Miss
}

#[derive(Debug)]
pub enum MoveResult {
    Miss,
    Hit(Ship),
    Sunk(Ship),
}

pub fn move_result(board: &BoardState, ships: &Vec<Ship>, pos: &Pos) -> MoveResult {
    match hit_result(ships, pos) {
        HitResult::Miss => MoveResult::Miss,
        HitResult::Hit(ship) => {
            let mut ship_positions = Vec::new();
            for i in 0..ship.size {
                match ship.orientation {
                    Orientation::Horizontal => {
                        ship_positions.push(Pos { x: ship.head.x, y: ship.head.y + i });
                    }
                    Orientation::Vertical => {
                        ship_positions.push(Pos { x: ship.head.x + i, y: ship.head.y });
                    }
                }
            }
            let test = ship_positions.iter().all(|pos| board.board[pos.x][pos.y] == Field::Hit);
            match test {
                true => MoveResult::Sunk(ship),
                false => MoveResult::Hit(ship),
            }
        }
    }
}
