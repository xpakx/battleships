use lapin::{Channel, options::BasicAckOptions, message::DeliveryResult, Consumer};

use serde::{Serialize, Deserialize};
use crate::{rabbit::DESTINATION_EXCHANGE, data::{BoardState, Ship, Pos, Field}, move_result, MoveResult, validator, get_ship_sizes};

use super::ai_client::{ShipMsg, to_rule_set, ReqRuleSet};
use crate::data::Orientation;

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
                let move_msg: MoveMessage = match serde_json::from_str(message) {
                    Ok(msg) => msg,
                    Err(err) => {
                        println!("Failed to deserialize move message: {:?}", err);
                        return;
                    }
                };
                println!("Received message: {:?}", &move_msg);

                let response = process_move_event(&move_msg);
                println!("Response: {:?}", &response);
                let response = serde_json::to_string(&response).unwrap();

                if let Err(err) = channel
                    .basic_publish(
                        DESTINATION_EXCHANGE,
                        "validation.move",
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
struct MoveMessage {
    game_state: String,
    targets: String,
    game_id: i32,
    column: usize,
    row: usize,
    ruleset: ReqRuleSet,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct EngineEvent {
    game_id: i32,
    row: usize,
    column: usize,
    legal: bool,
    finished: bool,
    result: String,
    new_state: String,
    malformed: Option<bool>,
}

impl Default for EngineEvent {
    fn default() -> EngineEvent {
        EngineEvent { 
            game_id: -1,
            row: 0,
            column: 0,
            legal: false,
            finished: false,
            result: String::from(""),
            new_state: String::from(""),
            malformed: Some(true),
        } 
    } 
}

fn process_move_event(game_msg: &MoveMessage) -> EngineEvent {
    let mut board = BoardState::of(&game_msg.game_state, vec![], true);
    let ships: Vec<ShipMsg> = match serde_json::from_str(&game_msg.targets) {
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

    let hit_pos = Pos {x: game_msg.row, y: game_msg.column};

    let result = move_result(&board, &ships, &hit_pos);

    let legal_move = match result {
            MoveResult::Illegal => false,
            _ => true,
    };

    if legal_move {
        board.board[hit_pos.x][hit_pos.y] = match result {
            MoveResult::Miss=> Field::Miss,
            MoveResult::Hit(_) => Field::Hit,
            MoveResult::Sunk(_) => Field::Sunk,
            MoveResult::Illegal => panic!("Shouldn't happen"),
        };

        if let MoveResult::Sunk(ship) = result {
            for i in 0..ship.size {
                match ship.orientation {
                    Orientation::Horizontal => board.board[ship.head.x][ship.head.y+i] = Field::Sunk,
                    Orientation::Vertical => board.board[ship.head.x+i][ship.head.y] = Field::Sunk,
                }
            }
        }
    }

    let finished = validator::check_win(&board, get_ship_sizes(to_rule_set(&game_msg.ruleset)));

    let mut state: Vec<char> = vec![];
    for (i, row) in board.board.iter().enumerate() {
        if i != 0 {
            state.push('|');
        }
        for field in row.iter(){
            state.push(
                match field {
                    Field::Miss=> 'o',
                    Field::Hit => '.',
                    Field::Sunk => 'x',
                    Field::Empty => '?',
                }
                );
        }
    }
    let state: String = state.into_iter().collect();

    EngineEvent { 
        game_id: game_msg.game_id,
        row: game_msg.row,
        column: game_msg.column,
        legal: legal_move,
        finished,
        result: match result {
            MoveResult::Miss => String::from("Miss"),
            MoveResult::Hit(_) => String::from("Hit"),
            MoveResult::Sunk(_) => String::from("Sunk"),
            MoveResult::Illegal => String::from("Miss"),
        },
        new_state: state,
        malformed: None,
    }
}
