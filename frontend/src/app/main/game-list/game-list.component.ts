import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Game } from '../dto/game';
import { GameManagementService } from '../game-management.service';

@Component({
  selector: 'app-game-list',
  templateUrl: './game-list.component.html',
  styleUrls: ['./game-list.component.css']
})
export class GameListComponent implements OnInit {
  @Input() games: Game[] = [];
  @Input() active: boolean = true;
  @Input() requests: boolean = false;

  @Output() openGame: EventEmitter<number> = new EventEmitter<number>();

  constructor(private gameService: GameManagementService) { }

  ngOnInit(): void {
  }

  accept(gameId: number) {
    this.gameService.acceptRequest(gameId, {accepted: true})
      .subscribe({
        next: (value: Boolean) => this.onAccept(gameId),
        error: (err: HttpErrorResponse) => this.onError(err)
      });
  }

  onAccept(gameId: number) {
    this.open(gameId);
  }

  reject(gameId: number) {
    this.gameService.acceptRequest(gameId, {accepted: false})
      .subscribe({
        next: (value: Boolean) => this.onReject(gameId),
        error: (err: HttpErrorResponse) => this.onError(err)
      });

  }

  onReject(gameId: number) {
    this.games = this.games.filter((game) => game.id != gameId);
  }

  onError(err: HttpErrorResponse) {
    // todo
  }

  open(gameId: number) {
    this.openGame.emit(gameId);
  }
}
