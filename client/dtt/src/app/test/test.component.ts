import { Component, OnInit, ViewChild, AfterViewInit, ElementRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { DttService }  from '../dtt.service';

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css']
})
export class TestComponent implements OnInit {

    mode: string;
    @ViewChild("player") player: ElementRef;
    @ViewChild("input") input: ElementRef;
    message = "";
    value = "";
    numTrials = 100; // TODO
    trial = 0;
    
    constructor(private route: ActivatedRoute,
                private dttService: DttService) { }
    
    ngOnInit(): void { 
        this.dttService.checkStarted();
        this.getMode();
    }
    
    ngAfterViewInit(): void {
        this.input.nativeElement.focus();
    }

    getMode(): void {
        this.mode = this.route.snapshot.paramMap.get('mode');
    }

    keyDown(event: any): boolean {
        if (event.keyCode == 8) return true; // backspace gets through
        if (!/[0-9]/.test(event.key)) return false; // non-digits don't get through
        if (this.value.length >= 3) return false; // input too long
        return true;
    }

    press(key: string): void {
        console.log(`press ${key}`);
        if (key === "c") {
            this.value = "";
        } else if (key === "n") {
            console.log("Next TODO");
        } else if (key && this.value.length < 3) {
            this.value += key;
        }
        this.input.nativeElement.focus();
    }
}
