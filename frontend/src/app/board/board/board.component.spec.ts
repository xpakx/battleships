import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BoardComponent } from './board.component';
import { WebsocketService } from '../websocket.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Subject, of } from 'rxjs';
import { GameManagementService } from 'src/app/main/game-management.service';
import { BoardMessage } from '../dto/board-message';
import { MoveMessage } from '../dto/move-message';
import { PlacementMessage } from '../dto/placement-message';
import { FieldPipe } from '../field.pipe';

describe('BoardComponent', () => {
  let component: BoardComponent;
  let fixture: ComponentFixture<BoardComponent>;
  let websocketServiceSpy: jasmine.SpyObj<WebsocketService>;
  let gameManagementServiceSpy: jasmine.SpyObj<GameManagementService>;
  let boardSubject: Subject<BoardMessage>;
  let moveSubject: Subject<MoveMessage>;
  let placementSubject: Subject<PlacementMessage>;

  beforeEach(async () => {
    websocketServiceSpy = jasmine.createSpyObj('WebsocketService', ['connect', 'subscribeGame', 'unsubscribe', 'disconnect', 'makeMove', 'placeShips']);
    gameManagementServiceSpy = jasmine.createSpyObj('GameManagementService', ['getGame']);

    boardSubject = new Subject<BoardMessage>();
    moveSubject = new Subject<MoveMessage>();
    placementSubject = new Subject<PlacementMessage>();

    websocketServiceSpy.board$ = boardSubject.asObservable();
    websocketServiceSpy.move$ = moveSubject.asObservable();
    websocketServiceSpy.placement$ = placementSubject.asObservable();

    await TestBed.configureTestingModule({
      declarations: [ BoardComponent, FieldPipe ],
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: WebsocketService, useValue: websocketServiceSpy },
        { provide: GameManagementService, useValue: gameManagementServiceSpy }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set gameId and subscribe to game updates', () => {
    const gameId = 123;
    
    component.gameId = gameId;
    fixture.detectChanges();

    expect(websocketServiceSpy.connect).toHaveBeenCalled();
    expect(websocketServiceSpy.subscribeGame).toHaveBeenCalledWith(gameId);
    expect(component._gameId).toEqual(gameId);
  });
  
  it('should subscribe to websocket messages on ngOnInit', () => {
    expect((component as any).boardSub).toBeTruthy();
    expect((component as any).moveSub).toBeTruthy();
    expect((component as any).placementSub).toBeTruthy();
  });

  it('should send move request to WebSocket service', () => {
    const row = 3;
    const column = 5;
    const expectedMove = { x: row, y: column };
    const gameId = 123;
    component.gameId = gameId;

    component.move(row, column);

    expect(websocketServiceSpy.makeMove).toHaveBeenCalledWith(gameId, expectedMove);
  });
});
