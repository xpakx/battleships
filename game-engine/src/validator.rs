use std::{usize, cmp::min};

use crate::{BoardDefinition, Ship};

enum Orientation {
    Horizontal,
    Vertical,
}

pub fn check_ship_placement(board: &BoardDefinition, ships: &Vec<Ship>) -> bool {
    let mut ships_placed = vec![vec![false; board.width as usize]; board.height as usize];
    for ship in ships.iter() {
        let orientation = match &ship {
            ship if ship.tail.x == ship.head.x => Orientation::Horizontal,
            ship if ship.tail.y == ship.head.y => Orientation::Vertical,
            _ => panic!("shouldn't happen!")
        };

        let len = match orientation {
            Orientation::Vertical => (ship.tail.x - ship.head.x + 1).abs() as usize,
            Orientation::Horizontal => (ship.tail.y - ship.head.y + 1).abs() as usize,
        };
        let start = match orientation {
            Orientation::Vertical => min(ship.head.x, ship.tail.x) as usize,
            Orientation::Horizontal => min(ship.head.y, ship.tail.y) as usize,
        };

        for i in 0..len {
            let x = match orientation {
                Orientation::Vertical => start + i,
                Orientation::Horizontal => ship.head.x as usize,
            };
            let y = match orientation {
                Orientation::Vertical => ship.head.y as usize,
                Orientation::Horizontal => start + i,
            };
            if ships_placed[x][y] {
                return false
            }
            ships_placed[x][y] = true;
        }
    }

    true
}
