use lapin::{Channel, options::BasicAckOptions, message::DeliveryResult, Consumer};

use serde::{Serialize, Deserialize};
use crate::rabbit::DESTINATION_EXCHANGE;

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
                        println!("Failed to deserialize move message: {:?}", err);
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
    game_state: String,
    game_id: i32,
    column: Option<i32>,
    row: Option<i32>,
    ai: bool,
    current_symbol: String,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct EngineEvent {
    game_id: i32,
    malformed: Option<bool>,
}

impl Default for EngineEvent {
    fn default() -> EngineEvent {
        EngineEvent { 
            game_id: -1,
            malformed: None,
        } 
    } 
}

fn process_placement_event(game_msg: &PlacementMessage) -> EngineEvent {
    Default::default()
}
