use crate::{RuleSet, get_board_definition};

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
    pub fn of(board_str: &String, ships: Vec<usize>, rules: RuleSet) -> BoardState {
        let mut board = vec![];
        for line in board_str.split('|') {
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
            remaining_ships: ships,
            definition: get_board_definition(rules),
            board,
        }
    }
}
