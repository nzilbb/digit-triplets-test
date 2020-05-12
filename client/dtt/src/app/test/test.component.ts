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
    answer = "";
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
        this.player.nativeElement.src = this.dttService.mediaUrl(null);
    }

    getMode(): void {
        this.mode = this.route.snapshot.paramMap.get('mode');
    }

    keyDown(event: any): boolean {
        if (event.keyCode == 8) return true; // backspace gets through
        if (event.keyCode == 13) { // enter means next
            this.next();
            return false;
        }
        if (!/[0-9]/.test(event.key)) return false; // non-digits don't get through
        if (this.answer.length >= 3) return false; // input too long
        return true;
    }

    press(key: string): void {
        console.log(`press ${key}`);
        if (key === "c") {
            this.answer = "";
        } else if (key === "n") {
            this.next();
        } else if (key && this.answer.length < 3) {
            this.answer += key;
        }
        this.input.nativeElement.focus();
    }

    next(): void {
        // set the next media url, given the current
        const answer = this.answer;
        this.answer = "";
        this.player.nativeElement.src = this.dttService.mediaUrl(answer);
        // if there's no URL, it's because the dttService has submitted the lasrt answer
        // and the test is finished.
    }
}