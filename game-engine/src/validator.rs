use std::{usize, cmp::min};

use crate::{BoardDefinition, Ship, Orientation, data::{BoardState, Field}};

pub fn check_ship_placement(board: &BoardDefinition, ships: &Vec<Ship>) -> bool {
    let mut ships_placed = vec![vec![false; board.width as usize]; board.height as usize];
    for ship in ships.iter() {
        let orientation = &ship.orientation;
        let len = ship.size.clone();
        let start = match orientation {
            Orientation::Vertical => ship.head.x,
            Orientation::Horizontal => ship.head.y,
        };

        if !board.adjacent_ships_allowed && start > 0 {
            let x = match orientation {
                Orientation::Vertical => start - 1,
                Orientation::Horizontal => ship.head.x,
            };
            let y = match orientation {
                Orientation::Vertical => ship.head.y,
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

pub fn check_ships_are_on_board(board: &BoardDefinition, ships: &Vec<Ship>) -> bool {
    ships.iter().all(|ship| {
        match ship.orientation {
            Orientation::Horizontal => {
                ship.head.y + ship.size - 1 < board.width
            },
            Orientation::Vertical => {
                ship.head.x + ship.size - 1 < board.height
            },
        }
    })
}

pub fn check_all_ships_are_placed(ships: &Vec<Ship>, sizes: Vec<usize>) -> bool {
    let mut ship_sizes: Vec<usize> = ships.iter()
        .map(|ship| ship.size)
        .collect();
    ship_sizes.sort();
    ship_sizes.eq(&sizes)
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

pub fn check_win(board: &BoardState, sizes: Vec<usize>) -> bool {
    let all: usize = sizes.iter()
        .sum();
    let sunk: usize = board.board.iter().map(|row| {
        let row_value: usize = row.iter().map(|f| match f {
            Field::Sunk => 1,
            _ => 0
        }).sum();
        row_value
    }
    ).sum();
    return all == sunk;
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::Pos;

    #[test]
    fn test_detecting_intersecting_ends() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: true};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, size: 2, orientation: Orientation::Horizontal},
            Ship {head: Pos {x: 0, y: 2}, size: 2, orientation: Orientation::Vertical},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_detecting_intersecting_ships() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: true};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, size: 3, orientation: Orientation::Horizontal},
            Ship {head: Pos {x: 0, y: 2}, size: 3, orientation: Orientation::Vertical},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_detecting_duplicated_ships() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: true};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, size: 2, orientation: Orientation::Horizontal},
            Ship {head: Pos {x: 1, y: 1}, size: 2, orientation: Orientation::Horizontal},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_accepting_correct_placement() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: true};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, size: 2, orientation: Orientation::Horizontal},
            Ship {head: Pos {x: 2, y: 2}, size: 2, orientation: Orientation::Horizontal},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(result);
    }

    #[test]
    fn test_detecting_adjacent_ships() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: false};
        let ships = vec![
            Ship {head: Pos {x: 1, y: 1}, size: 2, orientation: Orientation::Horizontal},
            Ship {head: Pos {x: 1, y: 3}, size: 2, orientation: Orientation::Vertical},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_detecting_adjacent_ships_2() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: false};
        let ships = vec![
            Ship {head: Pos {x: 0, y: 0}, size: 2, orientation: Orientation::Horizontal},
            Ship {head: Pos {x: 1, y: 1}, size: 2, orientation: Orientation::Vertical},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_detecting_adjacent_ships_corner() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: false};
        let ships = vec![
            Ship {head: Pos {x: 0, y: 0}, size: 2, orientation: Orientation::Horizontal},
            Ship {head: Pos {x: 1, y: 2}, size: 2, orientation: Orientation::Vertical},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(!result);
    }

    #[test]
    fn test_accepting_correct_placement_without_adjacent_ships() {
        let board = BoardDefinition {width: 4, height: 4, adjacent_ships_allowed: false};
        let ships = vec![
            Ship {head: Pos {x: 0, y: 0}, size: 2, orientation: Orientation::Horizontal},
            Ship {head: Pos {x: 3, y: 0}, size: 2, orientation: Orientation::Horizontal},
        ];
        let result = check_ship_placement(&board, &ships);
        assert!(result);
    }
    
    #[test]
    fn test_check_ships_are_on_board() {
        let board = BoardDefinition { width: 5, height: 5, adjacent_ships_allowed: true };
        let ships = vec![
            Ship { head: Pos { x: 1, y: 1 }, size: 2, orientation: Orientation::Horizontal },
            Ship { head: Pos { x: 0, y: 2 }, size: 2, orientation: Orientation::Vertical },
        ];
        assert!(check_ships_are_on_board(&board, &ships));
    }
        
    #[test]
    fn test_check_ships_partially_outside_are_on_board() {
        let board = BoardDefinition { width: 4, height: 4, adjacent_ships_allowed: true };
        let ships = vec![
            Ship { head: Pos { x: 3, y: 2 }, size: 3, orientation: Orientation::Horizontal },
        ];
        assert!(!check_ships_are_on_board(&board, &ships));
    }
        
    #[test]
    fn test_check_ships_completelly_outside_are_on_board() {
        let board = BoardDefinition { width: 3, height: 3, adjacent_ships_allowed: true };
        let ships = vec![
            Ship { head: Pos { x: 3, y: 1 }, size: 2, orientation: Orientation::Vertical },
        ];
        assert!(!check_ships_are_on_board(&board, &ships));
    }

    #[test]
    fn test_check_all_ships_are_placed() {
        let ships = vec![
            Ship { head: Pos { x: 1, y: 1 }, size: 2, orientation: Orientation::Horizontal },
            Ship { head: Pos { x: 0, y: 2 }, size: 2, orientation: Orientation::Vertical },
        ];
        let sizes = vec![2, 2];
        assert!(check_all_ships_are_placed(&ships, sizes));
    }
        
    #[test]
    fn test_check_all_ships_are_placed_when_they_are_not() {
        let ships = vec![
            Ship { head: Pos { x: 1, y: 1 }, size: 2, orientation: Orientation::Horizontal },
            Ship { head: Pos { x: 0, y: 2 }, size: 2, orientation: Orientation::Vertical },
        ];
        let sizes = vec![2, 3];
        assert!(!check_all_ships_are_placed(&ships, sizes));
    }
        
    #[test]
    fn test_check_all_ships_are_placed_when_sizes_count_is_wrong() {
        let ships = vec![
            Ship { head: Pos { x: 1, y: 1 }, size: 2, orientation: Orientation::Horizontal },
            Ship { head: Pos { x: 0, y: 2 }, size: 3, orientation: Orientation::Vertical },
            Ship { head: Pos { x: 2, y: 2 }, size: 3, orientation: Orientation::Vertical },
        ];
        let sizes = vec![2, 2, 3];
        assert!(!check_all_ships_are_placed(&ships, sizes));
    }
}
