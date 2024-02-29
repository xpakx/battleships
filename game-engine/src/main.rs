mod data;
mod ai;
mod validator;
use data::*;
use ai::random_engine::*;
use ai::greedy_engine::*;
use ai::Engine;
use validator::check_ship_placement;


fn main() {
    let mut engine = RandomEngine::new();
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
        for x in ship.head.x..=ship.tail.x {
            for y in ship.head.y..=ship.tail.y {
                board[y as usize][x as usize] = 'X';
            }
        }
    }

    for row in board.iter() {
        println!("{}", row.iter().collect::<String>());
    }
}
