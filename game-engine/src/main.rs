mod data;
mod ai;
use data::*;
use ai::random_engine::*;
use ai::Engine;

fn main() {
    let mut engine = RandomEngine::new();
    println!("{}", engine.get_name());
    let board_definition = BoardDefinition { width: 10, height: 10 };
    let ships = engine.place_ships(&board_definition, vec![4, 3, 3, 2, 2, 2, 1, 1, 1, 1]);
    println!("{:?}", ships);
    print_board(ships, &board_definition);
    let shot = engine.get_shot(
        &BoardState { 
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
