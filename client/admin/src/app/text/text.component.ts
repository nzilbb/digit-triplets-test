import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { TextService }  from '../text.service';
import { Text } from '../text';

@Component({
  selector: 'app-text',
  templateUrl: './text.component.html',
  styleUrls: ['./text.component.css']
})
export class TextComponent implements OnInit {

    @Input() text: Text;

    constructor(private route: ActivatedRoute,
                private textService: TextService,
                private location: Location) { }
    
    ngOnInit(): void {
        this.getText();
    }
    
    getText(): void {
        const id = this.route.snapshot.paramMap.get('id');
        this.textService.getText(id).
            subscribe(text => this.text = text);
    }
    
    save(): void {
        this.textService.updateText(this.text)
            .subscribe(()=>this.location.back());        
    }
}
