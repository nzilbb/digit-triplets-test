import { Component, HostBinding } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import {  slideInAnimation } from './animations';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    animations: [ slideInAnimation ]
})
export class AppComponent {
    title = 'Digit Triplets Hearing Screening Test';
    prepareRoute(outlet: RouterOutlet) {
        try {
            return outlet && outlet.activatedRoute.snapshot.url[0].toString();
        } catch (x) {
            return null;
        }
    }
}
