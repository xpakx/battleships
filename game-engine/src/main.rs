mod data;
mod ai;
mod validator;
mod rabbit;
use data::*;
use std::env;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let rabbit_env = env::var("RABBIT_URL");
    let rabbit_uri = match rabbit_env {
        Ok(env) => env,
        _ => String::from("amqp://guest:guest@localhost:5672")
    };
    let result = rabbit::consumer(&rabbit_uri).await;
    if let Err(err) = result {
        println!("{:?}", err);
    }
    loop {
    }
}

pub enum RuleSet {
    Classic, // 1990 Milton Bradley version
    Polish,
}

pub fn get_board_definition(ruleset: RuleSet) -> BoardDefinition {
    match ruleset {
        RuleSet::Classic => BoardDefinition { width: 10, height: 10, adjacent_ships_allowed: true, sizes: get_ship_sizes(ruleset) },
        RuleSet::Polish => BoardDefinition { width: 10, height: 10, adjacent_ships_allowed: false, sizes: get_ship_sizes(ruleset) },
    }
}

pub fn get_ship_sizes(ruleset: RuleSet) -> Vec<usize> {
    match ruleset {
        RuleSet::Classic => vec![2, 3, 3, 4, 5],
        RuleSet::Polish => vec![1, 1, 1, 1, 2, 2, 2, 3, 3, 4],
    }
}

#[allow(dead_code)]
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
    Illegal,
}

pub fn move_result(board: &BoardState, ships: &Vec<Ship>, pos: &Pos) -> MoveResult {
    if  pos.x >= board.definition.height || pos.y >= board.definition.width {
        return MoveResult::Illegal;
    }
    if board.board[pos.x][pos.y] != Field::Empty {
        return MoveResult::Illegal;
    }

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
            let test = ship_positions.iter().all(|p| {board.board[p.x][p.y] == Field::Hit || p == pos});
            match test {
                true => MoveResult::Sunk(ship),
                false => MoveResult::Hit(ship),
            }
        }
    }
}
