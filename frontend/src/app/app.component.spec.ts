import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { GameManagementService } from './main/game-management.service';

describe('AppComponent', () => {
  let gameServiceSpy: jasmine.SpyObj<GameManagementService>;

  beforeEach(async () => {
    gameServiceSpy = jasmine.createSpyObj('GameManagementService', ['newGame']);
    await TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
      providers: [
        { provide: GameManagementService, useValue: gameServiceSpy }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'battleships'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('Battleships');
  });

  it('should render title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.content span')?.textContent).toContain('battleships app is running!');
  });

  it('should change register card value', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    expect(app.registerCard).toBeFalse();

    app.changeRegisterCard(true);
    expect(app.registerCard).toBeTrue();

    app.changeRegisterCard(false);
    expect(app.registerCard).toBeFalse();
  });

  it('should set gameId when open() is called', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    expect(app.gameId).toBeUndefined();

    const gameId = 123;
    app.open(gameId);

    expect(app.gameId).toEqual(gameId);
  });

  it('should open game modal for AI', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    expect(app.openRequestModal).toBeFalse();

    const aiGame = true;
    app.openGameModal(aiGame);

    expect(app.openRequestModal).toBeTrue();
    expect(app.requestModalForAI).toBeTrue();
  });
});
