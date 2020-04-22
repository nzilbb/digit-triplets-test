import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MenuComponent } from './menu/menu.component';
import { FieldsComponent } from './fields/fields.component';
import { UsersComponent } from './users/users.component';
import { TextsComponent } from './texts/texts.component';

@NgModule({
  declarations: [
    AppComponent,
    MenuComponent,
    FieldsComponent,
    UsersComponent,
    TextsComponent
  ],
  imports: [
      BrowserModule,
      AppRoutingModule,
      RouterModule.forRoot([
          { path: '', component: FieldsComponent },
          { path: 'users', component: UsersComponent },
          { path: 'texts', component: TextsComponent },
      ])
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
