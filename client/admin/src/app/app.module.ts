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
import { InstancesComponent } from './instances/instances.component';
import { TrialsComponent } from './trials/trials.component';
import { TrialSetsComponent } from './trial-sets/trial-sets.component';
import { AttributesComponent } from './attributes/attributes.component';
import { WaitComponent } from './wait/wait.component';

@NgModule({
    declarations: [
        AppComponent,
        MenuComponent,
        FieldsComponent,
        UsersComponent,
        TextsComponent,
        MessagesComponent,
        OptionsComponent,
        TextComponent,
        InstancesComponent,
        TrialsComponent,
        TrialSetsComponent,
        AttributesComponent,
        WaitComponent
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
            { path: 'texts/:id', component: TextComponent },
            { path: 'attributes', component: AttributesComponent },
            { path: 'instances', component: InstancesComponent },
            { path: 'trials/:instanceId', component: TrialsComponent },
            { path: 'trialsets', component: TrialSetsComponent },
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
