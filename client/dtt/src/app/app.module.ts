import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { RouteReuseStrategy } from '@angular/router';

import { AppRoutingModule, NeverReuseStrategy } from './app-routing.module';
import { AppComponent } from './app.component';
import { TextComponent } from './text/text.component';
import { SoundCheckComponent } from './sound-check/sound-check.component';
import { FieldComponent } from './field/field.component';
import { TestComponent } from './test/test.component';
import { AutofocusDirective } from './autofocus.directive';
import { WaitComponent } from './wait/wait.component';

@NgModule({
    declarations: [
        AppComponent,
        TextComponent,
        SoundCheckComponent,
        FieldComponent,
        TestComponent,
        AutofocusDirective,
        WaitComponent
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        AppRoutingModule,
        HttpClientModule,
        FormsModule
    ],
    providers: [
        {
            provide: LocationStrategy, useClass: HashLocationStrategy
        },{
            provide: RouteReuseStrategy, useClass: NeverReuseStrategy
        }],
    bootstrap: [AppComponent]
})
export class AppModule { }
