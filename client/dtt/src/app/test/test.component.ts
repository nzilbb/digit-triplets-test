import { Component, OnInit, ViewChild, AfterViewInit, ElementRef, HostListener } from '@angular/core';
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
    @ViewChild("btnc") clearButton: ElementRef;
    @ViewChild("btnn") nextButton: ElementRef;
    message = "";
    answer = "";
    helpText = "";
    numTrials = 100;
    trial = 0;
    playing = false;
    timeout: any;
    timeoutSeconds = 10;
    wait = true;
    
    constructor(private route: ActivatedRoute,
                private dttService: DttService) { }
    
    ngOnInit(): void { 
        this.dttService.checkStarted();
        this.getTimeout();
        this.getMode();
        this.getNumTrials();
        this.getHelpText();
    }
    
    ngAfterViewInit(): void {
        this.dttService.getMedia(null).subscribe(audioData => {
            this.player.nativeElement.src = window.URL.createObjectURL(audioData);
        });
    }

    startTimeout() {
        if (this.timeoutSeconds) {
            this.cancelTimeout();
            this.timeout = window.setTimeout(
                () => {
                    while (this.answer.length < 3) this.answer += " ";
                    this.next();
                }, this.timeoutSeconds * 1000);
        }
    }
    cancelTimeout() {
        if (this.timeout) window.clearTimeout(this.timeout);
    }

    getTimeout(): void {
        this.dttService.getAttribute("timeoutseconds").
            subscribe(attribute => this.timeoutSeconds = parseInt(attribute.value));
    }
    getMode(): void {
        this.mode = this.route.snapshot.paramMap.get('mode');
    }
    getNumTrials(): void {
        this.numTrials = this.dttService.getNumTrials();
    }
    getHelpText(): void {
        this.dttService.getText("dtt-help")
            .subscribe((text) => {
                this.helpText = text.html;
            });
    }

    @HostListener('document:keydown', ['$event'])
    keyDown(event: KeyboardEvent) {
        if (event.keyCode == 8 || event.keyCode == 46) { // backspace/del means clear
            this.clearButton.nativeElement.click();
        } else if (event.keyCode == 13) { // enter means next
            this.nextButton.nativeElement.click();
        } else  if (/[0-9]/.test(event.key)) {
            this.press(event.key);
        }
    }

    // if they close or reload or navigate away, ask if they're sure
    @HostListener('window:beforeunload', ['$event'])
    unloadNotification($event: any) {
        $event.returnValue = "The test is not finished yet"; // TODO i18n
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
    }

    next(): void {
        if (this.answer.length >= 3) {
            this.wait = true;
            this.cancelTimeout();
            // set the next media url, given the current
            const answer = this.answer;
            this.answer = "";
            //this.player.nativeElement.src = this.dttService.mediaUrl(answer);
            this.trial++;
            this.dttService.getMedia(answer).subscribe(audioData => {
                this.player.nativeElement.src = window.URL.createObjectURL(audioData);
            });
            // after the last trial, the URL is silence, and the dtt.service handles moving
            // to the next step
        }
    }

    onAudioError(event: any): void {
        console.log("onAudioError: " + JSON.stringify(event));
    }
}
