import { Component, OnInit } from '@angular/core';

import { Text } from '../text';
import { TextService } from '../text.service';

@Component({
  selector: 'app-texts',
  templateUrl: './texts.component.html',
  styleUrls: ['./texts.component.css']
})
export class TextsComponent implements OnInit {
    texts: Text[];
    
    constructor(
        private textService: TextService
    ) {}
    
    ngOnInit(): void {
        this.readTexts();
    }

    readTexts(): void {
        this.textService.readTexts()
            .subscribe(texts => this.texts = texts);
    }

    updateText(text: Text) {
        this.textService.updateText(text)
            .subscribe(text => {
                // update the model with the text returned
                this.texts.findIndex(t => { return t.id == text.id; });
            });
    }
}
