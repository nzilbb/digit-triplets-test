import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { DttService }  from '../dtt.service';

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css']
})
export class TestComponent implements OnInit {

    mode: string;
    
    constructor(private route: ActivatedRoute,
                private dttService: DttService) { }
    
    ngOnInit(): void { 
        this.dttService.checkStarted();
        this.getMode();
    }

    getMode(): void {
        this.mode = this.route.snapshot.paramMap.get('mode');
    }
}
