use rand::prelude::*;

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

#[derive(Clone, Copy, Debug)]
struct Pos {
    x: i32,
    y: i32,
}

#[derive(Debug)]
struct Ship {
    head: Pos,
    tail: Pos,
    size: i32,
}

struct BoardDefinition {
    width: i32,
    height: i32,
}

#[derive(PartialEq)]
enum Field {
    Empty,
    Hit,
    Drown,
    Miss,
}

struct BoardState {
    board: Vec<Vec<Field>>,
    remaining_ships: Vec<i32>,
}

trait Engine {
    fn get_name(&self) -> String;
    fn place_ships(&mut self, board: &BoardDefinition, ships: Vec<i32>) -> Vec<Ship>;
    fn get_shot(&mut self, board: &BoardState) -> Pos;
}



struct RandomEngine {
    rng: ThreadRng,
}

impl RandomEngine {
    fn new() -> RandomEngine {
        RandomEngine {
            rng: thread_rng(),
        }
    }
}

impl Engine for RandomEngine {
    fn get_name(&self) -> String {
        String::from("Random Engine")
    }

    fn place_ships(&mut self, board: &BoardDefinition, ships: Vec<i32>) -> Vec<Ship> {
        let mut placed_ships: Vec<Ship> = Vec::new();
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
                    let x_end = if ship.tail.x + 1 < width { ship.tail.x + 1 } else { ship.tail.x };
                    let y_end = if ship.tail.y + 1 < height { ship.tail.y + 1 } else { ship.tail.y };

                    // TODO
                    (x_start..=x_end).contains(&x) && (y_start..=y_end).contains(&y)
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
                    empty_positions.push(Pos {x: x as i32, y: y as i32});
                }
            }
        }

        if empty_positions.is_empty() {
            return Pos { x: -1, y: -1 };
        }
        let index = self.rng.gen_range(0..empty_positions.len());
        empty_positions[index]
    }
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
