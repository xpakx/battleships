import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ModalLoginComponent } from './modal-login.component';
import { FormBuilder, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { of } from 'rxjs';
import { AuthService } from '../auth.service';
import { HttpErrorResponse } from '@angular/common/http';

describe('ModalLoginComponent', () => {
  let component: ModalLoginComponent;
  let fixture: ComponentFixture<ModalLoginComponent>;
  let authService: AuthService;
  let formBuilder: FormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ModalLoginComponent ],
      imports: [ReactiveFormsModule, HttpClientTestingModule],
      providers: [AuthService, FormBuilder]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalLoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    formBuilder = TestBed.inject(FormBuilder);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should initialize the login form', () => {
    expect(component.loginForm).toBeInstanceOf(FormGroup);
    expect(component.error).toBeFalsy();
    expect(component.errorMsg).toEqual("");
  });

  it('should emit card event when goToRegistration is called', () => {
    spyOn(component.card, 'emit');
    component.goToRegistration();
    expect(component.card.emit).toHaveBeenCalledWith(true);
  });

  it('should call authService login method with correct parameters', () => {
    const authServiceSpy = spyOn(authService, 'login')
    .and
    .returnValue(of({ token: 'token', username: 'username', moderator_role: false }));
    const testUsername = 'username';
    const testPassword = 'password';

    component.loginForm.setValue({ username: testUsername, password: testPassword });
    component.login();

    expect(authServiceSpy).toHaveBeenCalled();
    expect(authServiceSpy.calls.mostRecent().args[0].username).toBe(testUsername);
    expect(authServiceSpy.calls.mostRecent().args[0].password).toBe(testPassword);
  });

  it('should handle successful login', () => {
    const mockResponse = { token: 'token', username: 'username', moderator_role: false };
    spyOn(localStorage, 'setItem');
    component.onLogin(mockResponse);
    expect(component.error).toBeFalsy();
    expect(localStorage.setItem).toHaveBeenCalledWith('token', mockResponse.token.toString());
    expect(localStorage.setItem).toHaveBeenCalledWith('username', mockResponse.username.toString());
  });

  it('should handle login error', () => {
    const mockError = new HttpErrorResponse({ error: 'Test error', status: 500 });
    component.onError(mockError);
    expect(component.error).toBeTruthy();
    expect(component.errorMsg).toBe(mockError.message);
  });
});
