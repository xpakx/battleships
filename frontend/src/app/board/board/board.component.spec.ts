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
import { Ship } from 'src/app/main/dto/ship';

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

  it('should add ship to myShips array when clicked twice', () => {
    const row = 3;
    const column = 5;
    const initialShipsLength = component.myShips.length;
    component.gameId = 123;

    component.place(row, column);
    component.place(row, column);

    expect(component.myShips.length).toBe(initialShipsLength + 1);
    const addedShip = component.myShips[initialShipsLength];
    expect(addedShip.headX).toBe(row);
    expect(addedShip.headY).toBe(column);
    expect(addedShip.size).toBe(1);
  });

  it('should add ship to myShips array for horizontal placement', () => {
    const initialShipsLength = component.myShips.length;
    const startRow = 3;
    const startColumn = 5;
    const endColumn = 8;
    component.gameId = 123;

    component.place(startRow, startColumn);
    component.place(startRow, endColumn);

    expect(component.myShips.length).toBe(initialShipsLength + 1);
    const addedShip = component.myShips[initialShipsLength];
    expect(addedShip.headX).toBe(startRow);
    expect(addedShip.headY).toBe(startColumn);
    expect(addedShip.size).toBe(endColumn - startColumn + 1);
    expect(addedShip.orientation).toBe('Horizontal');
  });

  it('should add ship to myShips array for vertical placement', () => {
    const initialShipsLength = component.myShips.length;
    const startRow = 3;
    const endRow = 6;
    const startColumn = 5;
    component.gameId = 123;

    component.place(startRow, startColumn);
    component.place(endRow, startColumn);

    expect(component.myShips.length).toBe(initialShipsLength + 1);
    const addedShip = component.myShips[initialShipsLength];
    expect(addedShip.headX).toBe(startRow);
    expect(addedShip.headY).toBe(startColumn);
    expect(addedShip.size).toBe(endRow - startRow + 1);
    expect(addedShip.orientation).toBe('Vertical');
  });

  it('should not add ship when clicked outside the board', () => {
    const initialShipsLength = component.myShips.length;
    const row = -1;
    const column = 5;

    component.place(row, column);

    expect(component.myShips.length).toBe(initialShipsLength);
  });

  it('should remove ship when clicked again on the same ship', () => {
    const startRow = 3;
    const startColumn = 5;
    component.gameId = 123;
    component.myShips = [{headX: 3, headY: 5, size: 2, orientation: "Horizontal"}];

    component.place(startRow, startColumn);

    expect(component.myShips.length).toBe(0);
  });

  it('should call repaintShips() method after placing a ship', () => {
    spyOn(component, 'repaintShips');
    const startRow = 3;
    const startColumn = 5;
    component.gameId = 123;

    component.place(startRow, startColumn);
    component.place(startRow, startColumn+1);

    expect(component.repaintShips).toHaveBeenCalled();
  });

  it('should send ship placement data when called', () => {
    const gameId = 123;
    const mockShips: Ship[] = [{ headX: 0, headY: 0, size: 2, orientation: 'Horizontal' }];

    component.gameId = gameId;
    component.myShips = mockShips;

    component.sendPlacement();

    expect(websocketServiceSpy.placeShips).toHaveBeenCalledWith(gameId, { ships: mockShips });
  });
});
