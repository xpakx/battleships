use std::{usize, cmp::{min, max}};

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

        if !board.adjacent_ships_allowed && start > 0 {
            let x = match orientation {
                Orientation::Vertical => start - 1,
                Orientation::Horizontal => ship.head.x as usize,
            };
            let y = match orientation {
                Orientation::Vertical => ship.head.y as usize,
                Orientation::Horizontal => start - 1,
            };
            ships_placed[x][y] = true;
            let (x_1, y_1) = get_prev_coord(&orientation, x, y);
            ships_placed[x_1][y_1] = true;
            let (x_2, y_2) = get_next_coord(board, &orientation, x, y);
            ships_placed[x_2][y_2] = true;
        }


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

            if !board.adjacent_ships_allowed {
                let (x_1, y_1) = get_prev_coord(&orientation, x, y);
                ships_placed[x_1][y_1] = true;
                let (x_2, y_2) = get_next_coord(board, &orientation, x, y);
                ships_placed[x_2][y_2] = true;
            }
        }


        if !board.adjacent_ships_allowed {
            let test = match orientation {
                Orientation::Vertical => start + len < (board.height) as usize,
                Orientation::Horizontal => start + len < (board.width) as usize,
            };
            if test {
                let x = match orientation {
                    Orientation::Vertical => start + len,
                    Orientation::Horizontal => ship.head.x as usize,
                };
                let y = match orientation {
                    Orientation::Vertical => ship.head.y as usize,
                    Orientation::Horizontal => start + len,
                };
                ships_placed[x][y] = true;
                let (x_1, y_1) = get_prev_coord(&orientation, x, y);
                ships_placed[x_1][y_1] = true;
                let (x_2, y_2) = get_next_coord(board, &orientation, x, y);
                ships_placed[x_2][y_2] = true;
            }
        }
    }

    true
}

fn get_prev_coord(orientation: &Orientation, x: usize, y: usize) -> (usize, usize) {
    let x_1 = match orientation {
        Orientation::Vertical => x,
        Orientation::Horizontal => {
            if x > 0 {
                x-1 
            } else { 
                0
            }
        }
    };
    let y_1 = match orientation {
        Orientation::Vertical => {
            if y > 0 {
                y-1
            } else {
                0
            }
        },
        Orientation::Horizontal => y,
    };
    (x_1, y_1)
}

fn get_next_coord(board: &BoardDefinition, orientation: &Orientation, x: usize, y: usize) -> (usize, usize) {
    let x_2 = match orientation {
        Orientation::Vertical => x,
        Orientation::Horizontal => min(board.width as usize - 1, x+1),
    };
    let y_2 = match orientation {
        Orientation::Vertical => min(board.height as usize - 1, y+1),
        Orientation::Horizontal => y,
    };
    (x_2, y_2)
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

    #[test]
    fn test_detecting_adjacent_ships() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: false};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, tail: Pos {x: 1, y: 2}, size: 2},
            Ship {head: Pos {x: 1, y: 3}, tail: Pos {x: 2, y: 3}, size: 2},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_detecting_adjacent_ships_2() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: false};
        let ships = vec![
            Ship {head: Pos {x: 0, y: 0}, tail: Pos {x: 0, y: 1}, size: 2},
            Ship {head: Pos {x: 1, y: 1}, tail: Pos {x: 2, y: 1}, size: 2},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_detecting_adjacent_ships_corner() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: false};
        let ships = vec![
            Ship {head: Pos {x: 0, y: 0}, tail: Pos {x: 0, y: 1}, size: 2},
            Ship {head: Pos {x: 1, y: 2}, tail: Pos {x: 2, y: 2}, size: 2},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_accepting_correct_placement_without_adjacent_ships() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: false};
        let ships = vec![
            Ship {head: Pos {x: 0, y: 0}, tail: Pos {x: 0, y: 1}, size: 2},
            Ship {head: Pos {x: 3, y: 0}, tail: Pos {x: 3, y: 1}, size: 2},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(result);
    }
}
