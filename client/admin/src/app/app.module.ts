import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { APP_BASE_HREF, Location } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { CKEditorModule } from '@ckeditor/ckeditor5-angular';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MenuComponent } from './menu/menu.component';
import { FieldsComponent } from './fields/fields.component';
import { UsersComponent } from './users/users.component';
import { TextsComponent } from './texts/texts.component';
import { MessagesComponent } from './messages/messages.component';
import { OptionsComponent } from './options/options.component';
import { TextComponent } from './text/text.component';

@NgModule({
    declarations: [
        AppComponent,
        MenuComponent,
        FieldsComponent,
        UsersComponent,
        TextsComponent,
        MessagesComponent,
        OptionsComponent,
        TextComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule,
        ReactiveFormsModule,
        AppRoutingModule,
        CKEditorModule,
        RouterModule.forRoot([
            { path: 'fields', component: FieldsComponent },
            { path: 'users', component: UsersComponent },
            { path: 'texts', component: TextsComponent },
            { path: 'texts/:id', component: TextComponent }
        ])
    ],
    providers: [
        {
            provide: APP_BASE_HREF,
            useFactory: getBaseLocation
        },{
            provide: LocationStrategy, useClass: HashLocationStrategy
        }],
    bootstrap: [AppComponent]
})
export class AppModule { }
export function getBaseLocation() {
    return location.pathname;
}
