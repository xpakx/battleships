use lapin::{Channel, options::BasicAckOptions, message::DeliveryResult, Consumer};

use serde::{Serialize, Deserialize};
use crate::{rabbit::DESTINATION_EXCHANGE, data::{Ship, Pos, Orientation}, validator::{check_ship_placement, check_ships_are_on_board, check_all_ships_are_placed}, get_ship_sizes, RuleSet};

use super::ai_client::{ShipMsg, ReqRuleSet, to_board_definition, to_rule_set};

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
                let move_msg: PlacementMessage = match serde_json::from_str(message) {
                    Ok(msg) => msg,
                    Err(err) => {
                        println!("Failed to deserialize placement message: {:?}", err);
                        println!("{:?}", message);
                        return;
                    }
                };
                println!("Received message: {:?}", &move_msg);

                let response = process_placement_event(&move_msg);
                println!("Response: {:?}", &response);
                let response = serde_json::to_string(&response).unwrap();

                if let Err(err) = channel
                    .basic_publish(
                        DESTINATION_EXCHANGE,
                        "placement",
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
struct PlacementMessage {
    game_id: i32,
    first_user: bool,
    ships: String,
    ruleset: ReqRuleSet,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct EngineEvent {
    game_id: i32,
    first_user: bool,
    ships: String,
    legal: bool,
    malformed: Option<bool>,
}

impl Default for EngineEvent {
    fn default() -> EngineEvent {
        EngineEvent { 
            game_id: -1,
            first_user: false,
            ships: String::from(""),
            legal: false,
            malformed: None,
        } 
    } 
}

fn process_placement_event(game_msg: &PlacementMessage) -> EngineEvent {
    let ships: Vec<ShipMsg> = match serde_json::from_str(&game_msg.ships) {
        Ok(ships) => ships,
        Err(_) => {
            return Default::default();
        }
    };
    let ships: Vec<Ship> = ships.iter().map(|ship| Ship {
        head: Pos{x: ship.head_x, y: ship.head_y},
        size: ship.size,
        orientation: match ship.orientation {
            super::ai_client::Orientation::Vertical => Orientation::Vertical,
            super::ai_client::Orientation::Horizontal => Orientation::Horizontal,
        },
    }).collect();
    let board_definition = to_board_definition(&game_msg.ruleset);
    let sizes = get_ship_sizes(to_rule_set(&game_msg.ruleset));
    let correct = check_ship_placement(&board_definition, &ships);
    let on_board = check_ships_are_on_board(&board_definition, &ships);
    let all = check_all_ships_are_placed(&ships, sizes);
    EngineEvent { 
        game_id: game_msg.game_id,
        first_user: game_msg.first_user,
        ships: game_msg.ships.clone(),
        legal: correct && on_board && all,
        malformed: None,
    }
}
