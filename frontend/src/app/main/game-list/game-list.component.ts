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

  open(gameId: number) {
    this.openGame.emit(gameId);
  }
}
