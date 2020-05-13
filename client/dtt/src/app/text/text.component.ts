import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Text } from '../text';
import { DttService }  from '../dtt.service';

@Component({
  selector: 'app-text',
  templateUrl: './text.component.html',
  styleUrls: ['./text.component.css']
})
export class TextComponent implements OnInit {

    text = {} as Text;
    mode: string;
    nextMode: string;
    canContinue: boolean;
    
    constructor(private route: ActivatedRoute,
                private dttService: DttService) { }
    
    ngOnInit(): void {
        this.getNextMode();
        this.getText();
    }

    getText(): void {
        const id = this.route.snapshot.paramMap.get('id');
        this.dttService.getText(id)
            .subscribe((text) => {
                this.text = text
                this.canContinue = this.nextMode != null || this.text.id.startsWith("test"); 
            });
    }
    
    getNextMode(): void {
        this.nextMode = this.dttService.getNextMode();
    }

    next(): void {
        if (this.nextMode) {
            this.dttService.start(this.nextMode);
        } else {
            this.dttService.nextAfterText(this.text.id);
        }
    }

    start(): void {
        this.dttService.start(this.mode);
    }

}
