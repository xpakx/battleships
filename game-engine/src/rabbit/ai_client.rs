use lapin::{Channel, options::BasicAckOptions, message::DeliveryResult, Consumer};

use serde::{Serialize, Deserialize};
use crate::{rabbit::DESTINATION_EXCHANGE, ai::{get_engine, EngineType, Engine}, data::{BoardState, BoardDefinition}, get_board_definition, RuleSet, get_ship_sizes};

pub fn set_delegate(consumer: Consumer, channel: Channel) {
    consumer.set_delegate({
        move |delivery: DeliveryResult| {
            println!("New message");
            let channel = channel.clone();
            async move {
                let channel = channel.clone();
                let delivery = match delivery {
                    Ok(Some(delivery)) => delivery,
                    Ok(None) => return,
                    Err(error) => {
                        println!("Failed to consume queue message {}", error);
                        return;
                    }
                };

                let message = std::str::from_utf8(&delivery.data).unwrap();
                let move_msg: AIMessage = match serde_json::from_str(message) {
                    Ok(msg) => msg,
                    Err(err) => {
                        println!("Failed to deserialize ai message: {:?}", err);
                        println!("{:?}", message);
                        return;
                    }
                };
                println!("Received message: {:?}", &move_msg);

                let response = match &move_msg.phase {
                    Phase::Move =>  {
                        let r = process_move_event(&move_msg);
                        serde_json::to_string(&r).unwrap()
                    }
                    Phase::Placement => {
                        let r = process_placement_event(&move_msg);
                        serde_json::to_string(&r).unwrap()
                    }
                };
                println!("Response: {:?}", &response);

                if let Err(err) = channel
                    .basic_publish(
                        DESTINATION_EXCHANGE,
                        match &move_msg.phase {
                            Phase::Move => "ai.move",
                            Phase::Placement => "placement"
                            
                        },
                        Default::default(),
                        response.into_bytes().as_slice(),
                        Default::default(),
                        )
                        .await {
                            println!("Failed to publish message to destination exchange: {:?}", err);
                        }

                delivery
                    .ack(BasicAckOptions::default())
                    .await
                    .expect("Failed to acknowledge message");
            }
        }
    }
    );
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct AIMessage {
    game_id: i32,
    game_state: String,
    phase: Phase,
    ruleset: ReqRuleSet,
    #[serde(rename = "type")]
    ai_type: AIType,
    remaining_ships: Option<Vec<usize>>
}

#[derive(Debug, Serialize, Deserialize)]
pub enum ReqRuleSet {
    Classic,
    Polish,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum AIType {
    Random,
    Greedy,
    Parity,
}

#[derive(Debug, Serialize, Deserialize)]
enum Phase {
    Move,
    Placement
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct EngineAIEvent {
    game_id: i32,
    row: usize,
    column: usize,
    malformed: Option<bool>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct EnginePlacementEvent {
    game_id: i32,
    ships: String,
    first_user: bool,
    legal: bool,
    malformed: Option<bool>,
}


#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ShipMsg {
    pub head_x: usize,
    pub head_y: usize,
    pub size: usize,
    pub orientation: Orientation,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum Orientation {
    Horizontal,
    Vertical,
}

impl Default for EngineAIEvent {
    fn default() -> EngineAIEvent {
        EngineAIEvent { 
            game_id: -1,
            row: 0,
            column: 0,
            malformed: None,
        } 
    } 
}

fn to_engine(ai_type: &AIType) -> Box<dyn Engine> {
    match ai_type {
        AIType::Random => get_engine(EngineType::Random),
        AIType::Greedy => get_engine(EngineType::Greedy),
        AIType::Parity => get_engine(EngineType::Parity),
    }
}

pub fn to_board_definition(rules: &ReqRuleSet) -> BoardDefinition {
    match rules {
        ReqRuleSet::Polish => get_board_definition(RuleSet::Polish),
        ReqRuleSet::Classic => get_board_definition(RuleSet::Classic),
    }
}

pub fn to_rule_set(rules: &ReqRuleSet) -> RuleSet {
    match rules {
        ReqRuleSet::Polish => RuleSet::Polish,
        ReqRuleSet::Classic => RuleSet::Classic,
    }
}

fn process_placement_event(game_msg: &AIMessage) -> EnginePlacementEvent {
    let mut engine = to_engine(&game_msg.ai_type);
    let board_definition = to_board_definition(&game_msg.ruleset);
    let ships: Vec<ShipMsg> = engine
        .place_ships(&board_definition)
        .iter()
        .map(|ship| 
             ShipMsg {
                 head_x: ship.head.x,
                 head_y: ship.head.y,
                 size: ship.size,
                 orientation: match ship.orientation {
                     crate::data::Orientation::Horizontal => Orientation::Horizontal,
                     crate::data::Orientation::Vertical => Orientation::Vertical,
                 }
             }
            )
        .collect();
    let ships = serde_json::to_string(&ships).unwrap();

    EnginePlacementEvent {
        game_id: game_msg.game_id,
        ships,
        first_user: false,
        legal: true,
        malformed: None,
    }
}

fn process_move_event(game_msg: &AIMessage) -> EngineAIEvent {
    let ships = match &game_msg.remaining_ships {
        None => get_ship_sizes(to_rule_set(&game_msg.ruleset)),
        Some(ships) => ships.clone(),
    };
    let board = BoardState::of(&game_msg.game_state, ships, to_rule_set(&game_msg.ruleset));
    let mut engine = to_engine(&game_msg.ai_type);
    let mv = engine.get_shot(&board);
    EngineAIEvent {
        game_id: game_msg.game_id,
        row: mv.x,
        column: mv.y,
        malformed: None,
    }
}
