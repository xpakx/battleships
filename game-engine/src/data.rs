#[derive(Clone, Copy, Debug)]
pub struct Pos {
    pub x: i32,
    pub y: i32,
}

#[derive(Debug)]
pub struct Ship {
    pub head: Pos,
    pub tail: Pos,
    pub size: i32,
}

pub struct BoardDefinition {
    pub width: i32,
    pub height: i32,
}

#[derive(PartialEq)]
pub enum Field {
    Empty,
    Hit,
    Drown,
    Miss,
}

pub struct BoardState {
    pub board: Vec<Vec<Field>>,
    pub remaining_ships: Vec<i32>,
}
