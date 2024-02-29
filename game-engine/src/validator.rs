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


#[cfg(test)]
mod tests {
    use super::*;
    use crate::Pos;

    #[test]
    fn test_detecting_intersecting_ends() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: true};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, tail: Pos {x: 1, y: 2}, size: 2},
            Ship {head: Pos {x: 0, y: 2}, tail: Pos {x: 1, y: 2}, size: 2},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_detecting_intersecting_ships() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: true};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, tail: Pos {x: 1, y: 3}, size: 3},
            Ship {head: Pos {x: 0, y: 2}, tail: Pos {x: 2, y: 2}, size: 3},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_detecting_duplicated_ships() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: true};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, tail: Pos {x: 1, y: 2}, size: 2},
            Ship {head: Pos {x: 1, y: 1}, tail: Pos {x: 1, y: 2}, size: 2},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_accepting_correct_placement() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: true};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, tail: Pos {x: 1, y: 2}, size: 2},
            Ship {head: Pos {x: 2, y: 2}, tail: Pos {x: 2, y: 3}, size: 2},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(result);
    }
}
