import { Component, Input, OnInit } from '@angular/core';
import { BoardMessage } from '../dto/board-message';
import { Subscription } from 'rxjs';
import { MoveMessage } from '../dto/move-message';
import { WebsocketService } from '../websocket.service';

@Component({
  selector: 'app-board',
  templateUrl: './board.component.html',
  styleUrls: ['./board.component.css']
})
export class BoardComponent implements OnInit {
  _gameId?: number;
  myBoard: ("Sunk" | "Hit" | "Miss" | "Empty")[][] = [
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
  ];
  opponentBoard: ("Sunk" | "Hit" | "Miss" | "Empty")[][] = [
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
    ["Hit", "Empty", "Empty","Empty", "Empty", "Empty" ,"Empty", "Empty", "Empty", "Empty"],
  ];
  game?: BoardMessage;
  error: String[][] = [
    ["", "", "","", "", "" ,"", "", "", ""],
    ["", "", "","", "", "" ,"", "", "", ""],
    ["", "", "","", "", "" ,"", "", "", ""],
    ["", "", "","", "", "" ,"", "", "", ""],
    ["", "", "","", "", "" ,"", "", "", ""],
    ["", "", "","", "", "" ,"", "", "", ""],
    ["", "", "","", "", "" ,"", "", "", ""],
    ["", "", "","", "", "" ,"", "", "", ""],
    ["", "", "","", "", "" ,"", "", "", ""],
    ["", "", "","", "", "" ,"", "", "", ""],
  ];
  private moveSub?: Subscription;
  private boardSub?: Subscription;

  finished: boolean = false;
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
      // TODO: should fetch ships from the game service if game is started

      this.myBoard = board.state1; // TODO: correctly assign both boards
      this.game = board;
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

}