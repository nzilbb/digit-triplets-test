import { Component, OnInit, ViewChild, AfterViewInit, ElementRef, HostListener } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { DttService }  from '../dtt.service';

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css']
})
export class TestComponent implements OnInit {
    // TODO timeout -> next

    mode: string;
    @ViewChild("player") player: ElementRef;
    @ViewChild("btnc") clearButton: ElementRef;
    @ViewChild("btnn") nextButton: ElementRef;
    message = "";
    answer = "";
    numTrials = 100; // TODO
    trial = 0;
    
    constructor(private route: ActivatedRoute,
                private dttService: DttService) { }
    
    ngOnInit(): void { 
        this.dttService.checkStarted();
        this.getMode();
        this.getNumTrials();
    }
    
    ngAfterViewInit(): void {
        this.player.nativeElement.src = this.dttService.mediaUrl(null);
    }

    getMode(): void {
        this.mode = this.route.snapshot.paramMap.get('mode');
    }

    getNumTrials(): void {
        this.numTrials = this.dttService.getNumTrials();
    }

    @HostListener('document:keydown', ['$event'])
    keyDown(event: KeyboardEvent) {
        if (event.keyCode == 8 || event.keyCode == 46) { // backspace/del means clear
            this.press("c");
        } else if (event.keyCode == 13) { // enter means next
            this.press("n");
        } else  if (/[0-9]/.test(event.key)) {
            this.press(event.key);
        }
    }

    // if they close or reload or navigate away, ask if they're sure
    @HostListener('window:beforeunload', ['$event'])
    unloadNotification($event: any) {
        $event.returnValue = true;
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
        this.nextButton.nativeElement.focus();
    }

    next(): void {
        if (this.answer.length >= 3) {
            // set the next media url, given the current
            const answer = this.answer;
            this.answer = "";
            this.player.nativeElement.src = this.dttService.mediaUrl(answer);
            this.trial++;
            // if there's no URL, it's because the dttService has submitted the lasrt answer
            // and the test is finished.
        }
    }
}
