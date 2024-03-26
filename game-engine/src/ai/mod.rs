pub mod random_engine;
pub mod greedy_engine;
pub mod parity_engine;
pub mod probability_engine;
use crate::{BoardDefinition, Ship, Pos, BoardState};

pub trait Engine {
    fn get_name(&self) -> String;
    fn place_ships(&mut self, board: &BoardDefinition) -> Vec<Ship>;
    fn get_shot(&mut self, board: &BoardState) -> Pos;
}

pub enum EngineType {
    Random,
    Greedy,
    Parity,
    Probability,
}

pub fn get_engine(engine: EngineType) -> Box<dyn Engine> {
    match engine {
        EngineType::Random => Box::new(random_engine::RandomEngine::new()),
        EngineType::Greedy => Box::new(greedy_engine::GreedyEngine::new()),
        EngineType::Parity => Box::new(parity_engine::ParityEngine::new()),
        EngineType::Probability => Box::new(probability_engine::ProbabilityDensityEngine::new()),
    }
}
