import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuComponent } from './menu.component';
import { GameManagementService } from '../game-management.service';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { By } from '@angular/platform-browser';
import { Game } from '../dto/game';

function getGame(id: number): Game {
  return { id: 1, currentState: [], lastMoveColumn: 0, lastMoveRow: 0, finished: false, won: false, drawn: false, lost: false, type: "AI", username1: "user1", username2: "user2", userStarts: false, myShips: [] }
}

describe('MenuComponent', () => {
  let component: MenuComponent;
  let fixture: ComponentFixture<MenuComponent>;
  let gameServiceSpy: jasmine.SpyObj<GameManagementService>;

  beforeEach(async () => {
    gameServiceSpy = jasmine.createSpyObj('GameManagementService', ['getGameRequests', 'getActiveGames', 'getFinishedGames']);
    await TestBed.configureTestingModule({
      declarations: [ MenuComponent ],
      providers: [{ provide: GameManagementService, useValue: gameServiceSpy }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch game requests', () => {
    const mockGames: Game[] = [ getGame(1), getGame(2)];
    gameServiceSpy.getGameRequests.and.returnValue(of(mockGames));

    component.getRequests();

    expect(gameServiceSpy.getGameRequests).toHaveBeenCalled();
    expect(component.games).toEqual(mockGames);
    expect(component.requestView).toBeTrue();
    expect(component.activeView).toBeFalse();
  });

  it('should fetch active games', () => {
    const mockGames: Game[] = [ getGame(1), getGame(2)];
    gameServiceSpy.getActiveGames.and.returnValue(of(mockGames));

    component.getGames();

    expect(gameServiceSpy.getActiveGames).toHaveBeenCalled();
    expect(component.games).toEqual(mockGames);
    expect(component.activeView).toBeTrue();
    expect(component.requestView).toBeFalse();
  });

  it('should fetch archived games', () => {
    const mockGames: Game[] = [ getGame(1), getGame(2)];
    gameServiceSpy.getFinishedGames.and.returnValue(of(mockGames));

    component.getArchive();

    expect(gameServiceSpy.getFinishedGames).toHaveBeenCalled();
    expect(component.games).toEqual(mockGames);
    expect(component.activeView).toBeFalse();
    expect(component.requestView).toBeFalse();
  });

  it('should handle error when fetching game requests', () => {
    const errorMessage = 'Error fetching game requests';
    const errorResponse = new HttpErrorResponse({ error: errorMessage });
    gameServiceSpy.getGameRequests.and.returnValue(throwError(() => errorResponse));

    component.getRequests();

    expect(gameServiceSpy.getGameRequests).toHaveBeenCalled();
    expect(component.error).toBeTrue();
  });

  it('should emit openGameModal with false when newGame is called', () => {
    spyOn(component.openGameModal, 'emit');
    
    component.newGame();

    expect(component.openGameModal.emit).toHaveBeenCalledWith(false);
  });

  it('should emit openGameModal with true when newAIGame is called', () => {
    spyOn(component.openGameModal, 'emit');
    
    component.newAIGame();

    expect(component.openGameModal.emit).toHaveBeenCalledWith(true);
  });

  it('should emit openGame with gameId when open is called', () => {
    spyOn(component.openGame, 'emit');
    const gameId = 123;
    
    component.open(gameId);

    expect(component.openGame.emit).toHaveBeenCalledWith(gameId);
  });

  it('should render buttons for various actions', () => {
    const buttons = fixture.debugElement.queryAll(By.css('button'));
    expect(buttons.length).toBe(5);
  });
});
