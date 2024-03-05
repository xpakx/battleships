use lapin::{Channel, options::BasicAckOptions, message::DeliveryResult, Consumer};

use serde::{Serialize, Deserialize};
use crate::{rabbit::DESTINATION_EXCHANGE, ai::{random_engine, Engine}, data::{BoardDefinition, BoardState}};

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
                        println!("Failed to deserialize move message: {:?}", err);
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
struct ShipMsg {
    pub head_x: usize,
    pub head_y: usize,
    pub size: usize,
    pub orientation: Orientation,
}

#[derive(Debug, Serialize, Deserialize)]
enum Orientation {
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

fn get_engine() -> Box<dyn Engine> {
    return Box::new(random_engine::RandomEngine::new())
    // TODO
}

fn process_placement_event(game_msg: &AIMessage) -> EnginePlacementEvent {
    let mut engine = get_engine();
    let board_definition = BoardDefinition { width: 10, height: 10, adjacent_ships_allowed: false };
    let sizes = vec![1, 1, 1, 1, 2, 2, 2, 3, 3, 4];
    let ships: Vec<ShipMsg> = engine
        .place_ships(&board_definition, sizes)
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
        first_user: true, //TODO
        legal: true,
        malformed: None,
    }
}

fn process_move_event(game_msg: &AIMessage) -> EngineAIEvent {
    let board = BoardState::of(&game_msg.game_state, vec![], true);
    let mut engine = get_engine();
    let mv = engine.get_shot(&board);
    EngineAIEvent {
        game_id: game_msg.game_id,
        row: mv.x,
        column: mv.y,
        malformed: None,
    }
}
