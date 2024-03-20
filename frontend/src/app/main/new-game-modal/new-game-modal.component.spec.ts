import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewGameModalComponent } from './new-game-modal.component';
import { FormBuilder, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { GameRequest } from '../dto/game-request';

describe('NewGameModalComponent', () => {
  let component: NewGameModalComponent;
  let fixture: ComponentFixture<NewGameModalComponent>;
  let formBuilder: FormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NewGameModalComponent ],
      imports: [ReactiveFormsModule],
      providers: [FormBuilder]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewGameModalComponent);
    component = fixture.componentInstance;
    formBuilder = TestBed.inject(FormBuilder);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the request form', () => {
    expect(component.requestForm).toBeInstanceOf(FormGroup);
  });

  it('should emit new event when form is submitted (AI)', () => {
    spyOn(component.new, 'emit');
    component.ai = true;
    fixture.detectChanges(); 
    const testRequest: GameRequest = {
      aiType: 'Random',
      rules: 'Classic',
      opponent: undefined,
      type: 'AI'
    };

    component.requestForm.setValue({
      username: 'TestUser',
      rules: 'Classic',
      ai_type: 'Random'
    });

    component.finish();

    expect(component.new.emit).toHaveBeenCalledWith(testRequest);
  });

  it('should emit new event when form is submitted (not AI)', () => {
    spyOn(component.new, 'emit');
    component.ai = false;
    fixture.detectChanges(); 
    const testRequest: GameRequest = {
      aiType: undefined,
      rules: 'Classic',
      opponent: 'TestUser',
      type: 'USER'
    };

    component.requestForm.setValue({
      username: 'TestUser',
      rules: 'Classic',
      ai_type: 'Random'
    });

    component.finish();

    expect(component.new.emit).toHaveBeenCalledWith(testRequest);
  });
});
