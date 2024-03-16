import { Component, Input, OnInit } from '@angular/core';
import { BoardMessage } from '../dto/board-message';
import { Subscription } from 'rxjs';
import { MoveMessage } from '../dto/move-message';
import { WebsocketService } from '../websocket.service';
import { Ship } from 'src/app/main/dto/ship';
import { Pair } from '../dto/pair';
import { PlacementMessage } from '../dto/placement-message';
import { GameManagementService } from 'src/app/main/game-management.service';
import { Game } from 'src/app/main/dto/game';

@Component({
  selector: 'app-board',
  templateUrl: './board.component.html',
  styleUrls: ['./board.component.css']
})
export class BoardComponent implements OnInit {
  _gameId?: number;
  myBoard: ("Sunk" | "Hit" | "Miss" | "Empty")[][] = Array(10).fill(Array(10).fill("Empty"));
  opponentBoard: ("Sunk" | "Hit" | "Miss" | "Empty")[][] = Array(10).fill(Array(10).fill("Empty"));
  game?: BoardMessage;
  error: String[][] = Array(10).fill(Array(10).fill(""));
  myShips: Ship[] = [];
  head?: Pair = undefined;
  shipsBoard: ("" | "ship")[][] = Array(10).fill(Array(10).fill(""));

  private moveSub?: Subscription;
  private boardSub?: Subscription;
  private placementSub?: Subscription;

  finished: boolean = false;
  errorMsg: boolean = false;
  msg: String = "";

  shipsPlaced: boolean = false;

  @Input() set gameId(value: number | undefined) {
    this._gameId = value;
    console.log(value);
    this.finished = false;
    if (this._gameId) {
      console.log("calling websocket");
      this.websocket.connect();
      this.websocket.subscribeGame(this._gameId);
    }
  }

  constructor(private websocket: WebsocketService, private gameService: GameManagementService) { }

  ngOnInit(): void {
    this.boardSub = this.websocket.board$
    .subscribe((board: BoardMessage) => this.updateBoard(board));

    this.moveSub = this.websocket.move$
    .subscribe((move: MoveMessage) => this.makeMove(move));

    this.placementSub = this.websocket.placement$
    .subscribe((placement: PlacementMessage) => this.makePlacement(placement));
  }

  ngOnDestroy() {
    this.websocket.unsubscribe();
    this.websocket.disconnect();
    this.boardSub?.unsubscribe();
    this.moveSub?.unsubscribe();
    this.placementSub?.unsubscribe();
  }

  move(row: number, column: number) {
    if (this._gameId == undefined) {
      return;
    }
    this.websocket.makeMove(this._gameId, { x: row, y: column });
    console.log(row, ", ", column)
  }

  makeMove(move: MoveMessage) {
    this.error = [["", "", ""], ["", "", ""], ["", "", ""]]; // FIXME
    if (!move.applied) {
      if (!move.legal) {
        this.error[move.x][move.y] = "illegal";
      }
      return;
    }
    let currentUser = localStorage.getItem("username");
    if  (currentUser == this.game?.username1 || currentUser == this.game?.username2) {
      if (currentUser == move.player) {
        this.opponentBoard[move.x][move.y] = move.result;
      } else {
        this.myBoard[move.x][move.y] = move.result;
      }
    } else if (move.player == this.game?.username1) {
        this.opponentBoard[move.x][move.y] = move.result;
    } else {
        this.myBoard[move.x][move.y] = move.result;
    }

    if (move.finished) {
      this.finished = true;
      console.log(`${move.winner} won!`);
      if (currentUser == move.winner) {
        this.msg = "You won!";
      } else if (currentUser == this.game?.username1 || currentUser == this.game?.username2) {
        this.msg = "You lost!";
      } else {
        this.msg = `${move.winner} won!`;
      }
    }
  }

  place(row: number, column: number) {
    if (this._gameId == undefined) {
      return;
    }
    if (this.game?.gameStarted || this.shipsPlaced) {
      return;
    }

    let clicked = this.getClickedShip(row, column);
    if (clicked) {
      this.myShips = this.myShips.filter((a) => a != clicked);
      this.head = undefined;
      return;
    }

    if (this.head) {
      if (this.head.column != column && this.head.row != row) {
        console.log("bad size")
        return;
      }
      let orientation: "Horizontal" | "Vertical" = "Horizontal";
      let size = Math.abs(this.head.column - column) + 1
      let headX = this.head.row;
      let headY = Math.min(this.head.column, column);
      if (this.head.column == column) {
        orientation = "Vertical";
        size = Math.abs(this.head.row - row) + 1
        headX = Math.min(this.head.row, row);
        headY = this.head.column;
      }

      let ship: Ship = {
        "headX": headX,
        "headY": headY,
        "orientation": orientation,
        "size": size,
      }
      this.myShips.push(ship);
      this.head = undefined;
      console.log(this.myShips)
      this.repaintShips();
    } else {
      this.head = { "row": row, "column": column };
    }
  }

  repaintShips() {
    this.shipsBoard = Array(10).fill(null).map(() => Array(10).fill(""));
    this.myShips.forEach((ship: Ship) => this.repaintSingleShip(ship));
  }

  repaintSingleShip(ship: Ship) {
    if (ship.orientation == "Horizontal") {
      let row = ship.headX;
      for (let i = ship.headY; i < ship.headY + ship.size; i++) {
        this.shipsBoard[row][i] = "ship";
      }
    } else {
      let column = ship.headY;
      for (let i = ship.headX; i < ship.headX + ship.size; i++) {
        this.shipsBoard[i][column] = "ship";
      }
    }
  }

  getClickedShip(row: number, column: number): Ship | undefined {
    return this.myShips.find((ship: Ship) => this.testIfShipIsClicked(ship, row, column));
  }

  testIfShipIsClicked(ship: Ship, row: number, column: number): boolean {
    if (ship.orientation == "Horizontal") {
      let shipRow = ship.headX;
      return (row == shipRow && ship.headY <= column && ship.headY + ship.size - 1 <= column) 
    } else {
      let shipColumn = ship.headY;
      return (column == shipColumn && ship.headX <= row && ship.headX + ship.size - 1 <= row) 
    }
  }

  sendPlacement() {
    if (this._gameId == undefined) {
      return;
    }
    this.websocket.placeShips(this._gameId, { "ships": this.myShips });
    console.log("sent ships")
  }

  makePlacement(placement: PlacementMessage) {
    let currentUser = localStorage.getItem("username");
    if (currentUser != placement.player) {
      return;
    }

    if (placement.legal) {
      this.shipsPlaced = true;
      this.errorMsg = false;
    } else {
      this.myShips = [];
      this.msg = "Ship placement not legal!";
      this.errorMsg = true;
    }
  }

  updateBoard(board: BoardMessage) {
    if (board.gameStarted) {
      this.getShips();
    }

    if (board.error) {
      this.msg = board.error;
      this.errorMsg = true;
      return;
    }

    this.myBoard = board.state1;
    this.game = board;

    let currentUser = localStorage.getItem("username");
    if (currentUser == board.username1) {
      this.myBoard = board.state1;
      this.opponentBoard = board.state2;
    } else if (currentUser == board.username2) {
      this.myBoard = board.state2;
      this.opponentBoard = board.state1;
    } else {
      this.myBoard = board.state1;
      this.opponentBoard = board.state2;
    }
  }

  getShips() {
    // TODO: error handling, maybe block placement till loaded?
    if (this._gameId == undefined) {
      return;
    }
    this.gameService.getGame(this._gameId)
    .subscribe({
      next: (game: Game) => {
        this.myShips = game.myShips;
      }
    });
  }
}