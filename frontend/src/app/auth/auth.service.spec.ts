import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthRequest } from './dto/auth-request';
import { AuthResponse } from './dto/auth-response';
import { RegisterRequest } from './dto/register-request';
import { environment } from 'src/environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify(); 
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('register', () => {
    it('should send a POST request to register endpoint', () => {
      const mockRequest: RegisterRequest = { username: 'testUser', password: 'testPassword', passwordRe: 'testPassword' };
      const mockResponse: AuthResponse = { token: 'mockToken', username: 'testUser', moderator_role: false };

      service.register(mockRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${environment.apiUrl}/register`);
      expect(req.request.method).toEqual('POST');
      req.flush(mockResponse);
    });
  });

  describe('login', () => {
    it('should send a POST request to login endpoint', () => {
      const mockRequest: AuthRequest = { username: 'testUser', password: 'testPassword' };
      const mockResponse: AuthResponse = { token: 'mockToken', username: 'testUser', moderator_role: false };

      service.login(mockRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${environment.apiUrl}/authenticate`);
      expect(req.request.method).toEqual('POST');
      req.flush(mockResponse);
    });
  });
});
