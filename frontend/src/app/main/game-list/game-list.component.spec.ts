import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GameListComponent } from './game-list.component';
import { of } from 'rxjs';
import { GameManagementService } from '../game-management.service';
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