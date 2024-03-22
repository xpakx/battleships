#[derive(Clone, Copy, Debug, PartialEq)]
pub struct Pos {
    pub x: usize,
    pub y: usize,
}

#[derive(Debug, Clone, Copy)]
pub struct Ship {
    pub head: Pos,
    pub size: usize,
    pub orientation: Orientation,
}

pub struct BoardDefinition {
    pub width: usize,
    pub height: usize,
    pub adjacent_ships_allowed: bool,
    pub sizes: Vec<usize>,
}

#[derive(PartialEq)]
pub enum Field {
    Empty,
    Hit,
    Sunk,
    Miss,
}

pub struct BoardState {
    pub board: Vec<Vec<Field>>,
    pub remaining_ships: Vec<usize>,
    pub definition: BoardDefinition,
}

#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum Orientation {
    Horizontal,
    Vertical,
}

impl BoardState {
    pub fn of(board_str: &String, ships: Vec<usize>, adjacent: bool) -> BoardState {
        let mut board = vec![];
        let mut height = 0;
        let mut width = 0;
        for line in board_str.split('|') {
            height += 1;
            width = line.len();
            let mut row = vec![];
            for ch in line.chars() {
                let field = match ch {
                    '?' => Field::Empty,
                    'x' => Field::Sunk,
                    '.' => Field::Hit,
                    _ => Field::Miss,
                };
                row.push(field);
            }
            board.push(row);
        }
        BoardState {
            remaining_ships: ships.clone(),
            definition: BoardDefinition { width, height, adjacent_ships_allowed: adjacent, sizes: ships },
            board,
        }
    }
}
