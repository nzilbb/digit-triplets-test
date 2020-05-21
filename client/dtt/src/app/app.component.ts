import { Component, OnInit, HostBinding } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { DttService }  from './dtt.service';
import { Attribute } from './attribute';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {
    title = 'Digit Triplets Hearing Screening Test';
    
    constructor(private dttService: DttService) { }
    
    ngOnInit(): void {
        this.getTitle();
    }
    
    getTitle(): void {
        this.dttService.getAttribute("title").
            subscribe(attribute => this.title = attribute.value);
    }
}
