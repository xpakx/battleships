use lapin::{Connection, ConnectionProperties, options::{BasicConsumeOptions, QueueBindOptions, QueueDeclareOptions, ExchangeDeclareOptions}, types::FieldTable, ExchangeKind};
mod move_client;
mod placement_client;
mod ai_client;

const EXCHANGE_NAME: &str = "battleships.moves.topic";

const MOVES_QUEUE: &str = "battleships.moves.queue";
const PLACEMENT_QUEUE: &str = "battleships.moves.placement.queue";
const AI_QUEUE: &str = "battleships.moves.ai.queue";
pub const DESTINATION_EXCHANGE: &str = "battleships.engine.topic"; // TODO

pub async fn consumer(rabbit_uri: &str) -> Result<(), lapin::Error> {
    println!("{}", rabbit_uri);
    let conn = Connection::connect(&rabbit_uri, ConnectionProperties::default())
        .await
        .expect("Cannot connect to rabbitmq");

    let channel = conn.create_channel().await?;

    channel.queue_declare(
        MOVES_QUEUE,
        QueueDeclareOptions::default(),
        Default::default(),
        )
        .await
        .expect("Cannot declare queue");

    channel
        .queue_bind(
            MOVES_QUEUE,
            EXCHANGE_NAME,
            "move",
            QueueBindOptions::default(),
            FieldTable::default(),
            )
        .await
        .expect("Cannot bind queue");

    channel.queue_declare(
        PLACEMENT_QUEUE,
        QueueDeclareOptions::default(),
        Default::default(),
        )
        .await
        .expect("Cannot declare queue");

    channel
        .queue_bind(
            PLACEMENT_QUEUE,
            EXCHANGE_NAME,
            "placement",
            QueueBindOptions::default(),
            FieldTable::default(),
            )
        .await
        .expect("Cannot bind queue");

    channel.queue_declare(
        AI_QUEUE,
        QueueDeclareOptions::default(),
        Default::default(),
        )
        .await
        .expect("Cannot declare queue");

    channel
        .queue_bind(
            AI_QUEUE,
            EXCHANGE_NAME,
            "ai",
            QueueBindOptions::default(),
            FieldTable::default(),
            )
        .await
        .expect("Cannot bind queue");

    channel
        .exchange_declare(
            DESTINATION_EXCHANGE,
            ExchangeKind::Topic,
            ExchangeDeclareOptions {
                durable: true,
                ..Default::default()
            },
            FieldTable::default(),
            )
        .await
        .expect("Cannot declare exchange");

    let consumer = channel.basic_consume(
        MOVES_QUEUE,
        "engine_move_consumer",
        BasicConsumeOptions::default(),
        FieldTable::default())
        .await
        .expect("Cannot create consumer");

    let consumer2 = channel.basic_consume(
        PLACEMENT_QUEUE,
        "engine_placement_consumer",
        BasicConsumeOptions::default(),
        FieldTable::default())
        .await
        .expect("Cannot create consumer");

    let consumer3 = channel.basic_consume(
        AI_QUEUE,
        "engine_ai_consumer",
        BasicConsumeOptions::default(),
        FieldTable::default())
        .await
        .expect("Cannot create consumer");

    move_client::set_delegate(consumer, channel.clone());
    placement_client::set_delegate(consumer2, channel.clone());
    ai_client::set_delegate(consumer3, channel.clone());
    println!("Waiting for messages...");

    Ok(())
}
