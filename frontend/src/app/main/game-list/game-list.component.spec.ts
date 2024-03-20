import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GameListComponent } from './game-list.component';
import { of } from 'rxjs';
import { GameManagementService } from '../game-management.service';
import { Component } from '@angular/core';
import { Game } from '../dto/game';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('GameListComponent', () => {
  let component: GameListComponent;
  let fixture: ComponentFixture<GameListComponent>;
  let gameService: jasmine.SpyObj<GameManagementService>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GameListComponent ],
      imports: [HttpClientTestingModule],
      providers: [GameManagementService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GameListComponent);
    component = fixture.componentInstance;
    gameService = TestBed.inject(GameManagementService) as jasmine.SpyObj<GameManagementService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call acceptRequest when accept is called', () => {
    const gameId = 1;
    const gameServiceSpy = spyOn(gameService, 'acceptRequest')
    .and
    .returnValue(of(true));
    spyOn(component.openGame, 'emit');

    component.accept(gameId);

    expect(gameServiceSpy).toHaveBeenCalledWith(gameId, { accepted: true });
    expect(component.openGame.emit).toHaveBeenCalledWith(gameId);
  });

  it('should call rejectRequest when reject is called', () => {
    const gameId = 1;
    const gameServiceSpy = spyOn(gameService, 'acceptRequest')
    .and
    .returnValue(of(true));

    component.reject(gameId);

    expect(gameServiceSpy).toHaveBeenCalledWith(gameId, { accepted: false });
  });

  it('should emit openGame event when onAccept is called', () => {
    const gameId = 1;
    spyOn(component.openGame, 'emit');
    component.onAccept(gameId);

    expect(component.openGame.emit).toHaveBeenCalledWith(gameId);
  });

});

@Component({
  selector: 'app-test-host-component',
  template: '<app-game-list [games]="games" [active]="active" [requests]="requests" (openGame)="onOpenGame($event)"></app-game-list>'
})
class TestRequestsHostComponent {
  games: Game[] = [
    { id: 1, username1: 'User1', username2: 'User2', currentState: [], lastMoveRow: 0, lastMoveColumn: 0, type: '', finished: false, won: false, lost: false, drawn: false, userStarts: true, currentSymbol: '', myShips: [] }
  ];
  active = false;
  requests = true;
  onOpenGame(gameId: number) {}
}

describe('GameListComponent (requests)', () => {
  let testHostComponent: TestRequestsHostComponent;
  let fixture: ComponentFixture<TestRequestsHostComponent>;
  let gameService: jasmine.SpyObj<GameManagementService>;

  beforeEach(async () => {
    const gameServiceSpy = jasmine.createSpyObj('GameManagementService', ['acceptRequest', 'rejectRequest']);
    await TestBed.configureTestingModule({
      declarations: [GameListComponent, TestRequestsHostComponent],
      imports: [HttpClientTestingModule],
      providers: [{ provide: GameManagementService, useValue: gameServiceSpy }]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TestRequestsHostComponent);
    testHostComponent = fixture.componentInstance;
    gameService = TestBed.inject(GameManagementService) as jasmine.SpyObj<GameManagementService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testHostComponent).toBeTruthy();
  });

  it('should render buttons based on inputs', () => {
    const buttons = fixture.nativeElement.querySelectorAll('button');
    expect(buttons.length).toEqual(2);
  });

  it('should call acceptRequest when accept button is clicked', () => {
    const gameId = 1;
    const acceptButton = fixture.nativeElement.querySelector('button:nth-child(2)');
    acceptButton.click();

    expect(gameService.acceptRequest).toHaveBeenCalledWith(gameId, { accepted: true });
  });

  it('should call rejectRequest when reject button is clicked', () => {
    const gameId = 1;
    const rejectButton = fixture.nativeElement.querySelector('button:nth-child(3)');
    rejectButton.click();

    expect(gameService.acceptRequest).toHaveBeenCalledWith(gameId, { accepted: false });
  });
});
