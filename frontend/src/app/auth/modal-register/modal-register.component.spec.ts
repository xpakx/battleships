import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalRegisterComponent } from './modal-register.component';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { AuthService } from '../auth.service';

describe('ModalRegisterComponent', () => {
  let component: ModalRegisterComponent;
  let fixture: ComponentFixture<ModalRegisterComponent>;
  let authService: AuthService;
  let formBuilder: FormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ModalRegisterComponent ],
      imports: [ReactiveFormsModule, HttpClientTestingModule],
      providers: [AuthService, FormBuilder]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalRegisterComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    formBuilder = TestBed.inject(FormBuilder);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the register form', () => {
    expect(component.registerForm).toBeInstanceOf(FormGroup);
    expect(component.error).toBeFalsy();
    expect(component.errorMsg).toEqual("");
  });

  it('should emit card event when goToLogin is called', () => {
    spyOn(component.card, 'emit');
    component.goToLogin();
    expect(component.card.emit).toHaveBeenCalledWith(false);
  });

  it('should call authService register method with correct parameters', () => {
    const authServiceSpy = spyOn(authService, 'register')
    .and.returnValue(of({ token: 'token', username: 'username', moderator_role: false }));
    const testUsername = 'user';
    const testPassword = 'password';
    const testPasswordRe = 'password';

    component.registerForm.setValue({ username: testUsername, password: testPassword, passwordRe: testPasswordRe });
    component.register();

    expect(authServiceSpy).toHaveBeenCalled();
    expect(authServiceSpy.calls.mostRecent().args[0].username).toBe(testUsername);
    expect(authServiceSpy.calls.mostRecent().args[0].password).toBe(testPassword);
    expect(authServiceSpy.calls.mostRecent().args[0].passwordRe).toBe(testPasswordRe);
  });

  it('should handle successful registration', () => {
    const mockResponse = { token: 'mockToken', username: 'mockUsername', moderator_role: false };
    spyOn(localStorage, 'setItem');
    component.onRegister(mockResponse);
    expect(component.error).toBeFalsy();
    expect(localStorage.setItem).toHaveBeenCalledWith('token', mockResponse.token.toString());
    expect(localStorage.setItem).toHaveBeenCalledWith('username', mockResponse.username.toString());
  });

  it('should handle registration error', () => {
    const mockError = new HttpErrorResponse({ error: 'Test error', status: 500 });
    spyOn(console, 'log');
    component.onError(mockError);
    expect(component.error).toBeTruthy();
    expect(component.errorMsg).toBe(mockError.message);
  });
});
