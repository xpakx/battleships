import { TestBed } from '@angular/core/testing';

import { GameManagementService } from './game-management.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { GameRequest } from './dto/game-request';
import { GameResponse } from './dto/game-response';
import { environment } from 'src/environments/environment';
import { Game } from './dto/game';
import { AcceptRequest } from './dto/accept-request';

describe('GameManagementService', () => {
  let service: GameManagementService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [GameManagementService]
    });
    service = TestBed.inject(GameManagementService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('new game', () => {
    it('should send a POST request to new game endpoint', () => {
      const mockRequest: GameRequest = {type: "AI", rules: "Classic", aiType: "Random"};
      const mockResponse: GameResponse = { id: 1 };
      spyOn(localStorage, 'getItem').and.returnValue('mockToken');

      service.newGame(mockRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${environment.apiUrl}/game`);
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(mockRequest);
      expect(req.request.headers.has('Authorization')).toBeTruthy();
      expect(req.request.headers.get('Authorization')).toEqual('Bearer mockToken');

      req.flush(mockResponse);
    });
  });

  describe('get games', () => {
    it('should send a GET request to active games endpoint', () => {
      const mockResponse: Game[] = [{ id: 1, username1: 'User1', username2: 'User2', currentState: [], lastMoveRow: 0, lastMoveColumn: 0, type: '', finished: false, won: false, lost: false, drawn: false, userStarts: true, myShips: [] }];
      spyOn(localStorage, 'getItem').and.returnValue('mockToken');

      service.getActiveGames().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${environment.apiUrl}/game`);
      expect(req.request.method).toEqual('GET');
      expect(req.request.headers.has('Authorization')).toBeTruthy();
      expect(req.request.headers.get('Authorization')).toEqual('Bearer mockToken');

      req.flush(mockResponse);
    });

    it('should send a GET request to requests endpoint', () => {
      const mockResponse: Game[] = [{ id: 1, username1: 'User1', username2: 'User2', currentState: [], lastMoveRow: 0, lastMoveColumn: 0, type: '', finished: false, won: false, lost: false, drawn: false, userStarts: true, myShips: [] }];
      spyOn(localStorage, 'getItem').and.returnValue('mockToken');

      service.getGameRequests().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${environment.apiUrl}/game/request`);
      expect(req.request.method).toEqual('GET');
      expect(req.request.headers.has('Authorization')).toBeTruthy();
      expect(req.request.headers.get('Authorization')).toEqual('Bearer mockToken');

      req.flush(mockResponse);
    });

    it('should send a GET request to archive endpoint', () => {
      const mockResponse: Game[] = [{ id: 1, username1: 'User1', username2: 'User2', currentState: [], lastMoveRow: 0, lastMoveColumn: 0, type: '', finished: false, won: false, lost: false, drawn: false, userStarts: true, myShips: [] }];
      spyOn(localStorage, 'getItem').and.returnValue('mockToken');

      service.getFinishedGames().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${environment.apiUrl}/game/archive`);
      expect(req.request.method).toEqual('GET');
      expect(req.request.headers.has('Authorization')).toBeTruthy();
      expect(req.request.headers.get('Authorization')).toEqual('Bearer mockToken');

      req.flush(mockResponse);
    });
  });

  describe('accept request', () => {
    it('should send a POST request to accept request endpoint', () => {
      const mockRequest: AcceptRequest = { accepted: true };
      const mockResponse: Boolean = true;
      spyOn(localStorage, 'getItem').and.returnValue('mockToken');

      service.acceptRequest(7, mockRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${environment.apiUrl}/game/7/request`);
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(mockRequest);
      expect(req.request.headers.has('Authorization')).toBeTruthy();
      expect(req.request.headers.get('Authorization')).toEqual('Bearer mockToken');

      req.flush(mockResponse);
    });
  });

  describe('get game', () => {
    it('should send a GET request to get game endpoint', () => {
      const mockResponse: Game = { id: 1, username1: 'User1', username2: 'User2', currentState: [], lastMoveRow: 0, lastMoveColumn: 0, type: '', finished: false, won: false, lost: false, drawn: false, userStarts: true, myShips: [] };
      spyOn(localStorage, 'getItem').and.returnValue('mockToken');

      service.getGame(7).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${environment.apiUrl}/game/7`);
      expect(req.request.method).toEqual('GET');
      expect(req.request.headers.has('Authorization')).toBeTruthy();
      expect(req.request.headers.get('Authorization')).toEqual('Bearer mockToken');

      req.flush(mockResponse);
    });
  });
});