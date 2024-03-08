import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor() {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError(
        (error: HttpErrorResponse) => {
          this.testAuthorization(error);
          return throwError(() => error);
        }
      )
    );
  }

  private testAuthorization(error: HttpErrorResponse): void {
    if (error.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("username");
    }
  }

}
