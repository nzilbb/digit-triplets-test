import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { APP_BASE_HREF, Location } from '@angular/common';

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
    let paths: string[] = location.pathname.split('/').splice(1, 1);
    let basePath: string = (paths && paths[0]) || ''; // Default: my-account
    return '/' + basePath;
}
