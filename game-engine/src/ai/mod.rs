pub mod random_engine;
pub mod greedy_engine;
use crate::{BoardDefinition, Ship, Pos, BoardState};

pub trait Engine {
    fn get_name(&self) -> String;
    fn place_ships(&mut self, board: &BoardDefinition, ships: Vec<i32>) -> Vec<Ship>;
    fn get_shot(&mut self, board: &BoardState) -> Pos;
}
