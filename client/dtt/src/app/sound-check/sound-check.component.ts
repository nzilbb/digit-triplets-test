import { Component, OnInit, ViewChild, AfterViewInit, ElementRef } from '@angular/core';

import { DttService }  from '../dtt.service';
import { Text } from '../text';
import { slider } from '../router-animations';

@Component({
    selector: 'app-sound-check',
    templateUrl: './sound-check.component.html',
    styleUrls: ['./sound-check.component.css'],
    animations: [ slider ],
})
export class SoundCheckComponent implements OnInit {

    text = {} as Text;
    @ViewChild("player") player: ElementRef;
    playing = false;
    
    constructor(private dttService: DttService) { }
    
    ngOnInit(): void {
        console.log("SoundCheckComponent.ngOnInit()");
        this.dttService.checkStarted();
        this.getText();
    }

    getText(): void {
        this.dttService.getText("sound-check")
            .subscribe(text => this.text = text);
    }

    ngAfterViewInit(): void {
        this.player.nativeElement.src = this.dttService.volumeCheckUrl();
    }

    play(): void {
        this.player.nativeElement.play();
    }

    next(): void {
        this.dttService.nextAfterSoundCheck();
    }
}
