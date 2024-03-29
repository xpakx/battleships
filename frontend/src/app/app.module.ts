import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { ModalLoginComponent } from './auth/modal-login/modal-login.component';
import { ModalRegisterComponent } from './auth/modal-register/modal-register.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from  '@angular/common/http';
import { ErrorInterceptor } from './error/error.interceptor';
import { GameListComponent } from './main/game-list/game-list.component';
import { MenuComponent } from './main/menu/menu.component';
import { NewGameModalComponent } from './main/new-game-modal/new-game-modal.component';
import { BoardComponent } from './board/board/board.component';
import { FieldPipe } from './board/field.pipe';

@NgModule({
  declarations: [
    AppComponent,
    ModalLoginComponent,
    ModalRegisterComponent,
    GameListComponent,
    MenuComponent,
    NewGameModalComponent,
    BoardComponent,
    FieldPipe
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    }

  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
