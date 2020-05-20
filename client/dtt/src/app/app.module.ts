import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { APP_BASE_HREF, Location } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
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
            provide: APP_BASE_HREF,
            useFactory: getBaseLocation
        }],
    bootstrap: [AppComponent]
})
export class AppModule { }
export function getBaseLocation() {
    return location.pathname;
}
