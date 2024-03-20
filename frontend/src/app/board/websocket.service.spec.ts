import { TestBed } from '@angular/core/testing';

import { WebsocketService } from './websocket.service';
import { IMessage, RxStomp } from '@stomp/rx-stomp';
import { Subscription, of } from 'rxjs';

describe('WebsocketService', () => {
  let service: WebsocketService;
  let rxStompSpy: jasmine.SpyObj<RxStomp>;


  beforeEach(() => {
    const spyStomp = jasmine.createSpyObj('RxStomp', ['configure', 'activate', 'deactivate', 'publish', 'watch']);

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
});
