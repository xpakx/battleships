import { Component, Input, OnInit } from '@angular/core';
import { BoardMessage } from '../dto/board-message';
import { Subscription } from 'rxjs';
import { MoveMessage } from '../dto/move-message';
import { WebsocketService } from '../websocket.service';
import { Ship } from 'src/app/main/dto/ship';
import { Pair } from '../dto/pair';

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

  finished: boolean = false;
  errorMsg: boolean = false;
  msg: String = "";

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

  constructor(private websocket: WebsocketService) { }

  ngOnInit(): void {
    this.boardSub = this.websocket.board$.subscribe((board: BoardMessage) => {
      if (board.gameStarted) {
        // TODO: should fetch ships from the game service
      }

      if (board.error) {
        this.msg = board.error;
        this.errorMsg = true;
      } else {
        this.myBoard = board.state1;
        this.game = board;

        let currentUser = localStorage.getItem("username");
        if (currentUser == board.username1 || currentUser == board.username2) {
          if (currentUser == board.username1) {
            this.myBoard = board.state1;
            this.opponentBoard = board.state2;
          } else {
            this.myBoard = board.state2;
            this.opponentBoard = board.state1;
          }
        } else {
          this.myBoard = board.state1;
          this.opponentBoard = board.state2;
        }
      }
      console.log(board);
    });

    this.moveSub = this.websocket.move$.subscribe((move: MoveMessage) => {
      this.makeMove(move);
      console.log(move);
    });
  }

  ngOnDestroy() {
    this.websocket.unsubscribe();
    this.websocket.disconnect();
    this.boardSub?.unsubscribe();
    this.moveSub?.unsubscribe();
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
      if (move.finished) {
        console.log(`${move.winner} won!`);
        if(currentUser == move.winner) {
          this.msg = "You won!";
        } else if (currentUser == this.game?.username1 || currentUser == this.game?.username2) {
          this.msg = "You lost!";
        } else {
          this.msg = `${move.winner} won!`;
        }
      } 
    }
  }

  place(row: number, column: number) {
    if (this._gameId == undefined) {
      return;
    }
    if (this.game?.gameStarted) {
      return;
    }

    // TODO: delete ship if field is occupied

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
    this.myShips.forEach((ship: Ship) => {
      if (ship.orientation == "Horizontal") {
        let row = ship.headX;
        for (let i = ship.headY; i < ship.headY+ship.size; i++) {
          this.shipsBoard[row][i] = "ship";
        }
      } else {
        let column = ship.headY;
        for (let i = ship.headX; i < ship.headX+ship.size; i++) {
          this.shipsBoard[i][column] = "ship";
        }
      }
    });
  }

  sendPlacement() {
    if (this._gameId == undefined) {
      return;
    }
    this.websocket.placeShips(this._gameId, {"ships": this.myShips});
    console.log("sent ships")
  }
}