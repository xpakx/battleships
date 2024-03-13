import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Game } from '../dto/game';
import { GameResponse } from '../dto/game-response';
import { GameManagementService } from '../game-management.service';
import { GameRequest } from '../dto/game-request';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {
  games: Game[] = [];
  requestView: boolean = false;
  activeView: boolean = false;
  error: boolean = false;
  errorMsg: String = "";
  openRequestModal: boolean = false;
  requestModalForAI: boolean = false;

  @Output() openGame: EventEmitter<number> = new EventEmitter<number>();

  constructor(private gameService: GameManagementService) { }

  ngOnInit(): void {
  }

  getRequests() {
    this.gameService.getGameRequests()
      .subscribe({
        next: (games: Game[]) => this.onRequests(games),
        error: (err: HttpErrorResponse) => this.onError(err)
      });
  }

  getGames() {
    this.gameService.getActiveGames()
      .subscribe({
        next: (games: Game[]) => this.onGames(games),
        error: (err: HttpErrorResponse) => this.onError(err)
      });
  }

  getArchive() {
    this.gameService.getFinishedGames()
      .subscribe({
        next: (games: Game[]) => this.onArchive(games),
        error: (err: HttpErrorResponse) => this.onError(err)
      });

  }

  onRequests(games: Game[]) {
    this.games = games;
    this.activeView = false;
    this.requestView = true;
  }

  onArchive(games: Game[]) {
    this.games = games;
    this.activeView = false;
    this.requestView = false;
  }

  onGames(games: Game[]) {
    this.games = games;
    this.activeView = true;
    this.requestView = false;
  }

  onError(err: HttpErrorResponse) {
    console.log(err);
    this.error = true;
    this.errorMsg = err.message;
  }
  
  newGame() {
    this.openRequestModal = true;
    this.requestModalForAI = false;
  }

  newAIGame() {
    this.openRequestModal = true;
    this.requestModalForAI = true;
  }

  open(gameId: number) {
    this.openGame.emit(gameId);
  }

  closeRequestModal(request: GameRequest) {
    this.openRequestModal = false;
    this.gameService.newGame(request)
      .subscribe({
        next: (game: GameResponse) => this.onRequestSent(game, request),
        error: (err: HttpErrorResponse) => this.onError(err)
      });

  }

  onRequestSent(game: GameResponse, request: GameRequest) {
    if (request.type == "AI") {
      this.open(game.id);
    } else {
      // todo
      console.log(`${request.opponent} invited to game`)
    }
  }
}
