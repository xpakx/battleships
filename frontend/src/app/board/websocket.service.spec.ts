import { TestBed } from '@angular/core/testing';

import { WebsocketService } from './websocket.service';
import { IMessage, RxStomp } from '@stomp/rx-stomp';
import { Subscription, of } from 'rxjs';
import { environment } from 'src/environments/environment';

describe('WebsocketService', () => {
  let service: WebsocketService;
  let rxStompSpy: jasmine.SpyObj<RxStomp>;

  beforeEach(() => {
    const spyStomp = jasmine.createSpyObj('RxStomp', ['configure', 'activate', 'deactivate', 'publish', 'watch', 'connected']);

    TestBed.configureTestingModule({
      providers: [
        WebsocketService,
        { provide: RxStomp, useValue: spyStomp }
      ]
    });
    service = TestBed.inject(WebsocketService);
    rxStompSpy = TestBed.inject(RxStomp) as jasmine.SpyObj<RxStomp>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should disconnect from WebSocket server', () => {
    service.rxStomp = rxStompSpy;
    service.disconnect();
    expect(rxStompSpy.deactivate).toHaveBeenCalled();
  });

  it('makeMove() should publish message to WebSocket server', () => {
    service.rxStomp = rxStompSpy;
    const gameId = 123;
    const move = { x: 10, y: 5};
    service.makeMove(gameId, move);
    expect(rxStompSpy.publish).toHaveBeenCalledWith({ destination: `/app/move/${gameId}`, body: JSON.stringify(move) });
  });

  it('placeShips() should publish message to WebSocket server', () => {
    service.rxStomp = rxStompSpy;
    const gameId = 123;
    const placement = { ships: [] };
    service.placeShips(gameId, placement);
    expect(rxStompSpy.publish).toHaveBeenCalledWith({ destination: `/app/placement/${gameId}`, body: JSON.stringify(placement) });
  });

  it('subscribeMoves() should watch for move messages on WebSocket server', () => {
    service.rxStomp = rxStompSpy;
    const mockMessage: IMessage = { body: '{ "message": "Move message" }', ack: ()=>{}, nack: ()=>{}, command: "", headers: {}, isBinaryBody: false, binaryBody: new Uint8Array() };
    rxStompSpy.watch.and.returnValue(of(mockMessage));
    const gameId = 123;
    service.subscribeMoves(gameId);
    expect(rxStompSpy.watch).toHaveBeenCalledWith(`/topic/game/${gameId}`);
  });

  it('subscribePlacement() should watch for placement messages on WebSocket server', () => {
    service.rxStomp = rxStompSpy;
    const mockMessage: IMessage = { body: '{ "message": "Placement message" }', ack: ()=>{}, nack: ()=>{}, command: "", headers: {}, isBinaryBody: false, binaryBody: new Uint8Array() };
    rxStompSpy.watch.and.returnValue(of(mockMessage));
    const gameId = 123;
    service.subscribePlacement(gameId);
    expect(rxStompSpy.watch).toHaveBeenCalledWith(`/topic/placement/${gameId}`);
  });

  it('subscribeChat() should watch for chat messages on WebSocket server', () => {
    service.rxStomp = rxStompSpy;
    const mockMessage: IMessage = { body: '{ "message": "Chat message" }', ack: ()=>{}, nack: ()=>{}, command: "", headers: {}, isBinaryBody: false, binaryBody: new Uint8Array() };
    rxStompSpy.watch.and.returnValue(of(mockMessage));
    const gameId = 123;
    service.subscribeChat(gameId);
    expect(rxStompSpy.watch).toHaveBeenCalledWith(`/topic/chat/${gameId}`);
  });

  it('subscribeBoard() should watch for board messages on WebSocket server', () => {
    service.rxStomp = rxStompSpy;
    const mockMessage: IMessage = { body: '{ "message": "Board message" }', ack: ()=>{}, nack: ()=>{}, command: "", headers: {}, isBinaryBody: false, binaryBody: new Uint8Array() };
    rxStompSpy.watch.and.returnValue(of(mockMessage));
    const gameId = 123;
    service.subscribeBoard(gameId);
    expect(rxStompSpy.watch).toHaveBeenCalledWith(`/topic/board/${gameId}`);
    expect(rxStompSpy.watch).toHaveBeenCalledWith(`/app/board/${gameId}`);
  });

  it('subscribeGame() should set up subscriptions correctly', () => {
    const gameId = 123;
    spyOn(service, 'subscribeMoves');
    spyOn(service, 'subscribeBoard');
    spyOn(service, 'subscribeChat');
    spyOn(service, 'subscribePlacement');

    service.subscribeGame(gameId);

    expect(service.subscribeMoves).toHaveBeenCalledWith(gameId);
    expect(service.subscribeBoard).toHaveBeenCalledWith(gameId);
    expect(service.subscribeChat).toHaveBeenCalledWith(gameId);
    expect(service.subscribePlacement).toHaveBeenCalledWith(gameId);
  });

  it('unsubscribe() should unsubscribe all queues', () => {
    const unsubscribeSpy = jasmine.createSpyObj<Subscription>('Subscription', ['unsubscribe']);
    (service as any).moveQueue = unsubscribeSpy;
    (service as any).boardQueue = unsubscribeSpy;
    (service as any).chatQueue = unsubscribeSpy;
    (service as any).boardOOB = unsubscribeSpy;
    (service as any).placementQueue = unsubscribeSpy;

    service.unsubscribe();

    expect(unsubscribeSpy.unsubscribe).toHaveBeenCalledTimes(5);
  });

  it('should connect to WebSocket server if not already connected', () => {
    rxStompSpy.connected.and.returnValue(false);
    service.rxStomp = rxStompSpy;
    spyOn(localStorage, 'getItem').and.returnValue('dummyToken');

    service.connect();

    expect(rxStompSpy.configure).toHaveBeenCalled();
    expect(rxStompSpy.activate).toHaveBeenCalled();

    const expectedUrl = `${environment.apiUrl.replace(/^http/, 'ws')}/play/websocket`
    const expectedToken = 'dummyToken';
    const expectedConnectHeaders = { Token: expectedToken };
    expect(rxStompSpy.configure).toHaveBeenCalledWith(jasmine.objectContaining({
      brokerURL: expectedUrl,
      connectHeaders: expectedConnectHeaders
    }));
  });

  it('should not attempt to connect if already connected', () => {
    rxStompSpy.connected.and.returnValue(true);
    service.rxStomp = rxStompSpy;

    service.connect();

    expect(rxStompSpy.configure).not.toHaveBeenCalled();
    expect(rxStompSpy.activate).not.toHaveBeenCalled();
  });
});
