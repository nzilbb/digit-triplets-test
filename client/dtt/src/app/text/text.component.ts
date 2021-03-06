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
    text2: Text;
    nextMode: string;
    canContinue: boolean;
    results: boolean;
    leaving = false;
    
    constructor(private route: ActivatedRoute,
                private dttService: DttService) { }
    
    ngOnInit(): void {
        this.getNextMode();
        this.getText();
    }

    getText(): void {
        const id = this.route.snapshot.paramMap.get('id');
        this.results = id.startsWith("result");
        this.dttService.getText(id)
            .subscribe((text) => {
                this.text = text
                this.canContinue = this.nextMode != null || this.text.id.startsWith("test"); 
            });
        const id2 = this.route.snapshot.paramMap.get('id2');
        if (id2) {
            this.dttService.getText(id2)
                .subscribe((text) => {
                    this.text2 = text
                });
        }
    }
    
    getNextMode(): void {
        this.nextMode = this.dttService.getNextMode();
    }

    next(): void {
        this.leaving = true;
        this.dttService.nextAfterText(this.text.id);
    }

    start(mode: string): void {
        this.leaving = true;
        this.dttService.start(mode);
    }

}
