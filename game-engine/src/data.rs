#[derive(Clone, Copy, Debug)]
pub struct Pos {
    pub x: usize,
    pub y: usize,
}

#[derive(Debug)]
pub struct Ship {
    pub head: Pos,
    pub size: usize,
    pub orientation: Orientation,
}

pub struct BoardDefinition {
    pub width: usize,
    pub height: usize,
    pub adjacent_ships_allowed: bool,
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

#[derive(Debug, PartialEq, Eq)]
pub enum Orientation {
    Horizontal,
    Vertical,
}
