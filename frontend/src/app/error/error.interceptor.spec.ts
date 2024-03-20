import { TestBed } from '@angular/core/testing';

import { ErrorInterceptor } from './error.interceptor';
import { HTTP_INTERCEPTORS, HttpClient, HttpErrorResponse } from '@angular/common/http';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';

describe('ErrorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should remove token and username from localStorage on 401 error', () => {
    const mockErrorResponse = new HttpErrorResponse({
      status: 401,
      statusText: 'Unauthorized'
    });

    spyOn(localStorage, 'removeItem');

    http.get('/api/some-endpoint').subscribe({
      next: () => { },
      error: (error: HttpErrorResponse) => {
        expect(error.status).toEqual(401);
        expect(localStorage.removeItem).toHaveBeenCalledWith('token');
        expect(localStorage.removeItem).toHaveBeenCalledWith('username');
      }
    }
    );

    const req = httpMock.expectOne('/api/some-endpoint');
    req.flush("", mockErrorResponse);
  });

  it('should not modify localStorage on other errors', () => {
    const mockErrorResponse = new HttpErrorResponse({
      status: 404,
      statusText: 'Not Found'
    });

    spyOn(localStorage, 'removeItem');

    http.get('/api/some-endpoint').subscribe(
      () => { },
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404);
        expect(localStorage.removeItem).not.toHaveBeenCalled();
      }
    );

    const req = httpMock.expectOne('/api/some-endpoint');
    req.flush("", mockErrorResponse);
  });
});
